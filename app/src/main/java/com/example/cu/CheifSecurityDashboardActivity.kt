package com.example.cu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChiefSecurityDashboardActivity : AppCompatActivity() {

    private val keyNAME = "MYGATE_CRED"
    private val keyUSERNAME = "user"
    private val keyTYPE = "type"

    private lateinit var sharedPreferences: android.content.SharedPreferences
    private val databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://cu-mygate-default-rtdb.firebaseio.com/")

    // Department RecyclerView Adapter
    private lateinit var departmentAdapter: DepartmentAdapter
    private val departments = mutableListOf<Department>()

    // UI Elements
    private lateinit var btnMenu: ImageButton
    private lateinit var btnHome: Button
    private lateinit var btnApproved: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cheif_security_dashboard)

        // Initialize UI elements
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.iv_menu)
        btnHome = findViewById(R.id.btn_home)
        btnApproved = findViewById(R.id.btn_approved)

        // Set up RecyclerView for departments in a grid layout
        val recyclerViewDepartments = findViewById<RecyclerView>(R.id.recyclerViewDepartments)
        recyclerViewDepartments.layoutManager = GridLayoutManager(this, 2)

        departmentAdapter = DepartmentAdapter(departments) { department ->
            // Start Chief_Dept_Unapproved activity and pass the department name
            val intent = Intent(this, Chief_Dept_Unapproved::class.java)
            intent.putExtra("department_name", department.name)
            startActivity(intent)
        }
        recyclerViewDepartments.adapter = departmentAdapter

        // Fetch data for departments
        fetchDepartmentsFromDatabase()

        btnApproved.setOnClickListener {
            // Navigate to Approved Activity
            val intent = Intent(this, ApprovedVisitorsActivity::class.java)
            startActivity(intent)
            drawerLayout.close()
        }

        btnHome.setOnClickListener {
            val intent = Intent(this, ChiefSecurityDashboardActivity::class.java)
            startActivity(intent)
            drawerLayout.close()
        }

        // Set up menu button to open the drawer
        btnMenu.setOnClickListener {
            drawerLayout.open()
        }

        // Handle navigation view item clicks
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                // Handle navigation view item clicks here
            }
            drawerLayout.close()
            true
        }

        // Logout button functionality
        val btnLogout = findViewById<ImageButton>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            sharedPreferences = getSharedPreferences(keyNAME, MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(keyUSERNAME, null)
            editor.putString(keyTYPE, null)
            editor.apply()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun fetchDepartmentsFromDatabase() {
        val query = databaseReference.child("Visitors")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val departmentCountMap = mutableMapOf<String, Int>()
                departments.clear()

                for (visitorSnapshot in snapshot.children) {
                    val departmentName = visitorSnapshot.child("department").getValue(String::class.java)
                    val approved = visitorSnapshot.child("approved").getValue(String::class.java) ?: "false"

                    // Only count if approved is false
                    if ((departmentName != null) && (approved == "false")) {
                        departmentCountMap[departmentName] = departmentCountMap.getOrDefault(departmentName, 0) + 1
                    }
                }

                for ((departmentName, count) in departmentCountMap) {
                    val departmentIcon = when (departmentName.lowercase()) {
                        "architecture" -> R.drawable.ic_architecture
                        "engineering" -> R.drawable.ic_engineering
                        "chemistry" -> R.drawable.ic_chemistry
                        "cs" -> R.drawable.ic_architecture
                        else -> R.drawable.ic_architecture // Default icon if department is unknown
                    }
                    departments.add(Department(departmentName, departmentIcon, count))
                }

                departmentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChiefSecurityDashboardActivity, "Failed to fetch department data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
