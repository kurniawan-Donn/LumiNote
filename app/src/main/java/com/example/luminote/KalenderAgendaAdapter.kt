package com.example.luminote

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class KalenderAgendaAdapter(
    private val context: Context,
    private var items: List<KalenderAgendaItem>,
    private val onItemClick: (KalenderAgendaItem) -> Unit
) : RecyclerView.Adapter<KalenderAgendaAdapter.AgendaViewHolder>() {

    private val colorCatatan = ContextCompat.getColor(context, R.color.dot_catatan)
    private val colorTugas   = ContextCompat.getColor(context, R.color.dot_tugas)

    inner class AgendaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewIndicator: View      = view.findViewById(R.id.view_kal_indicator)
        val tvBadge: TextView        = view.findViewById(R.id.tv_kal_badge)
        val tvJudul: TextView        = view.findViewById(R.id.tv_kal_judul)
        val tvDeskripsi: TextView    = view.findViewById(R.id.tv_kal_deskripsi)
        val tvWaktu: TextView        = view.findViewById(R.id.tv_kal_waktu)
        val tvStatus: TextView       = view.findViewById(R.id.tv_kal_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgendaViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_kalender_agenda, parent, false)
        return AgendaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AgendaViewHolder, position: Int) {
        val item = items[position]

        when (item) {
            is KalenderAgendaItem.CatatanItem -> {
                val catatan = item.catatan
                holder.viewIndicator.background.setTint(colorCatatan)
                // Menggunakan string resource
                holder.tvBadge.text = "📝 " + context.getString(R.string.label_catatan)
                holder.tvJudul.text = catatan.judul
                holder.tvDeskripsi.text = catatan.deskripsi.ifBlank { "-" }
                holder.tvWaktu.text = catatan.waktu ?: ""
                holder.tvStatus.visibility = View.GONE
            }
            is KalenderAgendaItem.TugasItem -> {
                val tugas = item.tugas
                holder.viewIndicator.background.setTint(colorTugas)
                // Menggunakan string resource
                holder.tvBadge.text = "✅ " + context.getString(R.string.label_tugas)
                holder.tvJudul.text = tugas.judul
                holder.tvDeskripsi.text = tugas.deskripsi.ifBlank { "-" }

                val waktu = tugas.tanggal?.split(" ")?.getOrNull(1) ?: ""
                holder.tvWaktu.text = waktu
                holder.tvStatus.visibility = View.VISIBLE

                // Logika status selesai/belum dalam 3 bahasa
                if (tugas.isSelesai) {
                    holder.tvStatus.text = context.getString(R.string.status_selesai)
                    // Gunakan warna hijau sukses (biasanya ditambahkan di colors.xml) atau tetap parse jika mendesak
                    holder.tvStatus.setTextColor(Color.parseColor("#27AE60"))
                } else {
                    holder.tvStatus.text = context.getString(R.string.status_belum_selesai)
                    // Gunakan warna dari colors.xml Anda: @color/color_danger
                    holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.color_danger))
                }
            }
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    fun updateData(newItems: List<KalenderAgendaItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}