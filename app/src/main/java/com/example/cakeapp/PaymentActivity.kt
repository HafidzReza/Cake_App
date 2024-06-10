package com.example.cakeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.cakeapp.databinding.ActivityPaymentBinding
import com.example.cakeapp.model.PesananData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var totalHargaSemuanya: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        database = FirebaseDatabase.getInstance().getReference("Pesanan").child(userId!!)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalHargaSemuanya = 0
                for (dataSnapshot in snapshot.children) {
                    val pesanan = dataSnapshot.getValue(PesananData::class.java)
                    if (pesanan != null) {
                        totalHargaSemuanya += pesanan.TotalHarga
                    }
                }
                binding.tvTotalHargaSemuanya.text = "Rp. $totalHargaSemuanya"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PaymentActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })

        binding.butpayment.setOnClickListener {
            val intent = Intent(this, BiodataActivity::class.java)
            intent.putExtra("totalHargaSemuanya", totalHargaSemuanya)
            startActivity(intent)
        }
    }
}