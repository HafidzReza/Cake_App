package com.example.cakeapp.model

import java.io.Serializable

class PemesanData ( //BIODATA
    val address: String? = "",
    val date: String? = "",
    val name: String? = "",
    val phone: String? = "",
    val photoUrl: String? = "",
    val userId: String? = ""
) : Serializable