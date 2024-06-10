package com.example.cakeapp.model

import java.io.Serializable

class PesananData( //PESANAN
    val NamaMakanan: String? = "",
    val TotalHarga: Int = 0,
    val JumlahPesanan: Int = 0,
    var id: String = ""
) : Serializable