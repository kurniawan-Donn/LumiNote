package com.example.luminote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FaforitAdapter(
    private var faforitList: List<FaforitItem>,
    private val onFavoritClick: (FaforitItem) -> Unit // Callback untuk toggle favorit
) : RecyclerView.Adapter<FaforitAdapter.FaforitViewHolder>() {

    class FaforitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ic_hapus_Faforit: ImageButton = view.findViewById(R.id.ic_hapus_Faforit)
        val tvTipe: TextView = view.findViewById(R.id.tvTipe)
        val tvJudul: TextView = view.findViewById(R.id.tvJudul)
        val tvDeskripsi: TextView = view.findViewById(R.id.tvDeskripsi)
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaforitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faforit, parent, false)
        return FaforitViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaforitViewHolder, position: Int) {
        val item = faforitList[position]

        // Set tipe (Catatan/Tugas)
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

        // Icon bintang selalu penuh karena ini halaman favorit
        holder.ic_hapus_Faforit.setImageResource(R.drawable.ic_delete)

        // Toggle favorit saat diklik
        holder.ic_hapus_Faforit.setOnClickListener {
            onFavoritClick(item)
        }
    }

    override fun getItemCount(): Int = faforitList.size

    fun updateData(newList: List<FaforitItem>) {
        faforitList = newList
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        // Implementasi filter jika perlu
        notifyDataSetChanged()
    }
}