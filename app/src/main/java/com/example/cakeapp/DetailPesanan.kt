package com.example.cakeapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cakeapp.adapter.DetailPesananAdapter
import com.example.cakeapp.databinding.ActivityDetailPesananBinding
import com.example.cakeapp.model.PesananData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetailPesanan : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPesananBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPesananBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        binding.toolbar.setNavigationOnClickListener { onBackPressed()}

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        // Inisialisasi RecyclerView
        val recyclerView = binding.recyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Mendapatkan referensi database
        databaseReference = FirebaseDatabase.getInstance().reference

        if (userId != null) {
            // Mendapatkan data biodata
            databaseReference.child("Biodata").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val nama = snapshot.child("name").getValue(String::class.java)
                            val nomor = snapshot.child("phone").getValue(String::class.java)
                            val alamat = snapshot.child("address").getValue(String::class.java)
                            val tanggal = snapshot.child("date").getValue(String::class.java)

                            binding.tvNamaPemesan.text = nama
                            binding.tvNomor.text = nomor
                            binding.tvAlamat.text = alamat
                            binding.tvTanggal.text = tanggal

                            binding.dataId.visibility = View.VISIBLE
                            binding.notFound.visibility = View.GONE
                        } else {
                            binding.dataId.visibility = View.GONE
                            binding.notFound.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@DetailPesanan,
                            "Gagal mengambil data biodata: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

            // Mendapatkan data pesanan
            databaseReference.child("Pesanan").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val pesananList = mutableListOf<PesananData>()
                        for (pesananSnapshot in snapshot.children) {
                            val pesanan = pesananSnapshot.getValue(PesananData::class.java)
                            if (pesanan != null) {
                                pesananList.add(pesanan)
                            }
                        }
                        if (pesananList.isNotEmpty()) {
                            // Mengatur adapter untuk RecyclerView
                            val adapter = DetailPesananAdapter(pesananList)
                            recyclerView.adapter = adapter

                            binding.dataId.visibility = View.VISIBLE
                            binding.notFound.visibility = View.GONE
                        } else {
                            binding.dataId.visibility = View.GONE
                            binding.notFound.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@DetailPesanan,
                            "Gagal mengambil data pesanan: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }

        binding.btnBatal.setOnClickListener {
            if (userId != null) {
                // Menghapus data biodata dan pesanan
                databaseReference.child("Biodata").child(userId).removeValue()
                databaseReference.child("Pesanan").child(userId).removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@DetailPesanan,
                                "Pesanan berhasil dibatalkan",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.dataId.visibility = View.GONE
                            binding.notFound.visibility = View.VISIBLE
                        } else {
                            Toast.makeText(
                                this@DetailPesanan,
                                "Gagal membatalkan pesanan: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }
}