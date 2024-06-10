package com.example.cakeapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.cakeapp.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable

@SuppressLint("CheckResult")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // inisialisasi firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // validasi username
        val usernameStream = RxTextView.textChanges(binding.edtEmail)
            .skipInitialValue()
            .map { username ->
                username.isEmpty()
            }
        usernameStream.subscribe {
            showTextMinimalAlert(it, "Username")
        }

        // validasi password
        val passwordStream = RxTextView.textChanges(binding.edtPassword)
            .skipInitialValue()
            .map { password ->
                password.isEmpty()
            }
        passwordStream.subscribe {
            showTextMinimalAlert(it, "Password")
        }

        // Button Enable
        val invalidFieldStream = Observable.combineLatest(
            usernameStream,
            passwordStream,
            { usernameInvalid: Boolean, passwordInvalid: Boolean ->
                !usernameInvalid && !passwordInvalid
            })
        invalidFieldStream.subscribe { isValid ->
            if (isValid) {
                binding.LBtn1.isEnabled = true
                binding.LBtn1.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primarycolor)
            } else {
                binding.LBtn1.isEnabled = false
                binding.LBtn1.backgroundTintList = ContextCompat.getColorStateList(this, R.color.secondarycolor)
            }
        }

        // set button
        binding.LBtn1.setOnClickListener {
            val emailOrUsername = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            loginUser(emailOrUsername, password)
        }
        binding.txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        if (text == "Email/Username")
            binding.edtEmail.error = if (isNotValid) "Tidak Boleh Kosong!" else null
        else if (text == "password")
            binding.edtPassword.error = if (isNotValid) "Tidak Boleh Kosong!" else null
    }

    private fun loginUser(emailOrUsername: String, pass: String) {
        // Cek apakah input adalah email atau username
        val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()

        if (isEmail) {
            // Jika input adalah email, gunakan signInWithEmailAndPassword
            auth.signInWithEmailAndPassword(emailOrUsername, pass)
                .addOnCompleteListener(this) { login ->
                    handleLoginResult(login)
                }
        } else {
            // Jika input adalah username, tambahkan metode untuk mendapatkan email dari username
            getUserEmailFromUsername(emailOrUsername) { email ->
                if (email != null) {
                    auth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this) { login ->
                            handleLoginResult(login)
                        }
                } else {
                    // Handle jika username tidak ditemukan
                    Toast.makeText(this, "Username tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Menangani hasil dari proses login
    private fun handleLoginResult(loginTask: Task<AuthResult>) {
        if (loginTask.isSuccessful) {
            Intent(this, MenuActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
                Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Dapatkan pesan kesalahan spesifik untuk menampilkan kesalahan yang tepat
            val errorMessage = when (loginTask.exception) {
                is FirebaseAuthInvalidUserException -> "Akun tidak ditemukan"
                is FirebaseAuthInvalidCredentialsException -> "Username atau password tidak valid"
                else -> "Gagal login: ${loginTask.exception?.message}"
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUserEmailFromUsername(username: String, onComplete: (String?) -> Unit) {
        // Ambil referensi database
        val usersRef = database.getReference("users")

        // mencari pengguna berdasarkan username
        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Jika username ditemukan, ambil email
                    val user = snapshot.children.first()
                    val email = user.child("email").getValue(String::class.java)
                    onComplete(email)
                } else {
                    onComplete(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(null)
            }
        })
    }
}