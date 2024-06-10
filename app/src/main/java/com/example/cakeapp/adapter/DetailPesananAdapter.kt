package com.example.cakeapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cakeapp.R
import com.example.cakeapp.model.PesananData

class DetailPesananAdapter (private val pesananList: List<PesananData>) :
    RecyclerView.Adapter<DetailPesananAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_datapesanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pesanan = pesananList[position]
        holder.tvNamaMakanan.text = pesanan.NamaMakanan
        holder.tvJumlahPesanan.text = pesanan.JumlahPesanan.toString()
    }

    override fun getItemCount(): Int {
        return pesananList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaMakanan: TextView = itemView.findViewById(R.id.tvNamaMakanan)
        val tvJumlahPesanan: TextView = itemView.findViewById(R.id.tvJumlahPesananUser)
    }
}