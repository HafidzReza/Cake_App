package com.example.cakeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cakeapp.model.PesananData
import com.google.firebase.database.DatabaseReference

class PesananAdapter(
    private val pesananList: List<PesananData>,
    private val pesananRef: DatabaseReference
) : RecyclerView.Adapter<PesananAdapter.PesananViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pesanan, parent, false)
        return PesananViewHolder(view)
    }

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        val pesanan = pesananList[position]
        holder.tvNamaMakanan.text = pesanan.NamaMakanan
        holder.tvJumlahPesanan.text = "Jumlah: ${pesanan.JumlahPesanan}"
        holder.tvTotalHarga.text = "Harga: ${pesanan.TotalHarga}"
        holder.btnBatal.setOnClickListener {
            pesananRef.child(pesanan.id).removeValue()
        }
    }

    override fun getItemCount() = pesananList.size

    class PesananViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaMakanan: TextView = itemView.findViewById(R.id.tvNamaMakanan)
        val tvJumlahPesanan: TextView = itemView.findViewById(R.id.tvJumlahPesanan)
        val tvTotalHarga: TextView = itemView.findViewById(R.id.tvTotalHarga)
        val btnBatal: Button = itemView.findViewById(R.id.btnBatal)
    }
}