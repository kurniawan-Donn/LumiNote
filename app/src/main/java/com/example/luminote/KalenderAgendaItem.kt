package com.example.luminote

sealed class KalenderAgendaItem {
    data class CatatanItem(val catatan: Catatan) : KalenderAgendaItem()
    data class TugasItem(val tugas: Tugas) : KalenderAgendaItem()
}