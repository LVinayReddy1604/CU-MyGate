package com.example.cu

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.media3.common.util.UnstableApi
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.media3.common.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerViewUpcomingEvents: RecyclerView
    private lateinit var department: String
    private lateinit var name: String
    private lateinit var type: String
    private lateinit var user: String
    private lateinit var position: String
    private lateinit var campus: String
    private lateinit var mail: String

    private lateinit var tvName: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvMail: TextView
    private lateinit var tvTitle: TextView

    private val keyNAME = "MYGATE_CRED"
    private val keyUSERNAME = "user"
    private val keyTYPE = "type"

    private var databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://cu-mygate-default-rtdb.firebaseio.com/")
    private lateinit var sharedPreferences: android.content.SharedPreferences

    private lateinit var recyclerViewVisitors: RecyclerView
    private lateinit var visitorAdapter: VisitorAdapter
    private val visitors = mutableListOf<visitorDetails>()

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_dashboard)

        drawerLayout = findViewById(R.id.drawer_layout)

        // Initialize RecyclerView for upcoming events
        recyclerViewUpcomingEvents = findViewById(R.id.recyclerViewUpcomingEvents)
        recyclerViewUpcomingEvents.layoutManager = LinearLayoutManager(this)
        recyclerViewUpcomingEvents.adapter = VisitorAdapter(this, visitors)

        // Initialize RecyclerView for visitors
        recyclerViewVisitors = findViewById(R.id.recyclerViewVisitors) // Corrected ID
        recyclerViewVisitors.layoutManager = LinearLayoutManager(this)
        visitorAdapter = VisitorAdapter(this, visitors)
        recyclerViewVisitors.adapter = visitorAdapter

        // Menu icon click listener to toggle drawer
        val menuButton = findViewById<ImageButton>(R.id.iv_menu)
        menuButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.sidebar))) {
                drawerLayout.closeDrawer(findViewById(R.id.sidebar))
            } else {
                drawerLayout.openDrawer(findViewById(R.id.sidebar))
            }
        }

        // Fetch user details
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading Profile...")
        progressDialog.setMessage("Processing...")
        progressDialog.show()

        val sh = getSharedPreferences(keyNAME, MODE_PRIVATE)
        user = sh.getString("user", "").toString()

        if (user == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        databaseReference.child("login").addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if username exists in DB
                if (snapshot.hasChild(user)) {
                    name = snapshot.child(user).child("name").value.toString()
                    department = snapshot.child(user).child("dept").value.toString()
                    type = snapshot.child(user).child("type").value.toString()
                    mail = snapshot.child(user).child("mail").value.toString()
                    position = snapshot.child(user).child("position").value.toString()
                    campus = snapshot.child(user).child("campus").value.toString()

                    tvTitle = findViewById(R.id.tv_title)
                    tvTitle.text = "Hello $name"
                    val imagename = "$type/$user"
                    val jpgimagename = "$type/$user.jpg"
                    val pngimagename = "$type/$user.png"
                    val jpegimagename = "$type/$user.jpeg"

                    val storageref = FirebaseStorage.getInstance().reference.child(imagename)
                    storageref.downloadUrl.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uri = task.result
                            Glide.with(this@TeacherDashboardActivity)
                                .load(uri)
                                .into(findViewById(R.id.iv_profile))
                        } else {
                            // Try with PNG if JPG fails
                            val storagerefPng = FirebaseStorage.getInstance().reference.child(pngimagename)
                            storagerefPng.downloadUrl.addOnCompleteListener { taskPng ->
                                if (taskPng.isSuccessful) {
                                    val uriPng = taskPng.result
                                    Glide.with(this@TeacherDashboardActivity)
                                        .load(uriPng)
                                        .into(findViewById(R.id.iv_profile))
                                } else {
                                    val storagerefJPG = FirebaseStorage.getInstance().reference.child(jpgimagename)
                                    storagerefJPG.downloadUrl.addOnCompleteListener { taskJpg ->
                                        if (taskJpg.isSuccessful) {
                                            val uriJpg = taskJpg.result
                                            Glide.with(this@TeacherDashboardActivity)
                                                .load(uriJpg)
                                                .into(findViewById(R.id.iv_profile))
                                        } else {
                                            val storagerefJPEG = FirebaseStorage.getInstance().reference.child(jpegimagename)
                                            storagerefJPEG.downloadUrl.addOnCompleteListener { taskJpeg ->
                                                if (taskJpeg.isSuccessful) {
                                                    val uriJpeg = taskJpeg.result
                                                    Glide.with(this@TeacherDashboardActivity)
                                                        .load(uriJpeg)
                                                        .into(findViewById(R.id.iv_profile))
                                                } else {
                                                    Toast.makeText(
                                                        this@TeacherDashboardActivity,
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

                    // Initialize TextViews after fetching data
                    tvName = findViewById(R.id.tv_name)
                    tvRole = findViewById(R.id.tv_role)
                    tvMail = findViewById(R.id.tv_contact)

                    tvName.text = name
                    tvRole.text = position
                    tvMail.text = mail
                    // Fetch visitor data
                    fetchVisitorData(department)

                } else {
                    Toast.makeText(this@TeacherDashboardActivity, "Failed to fetch user details", Toast.LENGTH_SHORT).show()
                }
                progressDialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TeacherDashboardActivity, "Database Error", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        })

        //status button
        val statusbtn=findViewById<Button>(R.id.btn_status)
        statusbtn.setOnClickListener {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.sidebar))) {
                drawerLayout.closeDrawer(findViewById(R.id.sidebar))
            }
            startActivity(Intent(this, VisitorStatusTeacherDashboard::class.java))
        }


        // Home button click listener
        val btnHome = findViewById<Button>(R.id.btn_home)
        btnHome.setOnClickListener {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.sidebar))) {
                drawerLayout.closeDrawer(findViewById(R.id.sidebar))
            } else {
                drawerLayout.openDrawer(findViewById(R.id.sidebar))
            }
        }

        // Create an invite button click listener
        val btnAddVisitor = findViewById<ImageButton>(R.id.btn_add_visitor)
        val txtAddVisitor = findViewById<TextView>(R.id.tv_create_invite)
        btnAddVisitor.setOnClickListener {
            val intent = Intent(this, FormActivity::class.java).apply {
                putExtra("dept", department)
                putExtra("id", user)
            }
            startActivity(intent)
        }
        findViewById<RelativeLayout>(R.id.RLInvite).setOnClickListener {
            val intent = Intent(this, FormActivity::class.java).apply {
                putExtra("dept", department)
                putExtra("id", user)
            }
            startActivity(intent)
        }
        txtAddVisitor.setOnClickListener {
            val intent = Intent(this, FormActivity::class.java).apply {
                putExtra("dept", department)
                putExtra("id", user)
            }
            startActivity(intent)
        }

        // Logout button implementation
        val btnLogout = findViewById<Button>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            sharedPreferences = getSharedPreferences(keyNAME, MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(keyUSERNAME, null)
            editor.putString(keyTYPE, null)
            editor.apply()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Profile button click listener to open ProfileActivity
        val profileButton = findViewById<Button>(R.id.btn_profile)
        profileButton.setOnClickListener {
            val intent = Intent(this@TeacherDashboardActivity, ProfileActivity::class.java).apply {
                putExtra("Name", name)
                putExtra("position", position)
                putExtra("department", department)
                putExtra("campus", campus)
                putExtra("email", mail)
                putExtra("type", type)
                putExtra("user", user)
            }
            startActivity(intent)
        }
    }
    private fun fetchVisitorData(department: String) {
        val visitorRef = FirebaseDatabase.getInstance().getReference("Visitors")
        val query = visitorRef.orderByChild("department").equalTo(department)

        query.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                visitors.clear()
                for (visitorSnapshot in snapshot.children) {
                    val visitor = visitorSnapshot.getValue(visitorDetails::class.java)
                    // Check if the visitor's approved field is true
                    if (visitor?.approved == "true") {
                        visitor?.let { visitors.add(it) }
                    }
                }

                visitors.reverse()
                // Sort visitors by date and time in descending order
                // visitors.sortWith(compareByDescending<visitorDetails> { parseDate(it.date.toString()) }
                //     .thenByDescending { parseTime(it.time.toString()) })
                visitorAdapter.setUserType("Teacher",false) // Corrected this line
                visitorAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TeacherDashboardActivity, "Failed to fetch visitor data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    private fun parseDate(dateStr: String): Date {
        val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return format.parse(dateStr) ?: Date(0)
    }

    private fun parseTime(timeStr: String): Date {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.parse(timeStr) ?: Date(0)
    }
}
