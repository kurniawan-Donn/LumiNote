package com.example.luminote

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView // Import CardView baru
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Memastikan layout yang dipanggil adalah layout baru yang menggunakan CardView
        return inflater.inflate(R.layout.dialog_tambah, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // PERBAIKAN: Mengubah tipe dari Button menjadi CardView agar sesuai dengan XML baru
        val btnCatatan: CardView = view.findViewById(R.id.btn_catatan)
        val btnTugas: CardView = view.findViewById(R.id.btn_tugas)

        btnCatatan.setOnClickListener {
            dismiss()
            val intent = Intent(requireContext(), TambahCatatanActivity::class.java)
            startActivity(intent)
        }

        btnTugas.setOnClickListener {
            dismiss()
            val intent = Intent(requireContext(), TambahTugasActivity::class.java)
            startActivity(intent)
        }
    }
}