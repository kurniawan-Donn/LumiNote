package com.example.luminote

import java.util.Calendar

data class KalenderDayItem(
    val date: Calendar? = null,
    val hasCatatan: Boolean = false,
    val hasTugas: Boolean = false,
    val isToday: Boolean = false,
    var isSelected: Boolean = false,
    val isCurrentMonth: Boolean = true
)