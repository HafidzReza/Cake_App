package com.example.cakeapp

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cakeapp.databinding.ActivityBiodataBinding
import com.example.cakeapp.model.PemesanData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class BiodataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBiodataBinding
    private var selectedPhotoUri: Uri? = null
    private val calendar = Calendar.getInstance()
    private var totalHargaSemuanya: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBiodataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        totalHargaSemuanya = intent.getIntExtra("totalHargaSemuanya", 0)

        binding.btnUploadPhoto.setOnClickListener {
            openGallery()
        }

        binding.tvSelectedDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSubmit.setOnClickListener {
            saveBiodata()
        }

        // Add TextWatchers to all input fields
        binding.nameEt.addTextChangedListener(textWatcher)
        binding.noEt.addTextChangedListener(textWatcher)
        binding.tvSelectedDate.addTextChangedListener(textWatcher)
        binding.locationiEt.addTextChangedListener(textWatcher)

        // Disable button initially
        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.background = ColorDrawable(Color.GRAY)
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            checkFieldsForEmptyValues()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun checkFieldsForEmptyValues() {
        val name = binding.nameEt.text.toString().trim()
        val phone = binding.noEt.text.toString().trim()
        val date = binding.tvSelectedDate.text.toString().trim()
        val address = binding.locationiEt.text.toString().trim()

        binding.btnSubmit.isEnabled = name.isNotEmpty() && phone.isNotEmpty() && date.isNotEmpty() && address.isNotEmpty() && selectedPhotoUri != null

        if (binding.btnSubmit.isEnabled) {
            binding.btnSubmit.background = ColorDrawable(Color.parseColor("#FFA500")) // Orange color
        } else {
            binding.btnSubmit.background = ColorDrawable(Color.GRAY)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            selectedPhotoUri = data?.data
            binding.imgView.setImageURI(selectedPhotoUri)
            checkFieldsForEmptyValues()
        }
    }

    private fun saveBiodata() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.nameEt.text.toString().trim()
        val phone = binding.noEt.text.toString().trim()
        val date = binding.tvSelectedDate.text.toString().trim()
        val address = binding.locationiEt.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || date.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPhotoUri == null) {
            Toast.makeText(this, "Please upload a photo", Toast.LENGTH_SHORT).show()
            return
        }

        uploadPhotoToFirebaseStorage(userId, name, phone, date, address)
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                binding.tvSelectedDate.setText(formattedDate)
                checkFieldsForEmptyValues()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun uploadPhotoToFirebaseStorage(userId: String, name: String, phone: String, date: String, address: String) {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$userId/$filename")

        selectedPhotoUri?.let { uri ->
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveBiodataToDatabase(userId, name, phone, date, address, downloadUrl.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload photo: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveBiodataToDatabase(userId: String, name: String, phone: String, date: String, address: String, photoUrl: String) {
        val biodata = PemesanData(
            name = name,
            phone = phone,
            date = date,
            address = address,
            photoUrl = photoUrl,
            userId = userId
        )

        val database = FirebaseDatabase.getInstance()
        val biodataRef = database.reference.child("Biodata").child(userId)

        biodataRef.setValue(biodata)
            .addOnSuccessListener {
                // Update totalHargaSemuanya setelah biodata berhasil disimpan
                biodataRef.child("totalHargaSemuanya").setValue(totalHargaSemuanya)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Biodata and totalHargaSemuanya saved successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to save totalHargaSemuanya: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save biodata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}