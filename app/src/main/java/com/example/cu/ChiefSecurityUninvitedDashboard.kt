package com.example.cu

import android.os.Bundle
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

class ChiefSecurityUninvitedDashboard : AppCompatActivity() {
    private lateinit var recyclerViewVisitors: RecyclerView
    private lateinit var visitorAdapter: UninvitedVisitorAdaptor
    private val visitors = mutableListOf<uninvitedDetails>() // Correct type here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chief_security_uninvited_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up RecyclerView for visitors in a linear layout
        recyclerViewVisitors = findViewById(R.id.recyclerViewVisitors)
        recyclerViewVisitors.layoutManager = LinearLayoutManager(this)

        visitorAdapter = UninvitedVisitorAdaptor(this, visitors) // Correct usage
        recyclerViewVisitors.adapter = visitorAdapter

        fetchVisitorData()
    }

    private fun fetchVisitorData() {
        val visitorRef = FirebaseDatabase.getInstance().getReference("VisitorsUninvited")
        val query = visitorRef.orderByChild("date")

        query.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                visitors.clear()

                // Fetch all visitor data
                for (visitorSnapshot in snapshot.children) {
                    val visitor = visitorSnapshot.getValue(uninvitedDetails::class.java)
                    if (visitor != null) {
                        visitors.add(visitor)
                    }
                }

                // Reverse the list to show the most recent dates at the top
                visitors.reverse()

                // Set user type via the public method
                visitorAdapter.setUserType("security", false)

                // Notify adapter of the data change
                visitorAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChiefSecurityUninvitedDashboard, "Failed to fetch visitor data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
