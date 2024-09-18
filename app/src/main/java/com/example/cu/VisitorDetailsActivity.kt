package com.example.cu

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class VisitorDetailsActivity : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var purposeTextView: TextView
    private lateinit var dateTimeTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var departmentTextView: TextView
    private lateinit var enteredThroughTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visitor_details)

        // Initialize views
        nameTextView = findViewById(R.id.tv_detail_name)
        purposeTextView = findViewById(R.id.tv_detail_purpose)
        dateTimeTextView = findViewById(R.id.tv_detail_date_time)
        emailTextView = findViewById(R.id.tv_detail_email)
        phoneTextView = findViewById(R.id.tv_detail_phone)
        departmentTextView = findViewById(R.id.tv_detail_department)
        enteredThroughTextView = findViewById(R.id.tv_detail_entered_through)
        imageView = findViewById(R.id.iv_detail_image)
        progressBar = findViewById(R.id.progressBar)

        // Get visitorId from intent
        val visitorId = intent.getStringExtra("visitorId") ?: return
        val userType=intent.getStringExtra("userType")

        // Fetch visitor details from Firebase
        val visitorRef = FirebaseDatabase.getInstance().getReference("Visitors").child(visitorId)
        visitorRef.get().addOnSuccessListener { dataSnapshot ->
            val visitor = dataSnapshot.getValue(visitorDetails::class.java)
            if (visitor != null) {
                // Set visitor details in views
                nameTextView.text = "Name: ${visitor.name}"
                purposeTextView.text = "Purpose: ${visitor.purpose}"
                dateTimeTextView.text = "Date & Time: ${visitor.date} ${visitor.time}"
                emailTextView.text = "Email: ${visitor.email}"
                phoneTextView.text = "Phone: ${visitor.phone}"
                departmentTextView.text = "Department: ${visitor.department}"
                enteredThroughTextView.text = "Entered Through: ${visitor.enteredThrough}"

                // Load image from Firebase Storage
                val storageReference = FirebaseStorage.getInstance().reference.child("Visitors/${visitor.id}")
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(this)
                        .load(uri)
                        .into(imageView)
                    progressBar.visibility = View.GONE
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch visitor details", Toast.LENGTH_SHORT).show()
        }
    }
}
