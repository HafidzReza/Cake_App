package com.example.cakeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cakeapp.databinding.ActivityLaporanBinding
import com.example.cakeapp.model.PesananData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LaporanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanBinding
    private lateinit var pesananRef: DatabaseReference
    private lateinit var pesananList: MutableList<PesananData>
    private lateinit var adapter: PesananAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        pesananRef = FirebaseDatabase.getInstance().reference.child("Pesanan").child(userId ?: "")

        pesananList = mutableListOf()
        adapter = PesananAdapter(pesananList, pesananRef)

        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = adapter

        pesananRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pesananList.clear()
                if (snapshot.exists()) {
                    for (pesananSnapshot in snapshot.children) {
                        val pesanan = pesananSnapshot.getValue(PesananData::class.java)
                        pesanan?.let {
                            it.id = pesananSnapshot.key ?: ""
                            pesananList.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        binding.butlaporan.setOnClickListener {
            val totalHarga = pesananList.sumOf { it.TotalHarga }
            val intent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("totalHarga", totalHarga)
            }
            startActivity(intent)
        }
    }
}