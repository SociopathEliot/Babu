package be.buithg.etghaifgte.presentation.ui.fragments.main

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import be.buithg.etghaifgte.R
import be.buithg.etghaifgte.databinding.DialogPredictWinnerBinding
import be.buithg.etghaifgte.databinding.FragmentMatchDetailBinding
import androidx.navigation.fragment.navArgs
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import be.buithg.etghaifgte.data.local.entity.PredictionEntity
import be.buithg.etghaifgte.presentation.viewmodel.PredictionsViewModel
import be.buithg.etghaifgte.presentation.viewmodel.NoteViewModel
import be.buithg.etghaifgte.domain.model.Match
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import be.buithg.etghaifgte.utils.parseUtcToLocal

@AndroidEntryPoint
class MatchDetailFragment : Fragment() {

    private fun String?.orDash(): String = this?.takeIf { it.isNotBlank() } ?: "-"

    private fun winnerTeam(match: Match): Int {
        val scoreA = match.scoreA ?: return 0
        val scoreB = match.scoreB ?: return 0
        return when {
            scoreA > scoreB -> 1
            scoreB > scoreA -> 2
            else -> 0
        }
    }
    private val args: MatchDetailFragmentArgs by navArgs()

    private lateinit var binding: FragmentMatchDetailBinding
    private val predictionsViewModel: PredictionsViewModel by viewModels()
    private val noteViewModel: NoteViewModel by viewModels()
    private var selectedTeam: String? = null
    private lateinit var buttons: List<MaterialButton>
    private var selectedBtn: MaterialButton? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMatchDetailBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        binding.btnHelp.setOnClickListener {
            findNavController().navigate(R.id.tutorialFragment)
        }

        val match = args.match
        if (args.fromHistory || match.matchEnded) {
            binding.btnMakeForecast.visibility = View.GONE
        }
        bindMatch(match)
        val team1Key = match.teamA
        val team2Key = match.teamB
        val noteKey = "${team1Key}_${team2Key}_${match.dateTimeGMT ?: ""}"
        noteViewModel.loadNote(noteKey)
        noteViewModel.noteText.observe(viewLifecycleOwner) { text ->
            binding.tvNote.text = text?.takeIf { it.isNotBlank() }
                ?: getString(R.string.no_notes)
        }

        val infoContainer = binding.infoContainer
        val editNote = binding.editNote
        val saveButton = binding.btnSaveNote

        buttons = listOf(binding.btnToday, binding.btnTomorrow)
        selectedBtn = binding.btnToday
        updateSelection(binding.btnToday)

        binding.btnToday.setOnClickListener {
            selectedBtn = binding.btnToday
            updateSelection(binding.btnToday)
            infoContainer.visibility = View.VISIBLE
            editNote.visibility = View.GONE
            saveButton.visibility = View.GONE
            binding.tvNote.visibility = View.VISIBLE
        }


        binding.btnMakeForecast.setOnClickListener {
            val dialog = Dialog(requireContext())
            val dialogBinding = DialogPredictWinnerBinding.inflate(layoutInflater)
            dialog.setContentView(dialogBinding.root)

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            selectedTeam = null

            val team1 = match.teamA
            val team2 = match.teamB
            dialogBinding.teamAText.text = team1
            dialogBinding.teamBText.text = team2

            dialogBinding.cardTeamA.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            dialogBinding.cardTeamB.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))

            fun highlightSelection(selectedCard: CardView, otherCard: CardView) {
                val yellow = ContextCompat.getColor(requireContext(), R.color.yellow)
                val white = ContextCompat.getColor(requireContext(), R.color.white)
                selectedCard.setCardBackgroundColor(yellow)
                otherCard.setCardBackgroundColor(white)
            }

            dialogBinding.cardTeamA.setOnClickListener {
                selectedTeam = team1
                highlightSelection(dialogBinding.cardTeamA, dialogBinding.cardTeamB)
            }

            dialogBinding.cardTeamB.setOnClickListener {
                selectedTeam = team2
                highlightSelection(dialogBinding.cardTeamB, dialogBinding.cardTeamA)
            }

            dialogBinding.btnClose.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.btnSubmit.setOnClickListener {
                val pick = selectedTeam ?: "Draw"

                val upcoming = if (match.matchEnded) 0 else 1
                val wonMatches = if (match.matchEnded) winnerTeam(match) else 0

                val venueParts = match.venue?.split(",")?.map { it.trim() } ?: emptyList()
                val stadium = venueParts.getOrNull(0) ?: match.venue.orEmpty()
                val city = venueParts.getOrNull(1) ?: ""

                val entity = PredictionEntity(
                    teamA = team1.toString(),
                    teamB = team2.toString(),
                    dateTime = match.dateTimeGMT ?: "",
                    matchType = match.league ?: "",
                    stadium = stadium,
                    city = city,
                    pick = pick,
                    predicted = 1,
                    corrects = 0,
                    upcoming = upcoming,
                    wonMatches = wonMatches

                )
                predictionsViewModel.addPrediction(entity)
                dialog.dismiss()
            }

            dialog.show()
        }

        binding.btnTomorrow.setOnClickListener {
            selectedBtn = binding.btnTomorrow
            updateSelection(binding.btnTomorrow)
            infoContainer.visibility = View.GONE
            binding.tvNote.visibility = View.GONE
            editNote.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
            editNote.setText(noteViewModel.noteText.value ?: "")
        }

        saveButton.setOnClickListener {
            val text = editNote.text.toString()
            noteViewModel.saveNote(noteKey, text)
            binding.tvNote.text = text.takeIf { it.isNotBlank() } ?: getString(R.string.no_notes)
            infoContainer.visibility = View.VISIBLE
            editNote.visibility = View.GONE
            saveButton.visibility = View.GONE
            binding.tvNote.visibility = View.VISIBLE
            selectedBtn = binding.btnToday
            updateSelection(binding.btnToday)

        }

    }

    private fun updateSelection(selectedButton: MaterialButton) {
        buttons.forEach { button ->
            val isSelected = button == selectedButton
            val bg = if (isSelected) "#FFCE01" else "#00000000"
            val text = if (isSelected) "#000000" else "#FFFFFF"
            button.setBackgroundColor(Color.parseColor(bg))
            button.setTextColor(Color.parseColor(text))
        }
    }

    private fun bindMatch(match: Match) {
        val formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val formatterTime = DateTimeFormatter.ofPattern("HH:mm")

        val date = runCatching { LocalDate.parse(match.date ?: "") }.getOrNull()
        val time = match.dateTimeGMT.parseUtcToLocal()

        binding.tvDateValue.text = date?.format(formatterDate) ?: (match.date.orDash())
        binding.tvTimeValue.text = time?.format(formatterTime) ?: (match.dateTimeGMT.orDash())

        val team1 = match.teamA ?: "-"
        val team2 = match.teamB ?: "-"
        binding.teamTitle.text = "$team1 - $team2"

        binding.statusText.text = match.status.orDash()

        val stadium = match.venue.orDash()
        val city = match.city ?: "-"

        binding.tvStadiumValue.text = stadium
        binding.tvCityValue.text = city

        val country = match.country ?: "-"
        binding.tvCountryValue.text = country

        binding.tvLeagueValue.text = match.league?.uppercase() ?: "-"
    }
}