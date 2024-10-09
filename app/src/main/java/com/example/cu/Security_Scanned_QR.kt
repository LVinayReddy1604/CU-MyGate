package com.example.cu

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class Security_Scanned_QR : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var purposeTextView: TextView
    private lateinit var dateTimeTextView: TextView
    private lateinit var visitorImageView: ImageView
    private lateinit var progressBar: ProgressBar

    private var databaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security_scanned_qr)

        // Initialize views
        nameTextView = findViewById(R.id.tv_visitor_name)
        purposeTextView = findViewById(R.id.tv_visitor_purpose)
        dateTimeTextView = findViewById(R.id.tv_visitor_date_time)
        visitorImageView = findViewById(R.id.iv_visitor_image)
        progressBar = findViewById(R.id.progressBar)

        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val visitorIDString: String? = intent.getStringExtra("visitorID")
        val visitorId=trimFromDash(visitorIDString)
        val visitorRef = visitorId?.let {
            FirebaseDatabase.getInstance().getReference("Visitors").child(
                it
            )
        }
        if (visitorRef != null) {
            visitorRef.get().addOnSuccessListener { dataSnapshot ->
                val visitor = dataSnapshot.getValue(visitorDetails::class.java)
                if (visitor != null) {
                    // Set visitor details in views
                    nameTextView.text = "Name: ${visitor.name}"
                    purposeTextView.text = "Purpose: ${visitor.purpose}"
                    dateTimeTextView.text = "Date & Time: ${visitor.date} ${visitor.time}"

                    // Load image from Firebase Storage
                    val storageReference = FirebaseStorage.getInstance().reference.child("Visitors/${visitor.id}")
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(this)
                            .load(uri)
                            .into(visitorImageView)
                        progressBar.visibility = ProgressBar.GONE
                        visitorImageView.visibility=ImageView.VISIBLE
                        findViewById<ImageView>(R.id.tick).visibility=ImageView.VISIBLE
                        findViewById<TextView>(R.id.verified).visibility=TextView.VISIBLE
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                        progressBar.visibility=ProgressBar.GONE
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to fetch visitor details", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "Unknown Visitor", Toast.LENGTH_SHORT).show()
            if (nameTextView.text.toString().isEmpty()) {
                Toast.makeText(this, "Unknown Visitor", Toast.LENGTH_SHORT).show()
                progressBar.visibility = ProgressBar.GONE
                findViewById<ImageView>(R.id.cross).visibility = ImageView.VISIBLE
                findViewById<TextView>(R.id.InvalidQR).visibility = TextView.VISIBLE
            }
        }

//        if (nameTextView.text.toString().isEmpty()) {
//            Toast.makeText(this, "Unknown Visitor", Toast.LENGTH_SHORT).show()
//            progressBar.visibility = ProgressBar.GONE
//            findViewById<ImageView>(R.id.cross).visibility = ImageView.VISIBLE
//            findViewById<TextView>(R.id.InvalidQR).visibility = TextView.VISIBLE
//        }

    }
    fun trimFromDash(input: String?): String? {
        // Check if the input is null or empty
        if (input.isNullOrEmpty()) return null

        // Find the index of the first dash
        val dashIndex = input.indexOf('-')

        // Check if the dash exists and there is more content after it
        if (dashIndex != -1 && dashIndex < input.length - 1) {
            // Return the substring after the dash, trimmed of leading and trailing spaces
            return input.substring(dashIndex).trim()
        }

        // Return null if no valid substring exists
        return null
    }


    private fun fetchVisitorDetails() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading Visitor Details...")
        progressDialog.setMessage("Please wait...")
        progressDialog.show()

        val visitorIDString: String? = intent.getStringExtra("visitorID")

        if (visitorIDString != null) {
        val dashIndex = visitorIDString.indexOf('-')
            if (dashIndex != null) {
                if (dashIndex != -1 && dashIndex < visitorIDString.length - 1) {
                    val visitorID = visitorIDString.substring(dashIndex).trim()
                    if (visitorID != null) {
                        val visitorRef = databaseReference.child("Visitors").child(visitorID)
                        visitorRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            @SuppressLint("SetTextI18n")
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val visitor = snapshot.getValue(visitorDetails::class.java)

                                    if (visitor != null) {
                                        nameTextView.text = visitor.name
                                        purposeTextView.text = visitor.purpose
                                        dateTimeTextView.text = "${visitor.date} ${visitor.time}"

                                        val jpgimagename = "$visitorID.jpg"
                                        val pngimagename = "$visitorID.png"
                                        val jpegimagename = "$visitorID.jpeg"

                                        // Display image if available
                                        val JPGimageRef =
                                            FirebaseStorage.getInstance().reference.child("visitor_images/${jpgimagename}")
                                        JPGimageRef.downloadUrl.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val uri = task.result
                                                Glide.with(this@Security_Scanned_QR)
                                                    .load(uri)
                                                    .into(visitorImageView)
                                                visitorImageView.visibility = ImageView.VISIBLE
                                                progressBar.visibility=ProgressBar.GONE
                                            } else {
                                                val JPEGimageRef =
                                                    FirebaseStorage.getInstance().reference.child("visitor_images/${jpegimagename}")
                                                JPEGimageRef.downloadUrl.addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        val uri = task.result
                                                        Glide.with(this@Security_Scanned_QR)
                                                            .load(uri)
                                                            .into(visitorImageView)
                                                        visitorImageView.visibility = ImageView.VISIBLE
                                                        progressBar.visibility=ProgressBar.GONE
                                                    } else {
                                                        val PNGimageRef =
                                                            FirebaseStorage.getInstance().reference.child("visitor_images/${pngimagename}")
                                                        PNGimageRef.downloadUrl.addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                val uri = task.result
                                                                Glide.with(this@Security_Scanned_QR)
                                                                    .load(uri)
                                                                    .into(visitorImageView)
                                                                visitorImageView.visibility = ImageView.VISIBLE
                                                                progressBar.visibility=ProgressBar.GONE
                                                            } else {
                                                                Toast.makeText(
                                                                    this@Security_Scanned_QR,
                                                                    "Failed to fetch profile photo",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            this@Security_Scanned_QR,
                                            "Visitor details not found",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        this@Security_Scanned_QR,
                                        "Visitor ID does not exist",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                progressDialog.dismiss()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    this@Security_Scanned_QR,
                                    "Database error: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                progressDialog.dismiss()
                            }
                        })
                    } else {
                        Toast.makeText(this, "No visitor ID provided", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }
                }
            }else{
                Handler().postDelayed({
                    Toast.makeText(this, "Failed at ID's '-'", Toast.LENGTH_SHORT).show()
                }, 2000)
                finish()
            }
        }else{
            Handler().postDelayed({
                Toast.makeText(this, "Failed at PutExtras ", Toast.LENGTH_SHORT).show()
            }, 2000)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this,SecurityDashboardActivity::class.java))
    }
}
