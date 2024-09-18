package com.example.cu

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.annotation.SuppressLint
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Chief_Dept_Approved : AppCompatActivity() {
    private lateinit var recyclerViewVisitors: RecyclerView
    private lateinit var visitorAdapter: VisitorAdapter
    private val visitors = mutableListOf<visitorDetails>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chief_dept_approved)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val departmentName = intent.getStringExtra("department_name")

        // Set up RecyclerView for visitors in a linear layout
        recyclerViewVisitors = findViewById(R.id.recyclerViewVisitors)
        recyclerViewVisitors.layoutManager = LinearLayoutManager(this)

        visitorAdapter = VisitorAdapter(this, visitors)
        recyclerViewVisitors.adapter = visitorAdapter

        if (departmentName != null) {
            findViewById<TextView>(R.id.tv_deptName).text = departmentName
            fetchVisitorData(departmentName)
        } else {
            findViewById<TextView>(R.id.tv_deptName).text = "NOT SET"
            Toast.makeText(this, "Department is not fetched", Toast.LENGTH_SHORT).show()
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
                    if (visitor?.approved == "true") {
                        visitor?.let { visitors.add(it) }
                    }
                }

                // Set user type via the public method
                visitorAdapter.setUserType("Chief",false)
                visitorAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Chief_Dept_Approved, "Failed to fetch visitor data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
