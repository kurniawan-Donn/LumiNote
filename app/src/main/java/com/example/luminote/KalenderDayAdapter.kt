package com.example.luminote

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class KalenderDayAdapter(
    private val context: Context,
    private var days: List<KalenderDayItem>,
    private val onDateClick: (KalenderDayItem) -> Unit
) : RecyclerView.Adapter<KalenderDayAdapter.DayViewHolder>() {

    private var selectedPosition = -1

    // Warna dot — bisa dipindah ke colors.xml
    private val colorCatatan = ContextCompat.getColor(context, R.color.dot_catatan)
    private val colorTugas   = ContextCompat.getColor(context, R.color.dot_tugas)

    inner class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tv_kal_date)
        val layoutDots: LinearLayout = view.findViewById(R.id.layout_kal_dots)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_kalender_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val item = days[position]

        // Kosong — sel pengisi
        if (item.date == null) {
            holder.tvDate.text = ""
            holder.tvDate.background = null
            holder.layoutDots.removeAllViews()
            holder.itemView.isClickable = false
            return
        }

        holder.tvDate.text = item.date.get(Calendar.DAY_OF_MONTH).toString()
        holder.itemView.isClickable = true

        // Warna teks & background lingkaran tanggal
        when {
            item.isSelected -> {
                holder.tvDate.setBackgroundResource(R.drawable.bg_kal_circle_selected)
                holder.tvDate.setTextColor(Color.WHITE)
            }
            item.isToday -> {
                holder.tvDate.setBackgroundResource(R.drawable.bg_kal_circle_today)
                holder.tvDate.setTextColor(ContextCompat.getColor(context, R.color.color_primary))
            }
            else -> {
                holder.tvDate.background = null
                holder.tvDate.setTextColor(
                    if (item.isCurrentMonth)
                        ContextCompat.getColor(context, R.color.text_primary)
                    else
                        ContextCompat.getColor(context, R.color.text_secondary)
                )
            }
        }

        // Dot indikator di bawah tanggal
        holder.layoutDots.removeAllViews()
        if (item.hasCatatan) holder.layoutDots.addView(createDot(colorCatatan))
        if (item.hasTugas)   holder.layoutDots.addView(createDot(colorTugas))

        // Klik tanggal
        holder.itemView.setOnClickListener {
            val prev = selectedPosition
            selectedPosition = holder.adapterPosition
            if (prev != -1) notifyItemChanged(prev)
            notifyItemChanged(selectedPosition)
            onDateClick(item)
        }
    }

    private fun createDot(color: Int): View {
        val dm = context.resources.displayMetrics
        val sizePx   = (6 * dm.density).toInt()
        val marginPx = (2 * dm.density).toInt()
        val dot = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                setMargins(marginPx, 0, marginPx, 0)
            }
            setBackgroundResource(R.drawable.bg_kal_dot)
            background.setTint(color)
        }
        return dot
    }

    fun updateData(newDays: List<KalenderDayItem>, selectedPos: Int = -1) {
        days = newDays
        selectedPosition = selectedPos
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = days.size
}