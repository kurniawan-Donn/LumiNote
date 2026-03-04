package com.example.luminote

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PilihTipeBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_tambah, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnCatatan: Button = view.findViewById(R.id.btn_catatan)
        val btnTugas: Button = view.findViewById(R.id.btn_tugas)

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