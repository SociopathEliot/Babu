package be.buithg.etghaifgte.presentation.ui.fragments.main

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import be.buithg.etghaifgte.R
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import be.buithg.etghaifgte.databinding.FragmentPredictionHistoryBinding
import androidx.core.view.isVisible
import be.buithg.etghaifgte.data.local.entity.PredictionEntity
import be.buithg.etghaifgte.presentation.ui.adapters.HistoryAdapter
import be.buithg.etghaifgte.presentation.viewmodel.PredictionsViewModel
import be.buithg.etghaifgte.domain.model.Match
import com.google.android.material.button.MaterialButton
import java.time.LocalDateTime
import be.buithg.etghaifgte.utils.parseUtcToLocal
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PredictionHistoryFragment : Fragment() {

    private lateinit var binding: FragmentPredictionHistoryBinding
    private val viewModel: PredictionsViewModel by activityViewModels()

    private lateinit var buttons: List<MaterialButton>
    private var allPredictions: List<PredictionEntity> = emptyList()
    private var selectedBtn: MaterialButton? = null

    private enum class Filter { ALL, WINNER, LOST }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPredictionHistoryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.matchScheduleFragment)
            }
        })

        binding.btnHelp.setOnClickListener {
            findNavController().navigate(R.id.tutorialFragment)
        }

        buttons = listOf(binding.btnYesterday, binding.btnToday, binding.btnTomorrow)

        val filterMap = mapOf(
            binding.btnYesterday to Filter.ALL,
            binding.btnToday to Filter.WINNER,
            binding.btnTomorrow to Filter.LOST
        )

        buttons.forEach { btn ->
            btn.setOnClickListener {
                selectedBtn = btn
                updateSelection(btn)
                filterMap[btn]?.let { filterAndDisplay(it) }
            }
        }

        selectedBtn = binding.btnToday
        updateSelection(binding.btnToday)

        viewModel.predictions.observe(viewLifecycleOwner) { list ->
            allPredictions = list
            filterMap[selectedBtn ?: binding.btnToday]?.let { filterAndDisplay(it) }
        }
        viewModel.loadPredictions()
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

    private fun isUpcoming(item: PredictionEntity): Boolean {
        if (item.upcoming == 1) return true
        val dt = item.dateTime.parseUtcToLocal()
        return dt?.isAfter(LocalDateTime.now()) ?: false
    }

    private fun getResult(item: PredictionEntity): String {
        if (isUpcoming(item)) return "матч еще не начался"
        return when (item.wonMatches) {
            1 -> if (item.pick == item.teamA) "Win" else "Lose"
            2 -> if (item.pick == item.teamB) "Win" else "Lose"
            else -> "Draw"
        }
    }
    private fun filterAndDisplay(filter: Filter) {
        val list = when (filter) {
            Filter.ALL -> allPredictions
            Filter.WINNER -> allPredictions.filter { getResult(it) == "Win" }
            Filter.LOST -> allPredictions.filter { getResult(it) == "Lose" }
        }
        binding.predictionsHistoryRecyclerview.adapter = HistoryAdapter(list) { prediction ->
            val match = prediction.toMatch()
            val action = PredictionHistoryFragmentDirections.actionPredictionHistoryFragmentToMatchDetailFragment(
                match,
                true
            )
            findNavController().navigate(action)
        }
    }

    private fun PredictionEntity.toMatch(): Match {
        val upcomingFlag = isUpcoming(this)
        val status = if (upcomingFlag) {
            "Upcoming"
        } else {
            when (wonMatches) {
                1 -> "$teamA won"
                2 -> "$teamB won"
                else -> "Draw"
            }
        }

        return Match(
            date = dateTime.substringBefore("T"),
            dateTimeGMT = dateTime,
            status = status,
            matchType = null,
            league = matchType,
            venue = stadium,
            city = city,
            country = country,
            teamA = teamA,
            teamB = teamB,
            scoreA = null,
            scoreB = null,
            matchEnded = !upcomingFlag
        )
    }

}