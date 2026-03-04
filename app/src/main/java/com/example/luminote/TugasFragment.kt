package com.example.luminote

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TugasFragment : Fragment() {

    private lateinit var layoutTugasSelesai: LinearLayout
    private lateinit var icDiselesaikan: ImageView
    private lateinit var tvDiselesaikan: TextView
    private lateinit var rvTugasSelesai: RecyclerView
    private lateinit var tugasSelesaiAdapter: TugasAdapter
    private lateinit var rvTugas: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var tugasPreferences: TugasPreferences
    private lateinit var faforitPreferences: FaforitPreferences
    private lateinit var arsipPreferences: ArsipPreferences
    private lateinit var tugasAdapter: TugasAdapter

    // 🔥 TAMBAHAN: BroadcastReceiver untuk auto refresh
    private lateinit var refreshReceiver: BroadcastReceiver

    private var tampilkanTugasSelesai = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tugas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tugasPreferences = TugasPreferences(requireContext())
        faforitPreferences = FaforitPreferences(requireContext())
        arsipPreferences = ArsipPreferences(requireContext())

        layoutTugasSelesai = view.findViewById(R.id.layout_tugas_selesai)
        icDiselesaikan = view.findViewById(R.id.ic_diselesaikan)
        tvDiselesaikan = view.findViewById(R.id.tv_diselesaikan)
        rvTugasSelesai = view.findViewById(R.id.rv_tugas_selesai)
        rvTugas = view.findViewById(R.id.rv_tugas)
        etSearch = view.findViewById(R.id.et_search)

        setupSearch()
        setupRecyclerView()
        setupRecyclerViewSelesai()
        setupClickListeners()
        loadData()

        animasiTugasSelesai(tampilkanTugasSelesai)
        updateCompletedText(tampilkanTugasSelesai)
    }

    private fun setupRecyclerView() {
        tugasAdapter = TugasAdapter(
            listTugas = mutableListOf(),
            onEditClick = { tugas -> openEditTugas(tugas) },
            onDeleteClick = { tugas -> deleteTugas(tugas) },
            onCheckedChange = { tugas, isChecked -> updateStatusTugas(tugas, isChecked) },
            onFavoritClick = { tugas ->
                toggleFavorit(tugas)
            },
            onArsipClick = { tugas ->
                showArsipDialog(tugas)
            }
        )

        rvTugas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tugasAdapter
        }
    }

    private fun setupRecyclerViewSelesai() {
        tugasSelesaiAdapter = TugasAdapter(
            listTugas = mutableListOf(),
            onEditClick = { tugas -> openEditTugas(tugas) },
            onDeleteClick = { tugas -> deleteTugas(tugas) },
            onCheckedChange = { tugas, isChecked -> updateStatusTugas(tugas, isChecked) },
            onFavoritClick = { tugas ->
                toggleFavorit(tugas)
            },
            onArsipClick = { tugas ->
                showArsipDialog(tugas)
            }
        )

        rvTugasSelesai.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tugasSelesaiAdapter
        }
    }

    private fun setupClickListeners() {
        layoutTugasSelesai.setOnClickListener {
            tampilkanTugasSelesai = !tampilkanTugasSelesai
            animasiTugasSelesai(tampilkanTugasSelesai)
            updateCompletedText(tampilkanTugasSelesai)
        }
    }

    private fun animasiTugasSelesai(ditunjukkan: Boolean) {
        icDiselesaikan.animate()
            .rotation(if (ditunjukkan) 180f else 0f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        if (ditunjukkan) {
            rvTugasSelesai.apply {
                alpha = 0f
                isVisible = true
                animate().alpha(1f).setDuration(300).start()
            }
        } else {
            rvTugasSelesai.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction { rvTugasSelesai.isVisible = false }
                .start()
        }
    }

    private fun updateCompletedText(isExpanded: Boolean) {
        val completedCount = tugasSelesaiAdapter.itemCount
        tvDiselesaikan.text = if (isExpanded) {
            getString(R.string.completed_with_count, completedCount)
        } else {
            getString(R.string.completed)
        }
    }

    private fun loadData() {
        val allTugas = tugasPreferences.getAllTugas()

        // Filter: Jangan tampilkan yang sudah diarsipkan
        val tugasAktif = allTugas.filter { !arsipPreferences.isTugasArsip(it.id) }

        // Update status favorit dari FaforitPreferences
        tugasAktif.forEach { tugas ->
            tugas.isFavorit = faforitPreferences.isTugasFavorit(tugas.id)
        }

        val belumSelesai = tugasAktif.filter { !it.isSelesai }
        val selesai = tugasAktif.filter { it.isSelesai }

        tugasAdapter.updateData(belumSelesai)
        tugasSelesaiAdapter.updateData(selesai)

        updateCompletedText(tampilkanTugasSelesai)
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(inputteks: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(inputteks: CharSequence?, start: Int, before: Int, count: Int) {
                val query = inputteks.toString().lowercase()
                val allTugas = tugasPreferences.getAllTugas()

                // Update status favorit
                allTugas.forEach { tugas ->
                    tugas.isFavorit = faforitPreferences.isTugasFavorit(tugas.id)
                }

                val belumSelesaiList = allTugas.filter {
                    !it.isSelesai && it.matchesQuery(query)
                }
                val selesaiList = allTugas.filter {
                    it.isSelesai && it.matchesQuery(query)
                }

                tugasAdapter.updateData(belumSelesaiList)
                tugasSelesaiAdapter.updateData(selesaiList)

                rvTugasSelesai.isVisible = selesaiList.isNotEmpty()
                updateCompletedText(tampilkanTugasSelesai)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun toggleFavorit(tugas: Tugas) {
        // Toggle status favorit
        faforitPreferences.toggleTugasFavorit(tugas.id)
        tugas.isFavorit = faforitPreferences.isTugasFavorit(tugas.id)

        // Update adapter dengan notifikasi spesifik
        val position = tugasAdapter.listTugas.indexOf(tugas)
        if (position != -1) {
            tugasAdapter.notifyItemChanged(position)
        }

        val positionSelesai = tugasSelesaiAdapter.listTugas.indexOf(tugas)
        if (positionSelesai != -1) {
            tugasSelesaiAdapter.notifyItemChanged(positionSelesai)
        }

        // Tampilkan pesan
        val messageResId = if (tugas.isFavorit) {
            R.string.added_to_favorit
        } else {
            R.string.removed_from_favorit
        }
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }

    private fun openEditTugas(tugas: Tugas) {
        val intent = Intent(requireContext(), TambahTugasActivity::class.java).apply {
            putExtra("id", tugas.id)
            putExtra("judul", tugas.judul)
            putExtra("deskripsi", tugas.deskripsi)
            putExtra("tanggal", tugas.tanggal)
            putExtra("isSelesai", tugas.isSelesai)
        }
        startActivity(intent)
    }

    private fun deleteTugas(tugas: Tugas) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_hapus_tugas, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val tvPesan = dialogView.findViewById<TextView>(R.id.tv_pesan_hapus_tugas)
        val btnBatal = dialogView.findViewById<Button>(R.id.btn_batal_hapus_tugas)
        val btnHapus = dialogView.findViewById<Button>(R.id.btn_oke_hapus_tugas)

        tvPesan.text = getString(R.string.confirm_delete_tugas, tugas.judul)

        btnBatal.setOnClickListener { dialog.dismiss() }

        btnHapus.setOnClickListener {
            // 1. Batalkan semua alarm terkait tugas ini
            val alarmScheduler = AlarmScheduler(requireContext())
            alarmScheduler.cancelAllReminders(tugas.id)

            // 2. Hapus dari database/preferences
            tugasPreferences.deleteTugas(tugas.id)

            // 3. Hapus dari favorit jika ada
            if (faforitPreferences.isTugasFavorit(tugas.id)) {
                faforitPreferences.toggleTugasFavorit(tugas.id)
            }

            // 4. Update UI
            loadData()
            Toast.makeText(requireContext(), R.string.tugas_deleted, Toast.LENGTH_SHORT).show()

            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun updateStatusTugas(tugas: Tugas, isChecked: Boolean) {
        val updatedTugas = tugas.copy(isSelesai = isChecked)
        tugasPreferences.updateTugas(updatedTugas)

        if (isChecked) {
            val alarmScheduler = AlarmScheduler(requireContext())
            alarmScheduler.cancelAllReminders(tugas.id)
        }

        loadData()
    }

    // 🔥 FUNGSI PENTING: Register/Unregister BroadcastReceiver
    override fun onResume() {
        super.onResume()
        loadData()

        // Register broadcast receiver untuk refresh saat notifikasi diklik
        refreshReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                loadData() // Refresh list tugas
                Toast.makeText(requireContext(), getString(R.string.list_tugas_diperbarui), Toast.LENGTH_SHORT).show()
            }
        }

        val filter = IntentFilter("com.example.LumiNote.REFRESH_TUGAS")

        // 🔥 FIX: Support Android API 24+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(refreshReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            requireContext().registerReceiver(refreshReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            requireContext().unregisterReceiver(refreshReceiver)
        } catch (e: Exception) {
            // Receiver already unregistered
            e.printStackTrace()
        }
    }

    private fun showArsipDialog(tugas: Tugas) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_konfirmasi_arsip_tugas, null)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val tvPesan = dialogView.findViewById<TextView>(R.id.tv_pesan_konfirmasi)
        val btnBatal = dialogView.findViewById<Button>(R.id.btn_batal_konfirmasi)
        val btnOke = dialogView.findViewById<Button>(R.id.btn_oke_konfirmasi)

        tvPesan.text = getString(R.string.confirm_arsip_tugas, tugas.judul)

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnOke.setOnClickListener {
            val alarmScheduler = AlarmScheduler(requireContext())
            alarmScheduler.cancelAllReminders(tugas.id)

            arsipPreferences.arsipkanTugas(tugas.id)
            Toast.makeText(requireContext(), R.string.tugas_archived, Toast.LENGTH_SHORT).show()

            loadData()
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}