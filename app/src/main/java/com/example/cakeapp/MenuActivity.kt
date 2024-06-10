package com.example.cakeapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cakeapp.databinding.ActivityMenuBinding
import com.example.cakeapp.model.MenuData
import com.example.cakein.adapter.MenuAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    private var menuList: MutableList<MenuData> = mutableListOf()

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    //Deklarasi variabel nama pengguna
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // get database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // set button profile
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        //dapatkan instance pengguna yang saat ini login
        val currentUser: FirebaseUser? = auth.currentUser

        //cek apakah ada pengguna login
        currentUser?.let { user ->
            val currentUserUid = user.uid

            val userRef = database.getReference("users").child(currentUserUid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        //ambil data username
                        username = snapshot.child("username").getValue(String::class.java) ?: ""

                        // tampilkan nama
                        binding.tvNameWelcome.text = "$username"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error: ${error.message}")
                }
            })
        }

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerview.layoutManager = layoutManager

        val adapter = MenuAdapter(this, emptyList()) { menuData ->
            val intent = Intent(this, DetailMakanan::class.java)
            intent.putExtra("menu", menuData)
            startActivity(intent)
        }

        // Mengatur adapter pada RecyclerView
        binding.recyclerview.adapter = adapter

        // perubahan data pada Firebase Database
        val menuRef = database.getReference("menu")
        menuRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val menuDataList = mutableListOf<MenuData>()

                for (dataSnapshot in snapshot.children) {
                    val gambar = dataSnapshot.child("gambar").getValue(String::class.java) ?: ""
                    val harga = when (val hargaValue = dataSnapshot.child("harga").value) {
                        is Long -> hargaValue.toDouble()
                        is Double -> hargaValue
                        is String -> hargaValue.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                    val makanan = dataSnapshot.child("makanan").getValue(String::class.java) ?: ""
                    val deskmakanan = dataSnapshot.child("deskmakanan").getValue(String::class.java) ?: ""

                    val menuData = MenuData(gambar, harga, makanan, deskmakanan)
                    menuDataList.add(menuData)
                }

                menuList = menuDataList
                adapter.setData(menuDataList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("FirebaseError", "Error: ${error.message}")
            }
        })

        // Set searchview
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchMenuData(it) }
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
            false
        }

    }

    // Fungsi untuk melakukan pencarian data wisata berdasarkan nama
    private fun searchMenuData(query: String) {
        val searchResult = menuList.filter {
            it.makanan.contains(query, ignoreCase = true)
        }
        (binding.recyclerview.adapter as MenuAdapter).setData(searchResult)
    }
}