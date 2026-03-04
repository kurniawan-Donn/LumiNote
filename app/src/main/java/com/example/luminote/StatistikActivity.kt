package com.example.luminote

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.*

class StatistikActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var tvJumlahCatatan: TextView
    private lateinit var tvJumlahTugas: TextView
    private lateinit var tvJumlahSelesai: TextView
    private lateinit var tvJumlahTertunda: TextView
    private lateinit var progressCircle: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvProgressText: TextView
    private lateinit var tvQuote1: TextView
    private lateinit var tvQuote2: TextView
    private lateinit var backButton: ImageView

    private lateinit var catatanPrefs: CatatanPreferences
    private lateinit var tugasPrefs: TugasPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistik)

        catatanPrefs = CatatanPreferences(this)
        tugasPrefs = TugasPreferences(this)

        initViews()
        backButton.setOnClickListener { finish() }

        loadStatistik()
    }

    private fun initViews() {
        barChart = findViewById(R.id.barChart)
        tvJumlahCatatan = findViewById(R.id.tvJumlahCatatan)
        tvJumlahTugas = findViewById(R.id.tvJumlahTugas)
        tvJumlahSelesai = findViewById(R.id.tvJumlahSelesai)
        tvJumlahTertunda = findViewById(R.id.tvJumlahTertunda)
        progressCircle = findViewById(R.id.progressCircle)
        tvProgress = findViewById(R.id.tvProgress)
        tvProgressText = findViewById(R.id.tvProgressText)
        tvQuote1 = findViewById(R.id.tvQuote1)
        tvQuote2 = findViewById(R.id.tvQuote2)
        backButton = findViewById(R.id.backButton)
    }

    private fun loadStatistik() {
        val allCatatan = catatanPrefs.getCatatanList()
        val allTugas = tugasPrefs.getAllTugas()

        val jumlahCatatan = allCatatan.size
        val jumlahTugas = allTugas.size
        val jumlahSelesai = allTugas.count { it.isSelesai }
        val jumlahTertunda = allTugas.count { !it.isSelesai }

        animateTextView(tvJumlahCatatan, jumlahCatatan)
        animateTextView(tvJumlahTugas, jumlahTugas)
        animateTextView(tvJumlahSelesai, jumlahSelesai)
        animateTextView(tvJumlahTertunda, jumlahTertunda)

        setupBarChart(allTugas)

        val progress = calculateProgress(jumlahSelesai, jumlahTugas)
        updateProgressCircle(progress)

        generateMotivationQuotes(allCatatan, allTugas, progress)
    }

    // =========================================================
    // BAR CHART – HANYA DATA TUGAS
    // =========================================================
    private fun setupBarChart(tugasList: List<Tugas>) {

        val isDarkMode = ThemeHelper.isDarkMode(this)

        val textColor = getColor(R.color.text_primary)
        val gridColor = getColor(R.color.divider_color)
        val axisLineColor = getColor(R.color.border_color)
        val backgroundColor = getColor(R.color.background_secondary)

        val barColorCreated =
            if (isDarkMode) "#9772FF".toColorInt() else "#7C4DFF".toColorInt()
        val barColorCompleted =
            if (isDarkMode) "#6DC571".toColorInt() else "#4CAF50".toColorInt()

        val calendar = Calendar.getInstance()
        val dayLabels = mutableListOf<String>()
        val createdData = mutableListOf<BarEntry>()
        val completedData = mutableListOf<BarEntry>()

        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfWeek = calendar.timeInMillis

        for (i in 0..6) {
            val dayStart = startOfWeek + (i * 24 * 60 * 60 * 1000)
            val dayEnd = dayStart + (24 * 60 * 60 * 1000)

            val createdCount = tugasList.count {
                it.timestamp in dayStart until dayEnd
            }.toFloat()

            val completedCount = tugasList.count {
                it.isSelesai && it.timestamp in dayStart until dayEnd
            }.toFloat()

            createdData.add(BarEntry(i.toFloat(), createdCount))
            completedData.add(BarEntry(i.toFloat(), completedCount))

            calendar.timeInMillis = dayStart
            val dayName = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> getString(R.string.day_monday_short)
                Calendar.TUESDAY -> getString(R.string.day_tuesday_short)
                Calendar.WEDNESDAY -> getString(R.string.day_wednesday_short)
                Calendar.THURSDAY -> getString(R.string.day_thursday_short)
                Calendar.FRIDAY -> getString(R.string.day_friday_short)
                Calendar.SATURDAY -> getString(R.string.day_saturday_short)
                Calendar.SUNDAY -> getString(R.string.day_sunday_short)
                else -> ""
            }
            dayLabels.add(dayName)
        }

        val createdSet = BarDataSet(createdData, getString(R.string.chart_label_created)).apply {
            color = barColorCreated
            valueTextColor = textColor
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String =
                    if (value > 0) value.toInt().toString() else ""
            }
        }

        val completedSet = BarDataSet(completedData, getString(R.string.chart_label_completed)).apply {
            color = barColorCompleted
            valueTextColor = textColor
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String =
                    if (value > 0) value.toInt().toString() else ""
            }
        }

        val barData = BarData(createdSet, completedSet).apply {
            barWidth = 0.35f
        }

        barChart.apply {
            data = barData
            description.isEnabled = false
            setDrawGridBackground(false)
            setScaleEnabled(false)
            setBackgroundColor(backgroundColor)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(dayLabels)
                granularity = 1f
                setDrawGridLines(false)
            }

            axisLeft.apply {
                axisMinimum = 0f
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String =
                        value.toInt().toString()
                }
            }

            axisRight.isEnabled = false

            legend.apply {
                isEnabled = true
                form = Legend.LegendForm.SQUARE
            }

            groupBars(0f, 0.1f, 0.05f)
            animateY(1000)
            invalidate()
        }
    }

    // =========================================================
    // PROGRESS
    // =========================================================
    private fun calculateProgress(selesai: Int, total: Int): Int =
        if (total > 0) ((selesai.toFloat() / total) * 100).toInt() else 0

    private fun updateProgressCircle(progress: Int) {
        progressCircle.progress = progress
        tvProgress.text = getString(R.string.progress_percentage, progress)
        tvProgressText.text = getString(R.string.progress_text, progress)
        tvProgress.startAnimation(
            AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        )
    }

    // =========================================================
    // QUOTES
    // =========================================================
    private fun generateMotivationQuotes(
        catatanList: List<Catatan>,
        tugasList: List<Tugas>,
        progress: Int
    ) {
        val productiveDayText = analyzeProductiveDay(catatanList, tugasList)
        tvQuote1.text = getString(R.string.quote_format, productiveDayText)

        val trendMessage = analyzeTrend(tugasList, progress)
        tvQuote2.text = getString(R.string.quote_format, trendMessage)
    }

    private fun analyzeProductiveDay(
        catatanList: List<Catatan>,
        tugasList: List<Tugas>
    ): String {

        if (catatanList.isEmpty() && tugasList.isEmpty()) {
            return getString(R.string.day_monday_short)
        }

        val calendar = Calendar.getInstance()
        val dayCount = mutableMapOf<Int, Int>()

        val allItems = catatanList.map { it.timestamp } +
                tugasList.map { it.timestamp }

        allItems.forEach { timestamp ->
            calendar.timeInMillis = timestamp
            val day = calendar.get(Calendar.DAY_OF_WEEK)
            dayCount[day] = (dayCount[day] ?: 0) + 1
        }

        val mostProductiveDay = dayCount.maxByOrNull { it.value }?.key
            ?: Calendar.MONDAY

        val daysOfWeek = resources.getStringArray(R.array.days_of_week)
        val dayIndex = (mostProductiveDay - 1).coerceIn(0, 6)
        val dayName = daysOfWeek[dayIndex]

        return getString(R.string.quote_productive_day, dayName)
    }

    private fun analyzeTrend(tugasList: List<Tugas>, progress: Int): String =
        when {
            progress >= 80 -> getString(R.string.quote_trend_excellent)
            progress >= 60 -> getString(R.string.quote_trend_good)
            progress >= 40 -> getString(R.string.quote_trend_keep_going)
            tugasList.any { it.isSelesai } -> getString(R.string.quote_trend_achievement)
            else -> getString(R.string.quote_trend_start)
        }

    private fun animateTextView(textView: TextView, targetValue: Int) {
        ValueAnimator.ofInt(0, targetValue).apply {
            duration = 1000
            addUpdateListener {
                textView.text = it.animatedValue.toString()
            }
            start()
        }
    }
}