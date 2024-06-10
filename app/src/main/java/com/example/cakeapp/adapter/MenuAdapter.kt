package com.example.cakein.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cakeapp.R
import com.example.cakeapp.model.MenuData

class MenuAdapter(
    private val context: Context,
    private var listMenu: List<MenuData>,
    private val itemClickListener: (MenuData) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    fun setData(newData: List<MenuData>) {
        listMenu = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.list_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuData = listMenu[position]
        Glide.with(context).load(menuData.gambar).into(holder.img)
        holder.makanan.text = menuData.makanan
        holder.harga.text = formatPrice(menuData.harga)

        holder.itemView.setOnClickListener {
            itemClickListener.invoke(menuData)
        }
    }

    override fun getItemCount(): Int {
        return listMenu.size
    }

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.imageMakanan)
        val makanan: TextView = itemView.findViewById(R.id.tvNamaMakanan)
        val harga: TextView = itemView.findViewById(R.id.tvHargaMakanan)
    }

    private fun formatPrice(price: Double): String {
        return if (price % 1 == 0.0) {
            price.toInt().toString()
        } else {
            price.toString()
        }
    }
}