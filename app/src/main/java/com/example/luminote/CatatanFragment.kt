package com.example.luminote

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CatatanFragment : Fragment() {

    private lateinit var preferences: CatatanPreferences
    private lateinit var faforitPreferences: FaforitPreferences
    private lateinit var arsipPreferences: ArsipPreferences
    private lateinit var adapter: CatatanAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private var allCatatan = listOf<Catatan>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_catatan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = CatatanPreferences(requireContext())
        faforitPreferences = FaforitPreferences(requireContext())
        arsipPreferences = ArsipPreferences(requireContext())

        recyclerView = view.findViewById(R.id.rv_catatan)
        searchEditText = view.findViewById(R.id.et_search)

        setupRecyclerView()
        setupSearch()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = CatatanAdapter(
            catatanList = emptyList(),
            onItemClick = { catatan ->
                val intent = Intent(requireContext(), CatatanActivity::class.java).apply {
                    putExtra("id", catatan.id)
                    putExtra("judul", catatan.judul)
                    putExtra("deskripsi", catatan.deskripsi)
                    putExtra("tanggal", catatan.tanggal)
                    putExtra("waktu", catatan.waktu)
                }
                startActivity(intent)
            },
            onEditClick = { catatan ->
                val intent = Intent(requireContext(), TambahCatatanActivity::class.java).apply {
                    putExtra("id", catatan.id)
                    putExtra("judul", catatan.judul)
                    putExtra("deskripsi", catatan.deskripsi)
                    putExtra("tanggal", catatan.tanggal)
                    putExtra("waktu", catatan.waktu)
                }
                startActivity(intent)
            },
            onDeleteClick = { catatan ->
                showDeleteConfirmation(catatan)
            },
            onFavoritClick = { catatan ->
                toggleFavorit(catatan)
            },
            onArsipClick = { catatan ->
                showArsipDialog(catatan)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchCatatan(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadData() {
        allCatatan = preferences.getCatatanList().sortedByDescending { it.timestamp }
        allCatatan = allCatatan.filter { !arsipPreferences.isCatatanArsip(it.id) }
        allCatatan.forEach { catatan ->
            catatan.isFavorit = faforitPreferences.isCatatanFavorit(catatan.id)
        }

        adapter.updateData(allCatatan)
    }

    private fun searchCatatan(query: String) {
        val filtered = if (query.isEmpty()) {
            allCatatan
        } else {
            allCatatan.filter { it.querypencocokan(query) }
        }

        adapter.updateData(filtered)
    }

    private fun toggleFavorit(catatan: Catatan) {
        // Toggle status favorit
        faforitPreferences.toggleCatatanFavorit(catatan.id)
        catatan.isFavorit = faforitPreferences.isCatatanFavorit(catatan.id)

        adapter.notifyDataSetChanged()

        // Mengambil pesan Toast dari string resource
        val message = if (catatan.isFavorit) {
            getString(R.string.toast_added_favorite)
        } else {
            getString(R.string.toast_removed_favorite)
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmation(catatan: Catatan) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_konfirmasi_hapus_catatan, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val tvPesan = dialogView.findViewById<TextView>(R.id.tv_pesan_hapus)
        val btnBatal = dialogView.findViewById<Button>(R.id.btn_batal_hapus)
        val btnOke = dialogView.findViewById<Button>(R.id.btn_oke_hapus)

        // Set pesan konfirmasi hapus permanen menggunakan placeholder %1$s
        tvPesan.text = getString(R.string.msg_confirm_delete_permanent, catatan.judul)

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnOke.setOnClickListener {
            preferences.deleteCatatan(catatan.id)

            if (faforitPreferences.isCatatanFavorit(catatan.id)) {
                faforitPreferences.toggleCatatanFavorit(catatan.id)
            }

            Toast.makeText(requireContext(), getString(R.string.toast_note_deleted), Toast.LENGTH_SHORT).show()
            loadData()
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showArsipDialog(catatan: Catatan) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_konfirmasi_arsip_catatan, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val tvPesan = dialogView.findViewById<TextView>(R.id.tv_pesan_catatan)
        val btnBatal = dialogView.findViewById<Button>(R.id.btn_batal_arsip_catatan)
        val btnOke = dialogView.findViewById<Button>(R.id.btn_oke_arsip_catatan)

        // Set pesan konfirmasi arsip menggunakan placeholder %1$s
        tvPesan.text = getString(R.string.msg_confirm_archive, catatan.judul)

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnOke.setOnClickListener {
            arsipPreferences.arsipkanCatatan(catatan.id)
            Toast.makeText(requireContext(), getString(R.string.toast_note_archived), Toast.LENGTH_SHORT).show()
            loadData()
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}