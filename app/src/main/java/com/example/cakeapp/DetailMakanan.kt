package com.example.cakeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.cakeapp.databinding.ActivityDetailMakananBinding
import com.example.cakeapp.model.MenuData
import com.example.cakeapp.model.PesananData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetailMakanan : AppCompatActivity() {
    private lateinit var binding: ActivityDetailMakananBinding
    private var jumlahPesanan: Int = 0
    private var hargaMakanan: Double = 0.0
    private val database = FirebaseDatabase.getInstance()
    private val pesananRef = database.reference.child("Pesanan")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailMakananBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val menuData = intent.getSerializableExtra("menu") as? MenuData
        menuData?.let { data ->
            Glide.with(this).load(data.gambar).into(binding.imageMakanan)
            binding.namaMakanan.text = data.makanan
            binding.deskMakanan.text = data.deskmakanan
            hargaMakanan = data.harga
            binding.hargaMakanan.text = formatPrice(hargaMakanan)
        }

        binding.btnMinus.setOnClickListener {
            if (jumlahPesanan > 0) {
                jumlahPesanan--
                updateTotalHarga()
            }
        }

        binding.btnPlus.setOnClickListener {
            jumlahPesanan++
            updateTotalHarga()
        }

        binding.btnPesan.setOnClickListener {
            checkAndAddPesanan()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun updateTotalHarga() {
        binding.tvJumlahPesanan.text = jumlahPesanan.toString()
        val totalHarga = jumlahPesanan * hargaMakanan
        binding.totalharga.text = formatPrice(totalHarga)
    }

    private fun formatPrice(price: Double): String {
        return if (price % 1 == 0.0) {
            price.toInt().toString()
        } else {
            price.toString()
        }
    }

    private fun checkAndAddPesanan() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val namaMakanan = binding.namaMakanan.text.toString()
        val jumlahPesananBaru = binding.tvJumlahPesanan.text.toString().toInt()
        val totalHargaBaru = binding.totalharga.text.toString().toDouble()

        if (userId != null) {
            val userPesananRef = pesananRef.child(userId)

            userPesananRef.orderByChild("NamaMakanan").equalTo(namaMakanan)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val pesanan = data.getValue(PesananData::class.java)
                                if (pesanan != null) {
                                    val newJumlahPesanan = pesanan.JumlahPesanan + jumlahPesananBaru
                                    val newTotalHarga = pesanan.TotalHarga + totalHargaBaru

                                    val updatedPesanan: MutableMap<String, Any?> = mutableMapOf( // Changed HashMap to mutableMapOf
                                        "JumlahPesanan" to newJumlahPesanan,
                                        "TotalHarga" to newTotalHarga
                                    )

                                    userPesananRef.child(data.key!!).updateChildren(updatedPesanan)
                                        .addOnSuccessListener {
                                            resetJumlahPesanan()
                                            val intent = Intent(this@DetailMakanan, LaporanActivity::class.java)
                                            startActivity(intent)
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this@DetailMakanan, "Gagal menambahkan pesanan: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    return
                                }

                            }
                        } else {
                            addNewPesanan(userId, namaMakanan, jumlahPesananBaru, totalHargaBaru)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@DetailMakanan, "Gagal menambahkan pesanan: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun addNewPesanan(userId: String, namaMakanan: String, jumlahPesanan: Int, totalHarga: Double) {
        val pesananId = pesananRef.child(userId).push().key

        if (pesananId != null) {
            val pesanan = mutableMapOf<String, Any>( // Changed HashMap to mutableMapOf
                "NamaMakanan" to namaMakanan,
                "JumlahPesanan" to jumlahPesanan,
                "TotalHarga" to totalHarga
            )

            pesananRef.child(userId).child(pesananId).setValue(pesanan)
                .addOnSuccessListener {
                    resetJumlahPesanan()
                    val intent = Intent(this, LaporanActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menambahkan pesanan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Gagal membuat pesanan ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetJumlahPesanan() {
        jumlahPesanan = 0
        updateTotalHarga()
    }
}