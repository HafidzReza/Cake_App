package com.example.cakeapp.model

import java.io.Serializable

data class MenuData( //MENU
    val gambar: String = "",
    val harga: Double = 0.0,
    val makanan: String = "",
    val deskmakanan: String = ""
) : Serializable