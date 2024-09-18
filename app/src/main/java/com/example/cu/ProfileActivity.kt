package com.example.cu

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Example: Retrieving intent extras
        val teacherName = intent.getStringExtra("Name") ?: "Teacher Name"
        val position = intent.getStringExtra("position") ?: "Position"
        val department = intent.getStringExtra("department") ?: "Department"
        val collegeName = intent.getStringExtra("campus") ?: "College Name"
        val email = intent.getStringExtra("email") ?: "Email"

        val type=intent.getStringExtra("type")?:"teacher"
        val user=intent.getStringExtra("user")

        // Example: Displaying data in TextViews
        findViewById<TextView>(R.id.tv_name).text = teacherName
        findViewById<TextView>(R.id.tv_position).text = position
        findViewById<TextView>(R.id.tv_department).text = department
        findViewById<TextView>(R.id.tv_college).text = "$collegeName campus"
        findViewById<TextView>(R.id.tv_email).text = email

        val imagename="$type/$user"
        val jpgimagename = "$type/$user.jpg"
        val pngimagename = "$type/$user.png"
        val jpegimagename = "$type/$user.jpeg"

        val storageref = FirebaseStorage.getInstance().reference.child(imagename)

        storageref.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uri = task.result
                Glide.with(this@ProfileActivity)
                    .load(uri)
                    .into(findViewById(R.id.iv_profile))
            } else {
                // Try with PNG if JPG fails
                val storagerefPng = FirebaseStorage.getInstance().reference.child(pngimagename)
                storagerefPng.downloadUrl.addOnCompleteListener { taskPng ->
                    if (taskPng.isSuccessful) {
                        val uriPng = taskPng.result
                        Glide.with(this@ProfileActivity)
                            .load(uriPng)
                            .into(findViewById(R.id.iv_profile))
                    } else {
                        val storagerefJPG = FirebaseStorage.getInstance().reference.child(jpgimagename)
                        storagerefJPG.downloadUrl.addOnCompleteListener { taskjpg ->
                            if (taskPng.isSuccessful) {
                                val uriPng = taskjpg.result
                                Glide.with(this@ProfileActivity)
                                    .load(uriPng)
                                    .into(findViewById(R.id.iv_profile))
                            } else {
                                val storagerefJPEG = FirebaseStorage.getInstance().reference.child(jpegimagename)
                                storagerefJPEG.downloadUrl.addOnCompleteListener { taskjpeg ->
                                    if (taskPng.isSuccessful) {
                                        val uriPng = taskjpeg.result
                                        Glide.with(this@ProfileActivity)
                                            .load(uriPng)
                                            .into(findViewById(R.id.iv_profile))
                                    } else {
                                        Toast.makeText(
                                            this@ProfileActivity,
                                            "Failed to fetch profile photo",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
