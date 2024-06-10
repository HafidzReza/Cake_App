package com.example.cakeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.cakeapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        // Inisialisasi database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Mendapatkan UID pengguna yang sedang masuk
        val currentUserUid = auth.currentUser?.uid

        // Menampilkan nama dan email dari database
        getUserData(currentUserUid)

        binding.btnPesanan.setOnClickListener {
            startActivity(Intent(this, PesananActivity::class.java))
        }

        binding.btnKeluar.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    // Fungsi untuk mendapatkan data pengguna dari Firebase Database
    private fun getUserData(uid: String?) {
        uid?.let {
            val userRef = database.getReference("users").child(uid)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val username = snapshot.child("username").getValue(String::class.java)
                        val email = snapshot.child("email").getValue(String::class.java)

                        // Menampilkan nama pengguna dan email
                        binding.txtUsername.text = username
                        binding.txtEmail.text = email
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    // Misalnya, tampilkan pesan kesalahan
                    binding.txtUsername.text = "Failed to load user data"
                    binding.txtEmail.text = ""
                }
            })
        } ?: run {
            // UID is null, handle this case
            binding.txtUsername.text = "User not logged in"
            binding.txtEmail.text = ""
        }
    }

    // Fungsi untuk menampilkan dialog konfirmasi logout
    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Logout")
        builder.setMessage("Apakah Anda yakin ingin keluar?")
        builder.setPositiveButton("Ya") { dialog, which ->
            signOut()
        }
        builder.setNegativeButton("Tidak") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    // Fungsi untuk logout
    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Mengakhiri aktivitas saat ini
    }
}