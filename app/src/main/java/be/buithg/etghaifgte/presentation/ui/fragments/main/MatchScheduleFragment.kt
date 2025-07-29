package be.buithg.etghaifgte.presentation.ui.fragments.main

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

import be.buithg.etghaifgte.R
import be.buithg.etghaifgte.databinding.FragmentMatchScheduleBinding
import be.buithg.etghaifgte.domain.model.Match
import be.buithg.etghaifgte.presentation.ui.adapters.MatchAdapter
import be.buithg.etghaifgte.presentation.viewmodel.MatchScheduleViewModel
import be.buithg.etghaifgte.presentation.viewmodel.PredictionsViewModel
import be.buithg.etghaifgte.utils.NetworkUtils.isInternetAvailable

@AndroidEntryPoint
class MatchScheduleFragment : Fragment() {

    private var _binding: FragmentMatchScheduleBinding? = null
    private val binding get() = _binding!!

    private val viewModel            by viewModels<MatchScheduleViewModel>()
    private val predictionsViewModel by activityViewModels<PredictionsViewModel>()

    private lateinit var buttons: List<MaterialButton>
    private var allMatches: List<Match> = emptyList()
    private var selectedBtn: MaterialButton? = null
    private var loadError = false

    private lateinit var connectivityManager: ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentMatchScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Определяем выбранный ранее день
        val current = predictionsViewModel.getFilterDate()
        selectedBtn = when (current) {
            LocalDate.now().minusDays(1) -> binding.btnYesterday
            LocalDate.now().plusDays(1)  -> binding.btnTomorrow
            else                         -> binding.btnToday
        }
        predictionsViewModel.selectDate(current)

        // 2) Подписываемся на метрики прогнозов
        lifecycleScope.launchWhenStarted {
            combine(
                predictionsViewModel.selectedDate,
                predictionsViewModel.predYesterday,
                predictionsViewModel.predToday,
                predictionsViewModel.predTomorrow
            ) { sel, y, t, tom ->
                when (sel) {
                    LocalDate.now().minusDays(1) -> y
                    LocalDate.now()             -> t
                    LocalDate.now().plusDays(1) -> tom
                    else -> 0
                }
            }.collect { count ->
                binding.tvPredictedCount.text = count.toString().padStart(2, '0')
            }
        }

        lifecycleScope.launchWhenStarted {
            predictionsViewModel.dailyStats.collect { stats ->
                binding.tvWonCount.text = stats.won.toString().padStart(2, '0')
            }
        }

        // 3) Автозагрузка матчей при сети
        connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                lifecycleScope.launch { viewModel.loadMatches() }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback!!)

        // 4) Первая загрузка
        if (requireContext().isInternetAvailable()) {
            lifecycleScope.launch { viewModel.loadMatches() }
        } else {
            Log.e("MatchSchedule", "No Internet")
            allMatches = emptyList()
            filterAndDisplay(selectedBtn!!.id)
        }

        binding.btnHelp.setOnClickListener {
            findNavController().navigate(R.id.tutorialFragment)
        }
        binding.btnRetry.setOnClickListener {
            if (requireContext().isInternetAvailable()) lifecycleScope.launch { viewModel.loadMatches() }
            else Log.e("MatchSchedule", "No Internet")
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            if (isLoading) {
                binding.recyclerMatcher.isVisible = false
                binding.emptyText.isVisible = false
                binding.btnRetry.isVisible = false
            } else {
                filterAndDisplay(selectedBtn!!.id)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            loadError = err
            if (!viewModel.loading.value!!) {
                filterAndDisplay(selectedBtn!!.id)
            }
        }

        // 5) Слушаем список матчей
        viewModel.matches.observe(viewLifecycleOwner) { list ->
            allMatches = list.orEmpty()
            if (viewModel.loading.value != true) {
                filterAndDisplay(selectedBtn!!.id)
            }
        }

        // 6) Настраиваем табы Yesterday/Today/Tomorrow
        buttons = listOf(binding.btnYesterday, binding.btnToday, binding.btnTomorrow)
        buttons.forEach { btn ->
            btn.setOnClickListener {
                selectedBtn = btn
                updateSelection(btn)
                val date = when (btn.id) {
                    R.id.btnYesterday -> LocalDate.now().minusDays(1)
                    R.id.btnTomorrow  -> LocalDate.now().plusDays(1)
                    else              -> LocalDate.now()
                }
                predictionsViewModel.selectDate(date)
                filterAndDisplay(btn.id)
            }
        }
        updateSelection(selectedBtn!!)
    }

    private fun updateSelection(selected: MaterialButton) {
        buttons.forEach { btn ->
            val sel = btn == selected
            val fromC = btn.backgroundTintList?.defaultColor ?: Color.TRANSPARENT
            val toC   = if (sel) Color.parseColor("#FFCE01") else Color.TRANSPARENT
            ValueAnimator.ofArgb(fromC, toC).apply {
                duration = 250
                addUpdateListener { btn.backgroundTintList = ColorStateList.valueOf(it.animatedValue as Int) }
                start()
            }
            val fromT = btn.currentTextColor
            val toT   = if (sel) Color.BLACK else Color.WHITE
            ValueAnimator.ofArgb(fromT, toT).apply {
                duration = 250
                addUpdateListener { btn.setTextColor(it.animatedValue as Int) }
                start()
            }
        }
    }

    private fun filterAndDisplay(buttonId: Int) {
        val filtered = when (buttonId) {
            R.id.btnYesterday -> allMatches.filter { it.matchEnded }
            R.id.btnTomorrow  -> allMatches.filter { !it.matchEnded }
            else -> {
                val today = LocalDate.now()
                allMatches.filter {
                    runCatching { LocalDate.parse(it.date) }.getOrNull() == today
                }
            }
        }
        val toShow = filtered.take(10)

        // Upcoming matches count should reflect only items displayed in
        // the RecyclerView, not the entire filtered set
        val upcomingCount = toShow.count { !it.matchEnded }
        binding.tvUpcomingCount.text = upcomingCount.toString().padStart(2, '0')

        binding.recyclerMatcher.adapter = MatchAdapter(ArrayList(toShow)) { match ->
            findNavController().navigate(
                MatchScheduleFragmentDirections
                    .actionMatchScheduleFragmentToMatchDetailFragment(match, false)
            )
        }
        binding.emptyText.isVisible       = toShow.isEmpty()
        binding.btnRetry.isVisible        = loadError && toShow.isEmpty()
        binding.recyclerMatcher.isVisible = toShow.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        _binding = null
    }
}
