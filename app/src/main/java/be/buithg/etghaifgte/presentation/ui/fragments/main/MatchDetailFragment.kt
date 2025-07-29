package be.buithg.etghaifgte.presentation.ui.fragments.main

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import be.buithg.etghaifgte.R
import be.buithg.etghaifgte.data.local.entity.PredictionEntity
import be.buithg.etghaifgte.databinding.DialogPredictWinnerBinding
import be.buithg.etghaifgte.databinding.FragmentMatchDetailBinding
import be.buithg.etghaifgte.domain.model.Match
import be.buithg.etghaifgte.presentation.viewmodel.NoteViewModel
import be.buithg.etghaifgte.presentation.viewmodel.PredictionsViewModel
import be.buithg.etghaifgte.utils.parseUtcToLocal

@AndroidEntryPoint
class MatchDetailFragment : Fragment() {

    private var _binding: FragmentMatchDetailBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<MatchDetailFragmentArgs>()
    private val predictionsViewModel: PredictionsViewModel by activityViewModels()
    private val noteViewModel: NoteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )

        binding.btnHelp.setOnClickListener {
            findNavController().navigate(R.id.tutorialFragment)
        }

        val match = args.match
        bindMatch(match)
        if (args.fromHistory || match.matchEnded) {
            binding.btnMakeForecast.visibility = View.GONE
        }

        val noteKey = "${match.teamA}_${match.teamB}_${match.dateTimeGMT}"
        noteViewModel.loadNote(noteKey)
        noteViewModel.noteText.observe(viewLifecycleOwner) { text ->
            binding.tvNote.text = text
                ?.takeIf { it.isNotBlank() }
                ?: getString(R.string.no_notes)
        }

        binding.btnToday.setOnClickListener { switchMode(true) }
        binding.btnTomorrow.setOnClickListener { switchMode(false) }
        switchMode(true)

        binding.btnMakeForecast.setOnClickListener {
            showForecastDialog(match)
        }
        binding.btnSaveNote.setOnClickListener {
            val newText = binding.editNote.text.toString()
            noteViewModel.saveNote(noteKey, newText)
            switchMode(true)
        }
    }

    private fun switchMode(infoMode: Boolean) {
        binding.infoContainer.visibility = if (infoMode) View.VISIBLE else View.GONE
        binding.tvNote.visibility      = if (infoMode) View.VISIBLE else View.GONE
        binding.editNote.visibility    = if (infoMode) View.GONE    else View.VISIBLE
        binding.btnSaveNote.visibility = if (infoMode) View.GONE    else View.VISIBLE

        val (btnInfo, btnEdit) = binding.btnToday to binding.btnTomorrow
        listOf(btnInfo, btnEdit).forEach { btn ->
            val sel = (btn == btnInfo) == infoMode
            btn.setBackgroundColor(if (sel) Color.parseColor("#FFCE01") else Color.TRANSPARENT)
            btn.setTextColor(if (sel) Color.BLACK else Color.WHITE)
        }

        if (!infoMode) {
            binding.editNote.setText(noteViewModel.noteText.value.orEmpty())
        }
    }

    private fun showForecastDialog(match: Match) {
        val dialog = Dialog(requireContext())
        val dlg = DialogPredictWinnerBinding.inflate(layoutInflater)
        dialog.setContentView(dlg.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var selected: String? = null
        dlg.teamAText.text = match.teamA
        dlg.teamBText.text = match.teamB

        fun highlight(a: CardView, b: CardView) {
            a.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellow))
            b.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        dlg.cardTeamA.setOnClickListener {
            selected = match.teamA; highlight(dlg.cardTeamA, dlg.cardTeamB)
        }
        dlg.cardTeamB.setOnClickListener {
            selected = match.teamB; highlight(dlg.cardTeamB, dlg.cardTeamA)
        }
        dlg.btnClose.setOnClickListener { dialog.dismiss() }

        dlg.btnSubmit.setOnClickListener {
            val pick     = selected ?: "Draw"
            val upcoming = if (match.matchEnded) 0 else 1
            val won      = if (match.matchEnded) winnerTeam(match) else 0

            val parts  = match.venue.orEmpty().split(",").map(String::trim)
            val stadium = parts.getOrNull(0).orEmpty()
            val city    = parts.getOrNull(1).orEmpty()
            val country = match.country.orEmpty()

            val matchTime = runCatching {
                java.time.OffsetDateTime.parse(match.dateTimeGMT)
                    .toInstant()
                    .toEpochMilli()
            }.getOrDefault(0L)
            val wonFlag = when (won) {
                1 -> pick == match.teamA
                2 -> pick == match.teamB
                else -> false
            }
            val entity = PredictionEntity(
                teamA      = match.teamA.orEmpty(),
                teamB      = match.teamB.orEmpty(),
                dateTime   = match.dateTimeGMT.orEmpty(),
                matchTime  = matchTime,
                matchType  = match.league.orEmpty(),
                stadium    = stadium,
                city       = city,
                country    = country,
                pick       = pick,
                predicted  = 1,
                corrects   = 0,
                upcoming   = upcoming,
                wonMatches = won,
                upcomingFlag = upcoming == 1,
                won = wonFlag
            )
            predictionsViewModel.addPrediction(entity)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun bindMatch(m: Match) {
        val dfDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val dfTime = DateTimeFormatter.ofPattern("HH:mm")
        val date   = runCatching { LocalDate.parse(m.date.orEmpty()) }.getOrNull()
        val time   = m.dateTimeGMT.parseUtcToLocal()

        binding.tvDateValue.text    = date?.format(dfDate) ?: m.date.orDash()
        binding.tvTimeValue.text    = time?.format(dfTime) ?: m.dateTimeGMT.orDash()
        binding.teamTitle.text      = "${m.teamA.orDash()} - ${m.teamB.orDash()}"
        binding.statusText.text     = m.status.orDash()
        binding.tvStadiumValue.text = m.venue.orDash()
        binding.tvCityValue.text    = m.city.orDash()
        binding.tvCountryValue.text = m.country.orDash()
        binding.tvLeagueValue.text  = m.league?.uppercase().orDash()
    }

    private fun winnerTeam(m: Match): Int {
        val a = m.scoreA ?: return 0
        val b = m.scoreB ?: return 0
        return when {
            a > b    -> 1
            b > a    -> 2
            else     -> 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun String?.orDash() = this?.takeIf { it.isNotBlank() } ?: "-"
}
