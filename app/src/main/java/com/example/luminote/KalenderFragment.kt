package com.example.luminote

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.CheckBox
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class KalenderFragment : Fragment() {

    // ─── Views ───────────────────────────────────────────────────────────────
    private lateinit var tvMonthYear: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var btnToggleView: ImageButton
    private lateinit var layoutCalendarSection: LinearLayout
    private lateinit var rvCalendar: RecyclerView
    private lateinit var rvAgenda: RecyclerView
    private lateinit var tvAgendaDate: TextView
    private lateinit var tvEmptyAgenda: TextView
    private lateinit var progressAgenda: ProgressBar
    private lateinit var chipCatatan: CheckBox
    private lateinit var chipTugas: CheckBox

    // ─── Preferences ─────────────────────────────────────────────────────────
    private lateinit var catatanPreferences: CatatanPreferences
    private lateinit var tugasPreferences: TugasPreferences
    private lateinit var arsipPreferences: ArsipPreferences

    // ─── Adapters ────────────────────────────────────────────────────────────
    private lateinit var dayAdapter: KalenderDayAdapter
    private lateinit var agendaAdapter: KalenderAgendaAdapter

    // ─── State ───────────────────────────────────────────────────────────────
    private var currentCalendar = Calendar.getInstance()
    private var selectedDate: Calendar = Calendar.getInstance()
    private var showCatatan = true
    private var showTugas   = true
    private var isAgendaMode = false

    // ─── Threading ───────────────────────────────────────────────────────────
    // Single-thread executor: semua operasi baca data antri, tidak overlap
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    // ─── Formatters ──────────────────────────────────────────────────────────
    private val dateFormat      = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("id"))
    private val displayFormat   = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id"))

    // ─────────────────────────────────────────────────────────────────────────
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_kalender, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPreferences()
        bindViews(view)
        setupCalendarRv()
        setupAgendaRv()
        setupMonthNav()
        setupChipFilter()
        setupToggle()
        loadCalendarAsync()
        loadAgendaAsync(selectedDate)
    }

    override fun onResume() {
        super.onResume()
        loadCalendarAsync()
        loadAgendaAsync(selectedDate)
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
    }

    // ─── Init ────────────────────────────────────────────────────────────────

    private fun initPreferences() {
        catatanPreferences = CatatanPreferences(requireContext())
        tugasPreferences   = TugasPreferences(requireContext())
        arsipPreferences   = ArsipPreferences(requireContext())
    }

    private fun bindViews(view: View) {
        tvMonthYear           = view.findViewById(R.id.tv_kal_month_year)
        btnPrevMonth          = view.findViewById(R.id.btn_kal_prev)
        btnNextMonth          = view.findViewById(R.id.btn_kal_next)
        btnToggleView         = view.findViewById(R.id.btn_kal_toggle)
        layoutCalendarSection = view.findViewById(R.id.layout_kal_calendar_section)
        rvCalendar            = view.findViewById(R.id.rv_kal_calendar)
        rvAgenda              = view.findViewById(R.id.rv_kal_agenda)
        tvAgendaDate          = view.findViewById(R.id.tv_kal_agenda_date)
        tvEmptyAgenda         = view.findViewById(R.id.tv_kal_empty)
        progressAgenda        = view.findViewById(R.id.progress_kal_agenda)
        chipCatatan           = view.findViewById(R.id.chip_kal_catatan)
        chipTugas             = view.findViewById(R.id.chip_kal_tugas)
    }

    // ─── Setup ───────────────────────────────────────────────────────────────

    private fun setupCalendarRv() {
        dayAdapter = KalenderDayAdapter(requireContext(), emptyList()) { dayItem ->
            dayItem.date?.let { cal ->
                selectedDate = cal.clone() as Calendar
                // ✅ FIX: pindah ke background thread
                loadAgendaAsync(selectedDate)
            }
        }
        rvCalendar.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = dayAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupAgendaRv() {
        agendaAdapter = KalenderAgendaAdapter(requireContext(), emptyList()) { agendaItem ->
            openAgendaItem(agendaItem)
        }
        rvAgenda.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = agendaAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupMonthNav() {
        btnPrevMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            loadCalendarAsync()
            if (isAgendaMode) loadFullMonthAgendaAsync()
        }
        btnNextMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            loadCalendarAsync()
            if (isAgendaMode) loadFullMonthAgendaAsync()
        }
    }

    private fun setupChipFilter() {
        chipCatatan.isChecked = true
        chipTugas.isChecked   = true
        chipCatatan.setOnCheckedChangeListener { _, checked ->
            showCatatan = checked
            loadCalendarAsync()
            if (isAgendaMode) loadFullMonthAgendaAsync() else loadAgendaAsync(selectedDate)
        }
        chipTugas.setOnCheckedChangeListener { _, checked ->
            showTugas = checked
            loadCalendarAsync()
            if (isAgendaMode) loadFullMonthAgendaAsync() else loadAgendaAsync(selectedDate)
        }
    }

    private fun setupToggle() {
        // Set icon awal (OFF = kalender terlihat)
        btnToggleView.setImageResource(R.drawable.ic_toggle_calendar_off)

        btnToggleView.setOnClickListener {
            isAgendaMode = !isAgendaMode
            if (isAgendaMode) {
                // Mode agenda penuh — kalender tersembunyi
                layoutCalendarSection.visibility = View.GONE
                btnToggleView.setImageResource(R.drawable.ic_toggle_calendar_on)
                loadFullMonthAgendaAsync()
            } else {
                // Mode normal — kalender terlihat
                layoutCalendarSection.visibility = View.VISIBLE
                btnToggleView.setImageResource(R.drawable.ic_toggle_calendar_off)
                loadAgendaAsync(selectedDate)
            }
        }
    }

    // ─── Async Load (public API fragment) ────────────────────────────────────

    private fun loadCalendarAsync() {
        tvMonthYear.text = monthYearFormat.format(currentCalendar.time)
            .replaceFirstChar { it.uppercase() }

        executor.execute {
            val catatanSet = if (showCatatan) getCatatanDateSet() else emptySet()
            val tugasSet   = if (showTugas)   getTugasDateSet()   else emptySet()
            val days       = buildDayList(catatanSet, tugasSet)
            val selectedPos = days.indexOfFirst { item ->
                item.date != null && isSameDay(item.date, selectedDate)
            }
            mainHandler.post {
                if (!isAdded) return@post
                dayAdapter.updateData(days, selectedPos)
            }
        }
    }

    private fun loadAgendaAsync(date: Calendar) {
        showLoading(true)
        tvAgendaDate.text = displayFormat.format(date.time).replaceFirstChar { it.uppercase() }

        executor.execute {
            val items = buildAgendaForDate(date)
            mainHandler.post {
                if (!isAdded) return@post
                showLoading(false)
                showAgendaResult(items)
            }
        }
    }

    private fun loadFullMonthAgendaAsync() {
        showLoading(true)
        tvAgendaDate.text = "Semua agenda — ${monthYearFormat.format(currentCalendar.time)
            .replaceFirstChar { it.uppercase() }}"

        executor.execute {
            val items = buildFullMonthAgenda()
            mainHandler.post {
                if (!isAdded) return@post
                showLoading(false)
                showAgendaResult(items)
            }
        }
    }

    // ─── Builder (BACKGROUND THREAD ONLY) ────────────────────────────────────

    private fun buildDayList(
        catatanDateSet: Set<String>,
        tugasDateSet: Set<String>
    ): List<KalenderDayItem> {
        val result  = mutableListOf<KalenderDayItem>()
        val today   = Calendar.getInstance()
        val firstDay = (currentCalendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        val offset  = firstDay.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        repeat(offset) { result.add(KalenderDayItem()) }

        val totalDays = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..totalDays) {
            val cal = (currentCalendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
            val dateStr = dateFormat.format(cal.time)
            result.add(KalenderDayItem(
                date           = cal,
                hasCatatan     = catatanDateSet.contains(dateStr),
                hasTugas       = tugasDateSet.contains(dateStr),
                isToday        = isSameDay(cal, today),
                isSelected     = isSameDay(cal, selectedDate),
                isCurrentMonth = true
            ))
        }
        return result
    }

    private fun buildAgendaForDate(date: Calendar): List<KalenderAgendaItem> {
        val dateStr = dateFormat.format(date.time)
        val items   = mutableListOf<KalenderAgendaItem>()
        if (showCatatan) {
            catatanPreferences.getCatatanList()
                .filter { !arsipPreferences.isCatatanArsip(it.id) }
                .filter { parseDateOnly(it.tanggal) == dateStr }
                .forEach { items.add(KalenderAgendaItem.CatatanItem(it)) }
        }
        if (showTugas) {
            tugasPreferences.getAllTugas()
                .filter { !arsipPreferences.isTugasArsip(it.id) }
                .filter { parseDateOnly(it.tanggal) == dateStr }
                .sortedBy { it.tanggal?.split(" ")?.getOrNull(1) ?: "" }
                .forEach { items.add(KalenderAgendaItem.TugasItem(it)) }
        }
        return items
    }

    private fun buildFullMonthAgenda(): List<KalenderAgendaItem> {
        val year  = currentCalendar.get(Calendar.YEAR)
        val month = currentCalendar.get(Calendar.MONTH)
        val items = mutableListOf<KalenderAgendaItem>()
        if (showCatatan) {
            catatanPreferences.getCatatanList()
                .filter { !arsipPreferences.isCatatanArsip(it.id) }
                .filter { calFromDateStr(parseDateOnly(it.tanggal))?.let { c ->
                    c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month
                } ?: false }
                .sortedBy { it.tanggal }
                .forEach { items.add(KalenderAgendaItem.CatatanItem(it)) }
        }
        if (showTugas) {
            tugasPreferences.getAllTugas()
                .filter { !arsipPreferences.isTugasArsip(it.id) }
                .filter { calFromDateStr(it.tanggal?.split(" ")?.firstOrNull())?.let { c ->
                    c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month
                } ?: false }
                .sortedBy { it.tanggal }
                .forEach { items.add(KalenderAgendaItem.TugasItem(it)) }
        }
        return items
    }

    // ─── UI Helper ───────────────────────────────────────────────────────────

    private fun showLoading(loading: Boolean) {
        progressAgenda.visibility = if (loading) View.VISIBLE else View.GONE
        rvAgenda.visibility       = if (loading) View.GONE    else View.VISIBLE
    }

    private fun showAgendaResult(items: List<KalenderAgendaItem>) {
        agendaAdapter.updateData(items)
        rvAgenda.visibility      = if (items.isEmpty()) View.GONE    else View.VISIBLE
        tvEmptyAgenda.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    // ─── Navigasi ────────────────────────────────────────────────────────────

    private fun openAgendaItem(item: KalenderAgendaItem) {
        when (item) {
            is KalenderAgendaItem.CatatanItem -> {
                val c = item.catatan
                startActivity(Intent(requireContext(), CatatanActivity::class.java).apply {
                    putExtra("id", c.id); putExtra("judul", c.judul)
                    putExtra("deskripsi", c.deskripsi); putExtra("tanggal", c.tanggal)
                    putExtra("waktu", c.waktu)
                })
            }
            is KalenderAgendaItem.TugasItem -> {
                val t = item.tugas
                startActivity(Intent(requireContext(), TambahTugasActivity::class.java).apply {
                    putExtra("id", t.id); putExtra("judul", t.judul)
                    putExtra("deskripsi", t.deskripsi); putExtra("tanggal", t.tanggal)
                    putExtra("isSelesai", t.isSelesai)
                })
            }
        }
    }

    // ─── Data Helper ─────────────────────────────────────────────────────────

    private fun getCatatanDateSet(): Set<String> =
        catatanPreferences.getCatatanList()
            .filter { !arsipPreferences.isCatatanArsip(it.id) }
            .mapNotNull { parseDateOnly(it.tanggal) }.toSet()

    private fun getTugasDateSet(): Set<String> =
        tugasPreferences.getAllTugas()
            .filter { !arsipPreferences.isTugasArsip(it.id) }
            .mapNotNull { parseDateOnly(it.tanggal) }.toSet()

    private fun parseDateOnly(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val datePart = raw.trim().split(" ").firstOrNull() ?: return null
        val parts = datePart.split("/")
        if (parts.size != 3) return null
        return try {
            String.format("%02d/%02d/%04d", parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        } catch (e: NumberFormatException) { null }
    }

    private fun calFromDateStr(dateStr: String?): Calendar? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(dateStr) ?: return null
            cal
        } catch (e: Exception) { null }
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR)         == b.get(Calendar.YEAR) &&
                a.get(Calendar.MONTH)        == b.get(Calendar.MONTH) &&
                a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH)
}