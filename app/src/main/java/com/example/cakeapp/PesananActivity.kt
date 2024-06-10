package com.example.cakeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cakeapp.adapter.ListPesananAdapter
import com.example.cakeapp.databinding.ActivityPesananBinding
import com.example.cakeapp.model.PemesanData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PesananActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPesananBinding
    private lateinit var adapter: ListPesananAdapter
    private lateinit var database: DatabaseReference
    private val pemesanDataList = mutableListOf<PemesanData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPesananBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.toolbar.setNavigationOnClickListener { onBackPressed()}

        setupRecyclerView()
        fetchPesananData()
    }

    private fun setupRecyclerView() {
        adapter = ListPesananAdapter(pemesanDataList, this)
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = adapter
    }

    private fun fetchPesananData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            // Handle user not logged in case
            return
        }

        database = FirebaseDatabase.getInstance().getReference("Biodata")
        database.orderByChild("userId").equalTo(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pemesanDataList.clear()
                for (dataSnapshot in snapshot.children) {
                    val pemesan = dataSnapshot.getValue(PemesanData::class.java)
                    if (pemesan != null) {
                        pemesanDataList.add(pemesan)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })
    }
}