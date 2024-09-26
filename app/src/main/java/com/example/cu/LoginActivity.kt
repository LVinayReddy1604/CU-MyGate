package com.example.cu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    // Database
    private var databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://cu-mygate-default-rtdb.firebaseio.com/")

    // Shared preference keys
    private val keyNAME = "MYGATE_CRED"
    private val keyUSERNAME = "user"
    private val keyTYPE = "type"

    // Shared preferences instance
    private lateinit var sharedPreferences: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences(keyNAME, MODE_PRIVATE)

        // If shared preference data is already available then login directly
        val sharedType = sharedPreferences.getString(keyTYPE, null)
        val sharedUser = sharedPreferences.getString(keyUSERNAME, null)
        if (sharedType != null) {
            when (sharedType) {
                "teacher" -> {
                    // Teacher dashboard
                    val intent = Intent(this@LoginActivity, TeacherDashboardActivity::class.java)
                    intent.putExtra("user", sharedUser)
                    startActivity(intent)
                }

                "chief" -> {
                    // Chief security dashboard
                    val intent = Intent(this@LoginActivity, ChiefSecurityDashboardActivity::class.java)
                    intent.putExtra("user", sharedUser)
                    startActivity(intent)
                }

                "guard" -> {
                    // Guard dashboard
                    val intent = Intent(this@LoginActivity, SecurityDashboardActivity::class.java)
                    intent.putExtra("user", sharedUser)
                    startActivity(intent)
                }

                else -> {
                    // Unknown user
                    Toast.makeText(this@LoginActivity, "Invalid User-Type", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Initialize views
        emailEditText = findViewById(R.id.et_email)
        passwordEditText = findViewById(R.id.et_password)
        loginButton = findViewById(R.id.btn_login)

        // Set click listener for login button
        loginButton.setOnClickListener {
            val user = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Validate email and password fields
            if (user.isEmpty()) {
                emailEditText.error = "ID is required"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Password is required"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            // Perform login using AuthenticationManager
            databaseReference.child("login").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if username exists in DB
                    try{
                    if (snapshot.hasChild(user)) {
                        val dbpass = snapshot.child(user).child("pass").value
                        if (dbpass.toString() == password.toString()) {
                            // If correct password
                            val editor = sharedPreferences.edit()
                            editor.putString(keyUSERNAME, user)
                            val type = snapshot.child(user).child("type").value
                            when (type) {
                                "teacher" -> {
                                    // Teacher dashboard
                                    val intent = Intent(this@LoginActivity, TeacherDashboardActivity::class.java)
                                    intent.putExtra("user", user)
                                    editor.putString(keyTYPE, "teacher")
                                    editor.apply()
                                    startActivity(intent)
                                }
                                "chief" -> {
                                    // Chief security dashboard
                                    val intent = Intent(this@LoginActivity, ChiefSecurityDashboardActivity::class.java)
                                    intent.putExtra("user", user)
                                    editor.putString(keyTYPE, "chief")
                                    editor.apply()
                                    startActivity(intent)
                                }
                                "guard" -> {
                                    // Guard dashboard
                                    val intent = Intent(this@LoginActivity, SecurityDashboardActivity::class.java)
                                    intent.putExtra("user", user)
                                    editor.putString(keyTYPE, "guard")
                                    editor.apply()
                                    startActivity(intent)
                                }
                                else -> {
                                    // Unknown user
                                    Toast.makeText(this@LoginActivity, "Invalid User-Type", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // Wrong password
                            Toast.makeText(this@LoginActivity, "Wrong Password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "No user Found", Toast.LENGTH_SHORT).show()
                    }
                    }catch(e: Exception) {
                        Toast.makeText(this@LoginActivity, "Please Use the ID to login", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                    Toast.makeText(this@LoginActivity, "Database Error:"+error+"", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
