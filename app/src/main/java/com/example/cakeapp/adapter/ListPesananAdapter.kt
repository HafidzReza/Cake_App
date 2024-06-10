package com.example.cakeapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cakeapp.DetailPesanan
import com.example.cakeapp.R
import com.example.cakeapp.model.PemesanData

class ListPesananAdapter(private val pemesanDataList: List<PemesanData>, private val context: Context) :
    RecyclerView.Adapter<ListPesananAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_pesananuser, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pemesan = pemesanDataList[position]
        holder.nameTextView.text = pemesan.name

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailPesanan::class.java)
            intent.putExtra("userId", pemesan.userId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pemesanDataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.tvNamaPemesan) // Corrected ID
    }
}