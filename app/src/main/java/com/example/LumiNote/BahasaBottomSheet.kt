package com.example.LumiNote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BahasaBottomSheet : BottomSheetDialogFragment() {

    private lateinit var layoutIndonesia: LinearLayout
    private lateinit var layoutInggris: LinearLayout
    private lateinit var layoutJawa: LinearLayout

    private lateinit var checkIndonesia: ImageView
    private lateinit var checkInggris: ImageView
    private lateinit var checkJawa: ImageView

    private var currentLanguage: String = LanguageHelper.LANGUAGE_INDONESIA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = LanguageHelper.getLanguage(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_bahasa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
        updateCheckmarks()
    }

    private fun initViews(view: View) {
        // Layouts
        layoutIndonesia = view.findViewById(R.id.layoutIndonesia)
        layoutInggris = view.findViewById(R.id.layoutInggris)
        layoutJawa = view.findViewById(R.id.layoutJawa)

        // Checkmarks
        checkIndonesia = view.findViewById(R.id.checkIndonesia)
        checkInggris = view.findViewById(R.id.checkInggris)
        checkJawa = view.findViewById(R.id.checkJawa)
    }

    private fun setupListeners() {
        layoutIndonesia.setOnClickListener {
            selectLanguage(LanguageHelper.LANGUAGE_INDONESIA)
        }

        layoutInggris.setOnClickListener {
            selectLanguage(LanguageHelper.LANGUAGE_ENGLISH)
        }

        layoutJawa.setOnClickListener {
            selectLanguage(LanguageHelper.LANGUAGE_JAVANESE)
        }
    }

    private fun selectLanguage(languageCode: String) {
        // Jika bahasa sama, tidak perlu update
        if (currentLanguage == languageCode) {
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_language_already_selected),
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
            return
        }

        // Update bahasa yang dipilih
        currentLanguage = languageCode

        // Simpan ke SharedPreferences
        LanguageHelper.setLanguage(requireContext(), languageCode)

        // Update checkmark secara visual sebelum tutup
        updateCheckmarks()

        // PERBAIKAN: Melewatkan Context ke getDisplayNameByCode
        val languageName = LanguageHelper.getDisplayNameByCode(requireContext(), languageCode)

        // Tampilkan pesan dengan nama bahasa yang dinamis
        Toast.makeText(
            requireContext(),
            getString(R.string.toast_language_changed, languageName),
            Toast.LENGTH_SHORT
        ).show()

        // Tutup bottom sheet
        dismiss()

        // Recreate activity untuk apply bahasa secara menyeluruh
        activity?.recreate()
    }

    private fun updateCheckmarks() {
        // Sembunyikan semua checkmark
        checkIndonesia.visibility = View.GONE
        checkInggris.visibility = View.GONE
        checkJawa.visibility = View.GONE

        // Tampilkan checkmark sesuai bahasa yang dipilih
        when (currentLanguage) {
            LanguageHelper.LANGUAGE_INDONESIA -> {
                checkIndonesia.visibility = View.VISIBLE
            }
            LanguageHelper.LANGUAGE_ENGLISH -> {
                checkInggris.visibility = View.VISIBLE
            }
            LanguageHelper.LANGUAGE_JAVANESE -> {
                checkJawa.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        const val TAG = "BahasaBottomSheet"

        fun newInstance(): BahasaBottomSheet {
            return BahasaBottomSheet()
        }
    }
}