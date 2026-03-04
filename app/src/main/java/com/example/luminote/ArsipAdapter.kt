package com.example.luminote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArsipAdapter(
    private var arsipList: List<ArsipItem>,
    private val onPulihkanClick: (ArsipItem) -> Unit,
    private val onHapusClick: (ArsipItem) -> Unit
) : RecyclerView.Adapter<ArsipAdapter.ArsipViewHolder>() {

    class ArsipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTipe: TextView = view.findViewById(R.id.tvTipe)
        val tvJudul: TextView = view.findViewById(R.id.tvJudul)
        val tvDeskripsi: TextView = view.findViewById(R.id.tvDeskripsi)
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggal)
        val btnPulihkan: Button = view.findViewById(R.id.btnPulihkan)
        val btnHapus: Button = view.findViewById(R.id.btnHapus)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int): ArsipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_arsip, parent, false)
        return ArsipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArsipViewHolder, position: Int) {
        val item = arsipList[position]

        // Set tipe dengan format [Catatan] atau [Tugas]
        holder.tvTipe.text = "[${item.tipe}]"

        // Set judul
        holder.tvJudul.text = item.judul

        // Set deskripsi
        holder.tvDeskripsi.text = if (item.deskripsi.isNotEmpty()) {
            item.deskripsi
        } else {
            holder.itemView.context.getString(R.string.no_description)
        }

        // Set tanggal
        holder.tvTanggal.text = if (item.tanggal.isNotEmpty()) {
            item.tanggal
        } else {
            "-"
        }

        // Button Pulihkan
        holder.btnPulihkan.setOnClickListener {
            onPulihkanClick(item)
        }

        // Button Hapus
        holder.btnHapus.setOnClickListener {
            onHapusClick(item)
        }
    }

    override fun getItemCount(): Int = arsipList.size

    fun updateData(newList: List<ArsipItem>) {
        arsipList = newList
        notifyDataSetChanged()
    }
}