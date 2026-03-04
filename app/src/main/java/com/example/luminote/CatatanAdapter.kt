package com.example.luminote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class CatatanAdapter(
    private var catatanList: List<Catatan>,
    private val onItemClick: (Catatan) -> Unit,
    private val onEditClick: (Catatan) -> Unit,
    private val onDeleteClick: (Catatan) -> Unit,
    private val onFavoritClick: ((Catatan) -> Unit)? = null,
    private val onArsipClick: ((Catatan) -> Unit)? = null
) : RecyclerView.Adapter<CatatanAdapter.CatatanViewHolder>() {

    class CatatanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val kartuCatatan: ConstraintLayout = view.findViewById(R.id.kartucatatan)
        val judulTextView: TextView = view.findViewById(R.id.catatan_judul)
        val deskripsiTextView: TextView = view.findViewById(R.id.catatan_deskripsi)
        val tanggalTextView: TextView = view.findViewById(R.id.tglcatatan)
        val editIcon: ImageView = view.findViewById(R.id.ic_edit_catatan)
        val deleteIcon: ImageView = view.findViewById(R.id.ic_hapus_catatan)
        val favoritIcon: ImageView = view.findViewById(R.id.ic_favorit_catatan)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int): CatatanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_catatan, parent, false)
        return CatatanViewHolder(view)
    }

    override fun onBindViewHolder(holder: CatatanViewHolder, position: Int) {
        val catatan = catatanList[position]

        val currentCalendar = Calendar.getInstance()

        val defaultTanggal = String.format("%02d/%02d/%04d",
            currentCalendar.get(Calendar.DAY_OF_MONTH),
            currentCalendar.get(Calendar.MONTH) + 1,
            currentCalendar.get(Calendar.YEAR)
        )

        val defaultWaktu = String.format("%02d:%02d",
            currentCalendar.get(Calendar.HOUR_OF_DAY),
            currentCalendar.get(Calendar.MINUTE)
        )

        holder.judulTextView.text = catatan.judul
        val context = holder.itemView.context
        holder.deskripsiTextView.text = if (catatan.deskripsi.isNotEmpty()) {
            "• ${catatan.deskripsi}"
        } else {
            context.getString(R.string.no_description)
        }

        val tanggal = if (!catatan.tanggal.isNullOrEmpty()) catatan.tanggal else defaultTanggal
        val waktu = if (!catatan.waktu.isNullOrEmpty()) catatan.waktu else defaultWaktu
        holder.tanggalTextView.text = "$tanggal $waktu"

        if (catatan.isFavorit) {
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

        holder.kartuCatatan.setOnClickListener {
            onItemClick(catatan)
        }

        holder.kartuCatatan.setOnLongClickListener {
            onArsipClick?.invoke(catatan)
            true
        }

        holder.editIcon.setOnClickListener {
            onEditClick(catatan)
        }

        holder.deleteIcon.setOnClickListener {
            onDeleteClick(catatan)
        }

        holder.favoritIcon.setOnClickListener {
            onFavoritClick?.invoke(catatan)
        }
    }

    override fun getItemCount(): Int = catatanList.size

    fun updateData(newList: List<Catatan>) {
        catatanList = newList
        notifyDataSetChanged()
    }
}