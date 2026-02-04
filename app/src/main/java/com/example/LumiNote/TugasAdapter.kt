package com.example.LumiNote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TugasAdapter(
    internal val listTugas: MutableList<Tugas>,
    private val onEditClick: (Tugas) -> Unit,
    private val onDeleteClick: (Tugas) -> Unit,
    private val onCheckedChange: (Tugas, Boolean) -> Unit,
    private val onFavoritClick: ((Tugas) -> Unit)? = null,
    private val onArsipClick: ((Tugas) -> Unit)? = null
) : RecyclerView.Adapter<TugasAdapter.TugasViewHolder>() {

    class TugasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.centang)
        val judul: TextView = itemView.findViewById(R.id.tugas_judul)
        val deskripsi: TextView = itemView.findViewById(R.id.tugas_deskripsi)
        val tanggal: TextView = itemView.findViewById(R.id.tgltugas)
        val btnEdit: ImageView = itemView.findViewById(R.id.ic_edit_tugas)
        val btnHapus: ImageView = itemView.findViewById(R.id.ic_hapus_tugas)
        val favoritIcon: ImageView = itemView.findViewById(R.id.ic_favorit_tugas)
        val kartuTugas = itemView.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.kartutugas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TugasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tugas, parent, false)
        return TugasViewHolder(view)
    }

    override fun onBindViewHolder(holder: TugasViewHolder, position: Int) {
        val tugas = listTugas[position]

        holder.judul.text = tugas.judul
        holder.deskripsi.text = tugas.deskripsi
        holder.tanggal.text = tugas.tanggal ?: ""

        // Set warna text berdasarkan status selesai
        val textColor = if (tugas.isSelesai) {
            holder.judul.context.getColor(android.R.color.darker_gray)
        } else {
            holder.judul.context.getColor(R.color.text_primary)
        }

        holder.judul.setTextColor(textColor)
        holder.deskripsi.setTextColor(textColor)
        holder.tanggal.setTextColor(textColor)

        // Set checkbox
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = tugas.isChecked()
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChange(tugas, isChecked)
        }

        // Set icon favorit dengan warna
        try {
            if (tugas.isFavorit) {
                holder.favoritIcon.setImageResource(R.drawable.ic_star_in)
                holder.favoritIcon.setColorFilter(
                    holder.favoritIcon.context.getColor(android.R.color.holo_orange_light)
                )
            } else {
                holder.favoritIcon.setImageResource(R.drawable.ic_star)
                holder.favoritIcon.setColorFilter(
                    holder.favoritIcon.context.getColor(android.R.color.darker_gray)
                )
            }
        } catch (e: Exception) {
            holder.favoritIcon.setImageResource(R.drawable.ic_star)
        }

        // Click listeners
        holder.btnEdit.setOnClickListener {
            onEditClick(tugas)
        }

        holder.btnHapus.setOnClickListener {
            onDeleteClick(tugas)
        }

        holder.favoritIcon.setOnClickListener {
            onFavoritClick?.invoke(tugas)
        }

        // Long press untuk arsip
        holder.kartuTugas.setOnLongClickListener {
            onArsipClick?.invoke(tugas)
            true
        }
    }

    override fun getItemCount(): Int = listTugas.size

    fun updateData(newList: List<Tugas>) {
        listTugas.clear()
        listTugas.addAll(newList)
        notifyDataSetChanged()
    }
}