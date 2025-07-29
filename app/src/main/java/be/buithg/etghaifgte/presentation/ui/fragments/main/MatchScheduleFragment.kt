package be.buithg.etghaifgte.presentation.ui.fragments.main

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import be.buithg.etghaifgte.R
import be.buithg.etghaifgte.databinding.FragmentMatchScheduleBinding
import be.buithg.etghaifgte.presentation.ui.adapters.MatchAdapter
import be.buithg.etghaifgte.presentation.viewmodel.MatchScheduleViewModel
import be.buithg.etghaifgte.domain.model.Match

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.button.MaterialButton
import androidx.navigation.fragment.findNavController
import be.buithg.etghaifgte.presentation.ui.fragments.main.MatchScheduleFragmentDirections
import be.buithg.etghaifgte.presentation.viewmodel.PredictionsViewModel
import be.buithg.etghaifgte.utils.NetworkUtils.isInternetAvailable
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import java.time.LocalDate

@AndroidEntryPoint
class MatchScheduleFragment : Fragment() {

    private lateinit var binding: FragmentMatchScheduleBinding
    private val viewModel: MatchScheduleViewModel by viewModels()
    private val predictionsViewModel: PredictionsViewModel by viewModels()
    private lateinit var buttons: List<MaterialButton>
    private lateinit var adapter: MatchAdapter
    private var allMatches: List<Match> = emptyList()
    private var selectedBtn: MaterialButton? = null
    private lateinit var connectivityManager: ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMatchScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedBtn = binding.btnToday
        predictionsViewModel.setFilterDate(LocalDate.now())

        connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.loadMatches()
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback!!)

        if (requireContext().isInternetAvailable()) {
            viewModel.loadMatches()
        } else {
            Log.e("FFFF", "No Internet connection")
            allMatches = emptyList()
            filterAndDisplay( (selectedBtn ?: binding.btnToday).id )
        }

        binding.btnHelp.setOnClickListener {
            findNavController().navigate(R.id.tutorialFragment)
        }

        binding.btnRetry.setOnClickListener {
            if (requireContext().isInternetAvailable()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.loadMatches()
                }
            } else {
                Log.e("FFFF", "No Internet connection")
            }
        }

        viewModel.matches.observe(viewLifecycleOwner) { list ->
            allMatches = list ?: emptyList()
            filterAndDisplay( (selectedBtn ?: binding.btnToday).id )
        }

        predictionsViewModel.predictedCount.observe(viewLifecycleOwner) {
            binding.tvPredictedCount.text = it.toString()
        }
        predictionsViewModel.wonCount.observe(viewLifecycleOwner) {
            binding.tvWonCount.text = it.toString().padStart(2, '0')
        }
        predictionsViewModel.loadPredictions()

        buttons = listOf(
            binding.btnYesterday,
            binding.btnToday,
            binding.btnTomorrow
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                selectedBtn = button
                updateSelection(button)
                filterAndDisplay( (selectedBtn ?: binding.btnToday).id )
            }
        }

        updateSelection(binding.btnToday)
    }

    private fun updateSelection(selectedButton: MaterialButton) {
        buttons.forEach { button ->
            val isSelected = button == selectedButton

            val fromColor = (button.backgroundTintList?.defaultColor ?: Color.TRANSPARENT)
            val toColor = if (isSelected) Color.parseColor("#FFCE01") else Color.TRANSPARENT

            ValueAnimator.ofArgb(fromColor, toColor).apply {
                duration = 250
                addUpdateListener { animator ->
                    val color = animator.animatedValue as Int
                    button.backgroundTintList = ColorStateList.valueOf(color)
                }
                start()
            }

            val fromTextColor = (button.currentTextColor)
            val toTextColor = if (isSelected) Color.BLACK else Color.WHITE

            ValueAnimator.ofArgb(fromTextColor, toTextColor).apply {
                duration = 250
                addUpdateListener { animator ->
                    button.setTextColor(animator.animatedValue as Int)
                }
                start()
            }
        }
    }

    // в MatchScheduleFragment

    private fun filterAndDisplay(buttonId: Int) {
        val filtered = when (buttonId) {
            R.id.btnYesterday -> allMatches.filter { it.matchEnded }
            R.id.btnTomorrow  -> allMatches.filter { !it.matchEnded }
            else              -> {
                val today = LocalDate.now()
                allMatches.filter {
                    runCatching { LocalDate.parse(it.date) }.getOrNull() == today
                }
            }
        }
        val toShow = filtered.take(10)
        binding.tvUpcomingCount.text =
            toShow.count { !it.matchEnded }.toString().padStart(2, '0')
        binding.recyclerMatcher.adapter = MatchAdapter(ArrayList(toShow)) { match ->
            // Важный кусок — обязательно здесь навигация
            val action = MatchScheduleFragmentDirections
                .actionMatchScheduleFragmentToMatchDetailFragment(match, false)
            findNavController().navigate(action)
        }
        binding.emptyText.isVisible       = toShow.isEmpty()
        binding.btnRetry.isVisible        = toShow.isEmpty()
        binding.recyclerMatcher.isVisible = toShow.isNotEmpty()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
    }
}