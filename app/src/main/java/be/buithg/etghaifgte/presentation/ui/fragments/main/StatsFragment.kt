package be.buithg.etghaifgte.presentation.ui.fragments.main

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import be.buithg.etghaifgte.R
import be.buithg.etghaifgte.databinding.FragmentStatsBinding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import be.buithg.etghaifgte.data.local.entity.PredictionEntity
import be.buithg.etghaifgte.presentation.viewmodel.PredictionsViewModel
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import be.buithg.etghaifgte.utils.parseUtcToLocal
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@AndroidEntryPoint
class StatsFragment : Fragment(R.layout.fragment_stats) {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PredictionsViewModel by activityViewModels()

    private fun isWin(item: PredictionEntity): Boolean {
        return when (item.wonMatches) {
            1 -> item.pick == item.teamA
            2 -> item.pick == item.teamB
            else -> false
        }
    }

    private fun updateSummary(list: List<PredictionEntity>) {
        val predicted = list.size
        val wins = list.count { isWin(it) }
        val accuracy = if (predicted > 0) (wins * 100 / predicted) else 0

        binding.predictionsText.text = predicted.toString()
        binding.winsText.text = wins.toString()
        binding.accuracyText.text = "$accuracy%"

        val now = LocalDate.now()
        val prev = now.minusMonths(1)

        fun monthAcc(y: Int, m: Int): Int {
            val monthList = list.filter {
                it.dateTime.parseUtcToLocal()?.let { dt ->
                    dt.year == y && dt.monthValue == m
                } ?: false
            }
            val w = monthList.count { isWin(it) }
            return if (monthList.isNotEmpty()) (w * 100 / monthList.size) else 0
        }

        val prevAcc = monthAcc(prev.year, prev.monthValue)
        val currAcc = monthAcc(now.year, now.monthValue)

        binding.LastMonthsText.text = "Last Month: ${prevAcc}%"
        binding.thisMonthsText.text = "This Month: ${currAcc}%"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
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
        viewModel.predictions.observe(viewLifecycleOwner) { list ->
            val hasData = list.isNotEmpty()
            binding.statsScroll.isVisible = hasData
            binding.emptyText.isVisible = !hasData

            if (hasData) {
                updateSummary(list)
                setupAccuracyChart(list)
                setupLineChart(list)
            }
        }
        viewModel.loadPredictions()
    }

    private fun setupAccuracyChart(data: List<PredictionEntity>) {
        val chart = binding.pieChart
        chart.apply {
            description.isEnabled = false
            isRotationEnabled = false
            legend.isEnabled = false
            setDrawEntryLabels(false)
            setUsePercentValues(false)
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 90f
            transparentCircleRadius = 0f
            setDrawRoundedSlices(true)
        }
        val correct = data.count { isWin(it) }.toFloat()
        val incorrect = data.count { !it.upcomingFlag && !isWin(it) }.toFloat()
        val pending = data.count { it.upcomingFlag }.toFloat()

        val entries = listOf(PieEntry(correct), PieEntry(incorrect), PieEntry(pending))
        val ds = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#FF4E4E"),
                Color.parseColor("#FFCE01"),
                Color.parseColor("#4EFF4E")
            )
            sliceSpace = 2f
            setDrawValues(false)
        }
        chart.data = PieData(ds)
        chart.invalidate()
    }

    private fun setupLineChart(data: List<PredictionEntity>) {
        val chart = binding.lineChart

        chart.apply {
            setBackgroundColor(Color.TRANSPARENT)
            description.isEnabled = false
            legend.isEnabled = true
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            axisRight.isEnabled = false
        }

        val parsed = data.mapNotNull { it.dateTime.parseUtcToLocal() }
        val monthsSet = parsed.map { it.monthValue }.distinct().sorted()
        val months = monthsSet.map { Month.of(it).name.take(3) }
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.parseColor("#AAAAAA")
            textSize = 10f
            setDrawAxisLine(true)
            setDrawGridLines(true)
            enableGridDashedLine(10f, 10f, 0f)
            valueFormatter = IndexAxisValueFormatter(months)
            granularity = 1f
            yOffset = 8f
        }

        chart.setExtraOffsets(
            /* left  */ 0f,
            /* top   */ 0f,
            /* right */ 0f,
            /* bottom*/ 16f
        )

        chart.axisLeft.apply {
            setDrawGridLines(true)
            enableGridDashedLine(10f,10f,0f)
            axisMinimum = 0f
            axisMaximum = 100f
            granularity = 20f
            textColor = Color.parseColor("#666666")
        }


        fun makeSet(values: List<Float>, color: Int): LineDataSet {
            return LineDataSet(values.mapIndexed { i, v -> Entry(i.toFloat(), v) }, "").apply {
                this.color = color
                lineWidth = 2.5f
                setDrawCircles(true)
                circleRadius = 4f
                setCircleColor(color)
                circleHoleRadius = 2f
                circleHoleColor = Color.BLACK
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
            }
        }

        fun monthAcc(year: Int, month: Int): Float {
            val monthData = data.filter {
                it.dateTime.parseUtcToLocal()?.let { dt ->
                    dt.year == year && dt.monthValue == month
                } ?: false
            }
            val wins = monthData.count { isWin(it) }
            return if (monthData.isNotEmpty()) wins * 100f / monthData.size else 0f
        }

        val colorMap = mapOf(
            2021 to Color.parseColor("#007AFF"),
            2022 to Color.parseColor("#4CD964"),
            2023 to Color.parseColor("#FF9500"),
            2024 to Color.parseColor("#32D3F2")
        )

        val sets = listOf(2021, 2022, 2023, 2024).map { year ->
            val values = monthsSet.map { monthAcc(year, it) }
            makeSet(values, colorMap[year] ?: Color.WHITE)
        }

        chart.data = LineData(sets)
        chart.legend.apply {
            verticalAlignment   = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation         = Legend.LegendOrientation.HORIZONTAL

            form      = Legend.LegendForm.CIRCLE
            formSize  = 8f
            textColor = Color.parseColor("#CCCCCC")

            xEntrySpace = 16f
            yEntrySpace = 4f

            setDrawInside(false)
            isEnabled = true

            setCustom(listOf(
                LegendEntry("2021", Legend.LegendForm.CIRCLE, 8f, 2f, null, Color.parseColor("#007AFF")),
                LegendEntry("2022", Legend.LegendForm.CIRCLE, 8f, 2f, null, Color.parseColor("#4CD964")),
                LegendEntry("2023", Legend.LegendForm.CIRCLE, 8f, 2f, null, Color.parseColor("#FF9500")),
                LegendEntry("2024", Legend.LegendForm.CIRCLE, 8f, 2f, null, Color.parseColor("#32D3F2"))
            ))
        }


        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
