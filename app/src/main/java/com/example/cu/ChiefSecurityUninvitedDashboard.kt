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
import android.app.DatePickerDialog
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChiefSecurityUninvitedDashboard : AppCompatActivity() {

    private lateinit var etDate: EditText
    private lateinit var recyclerViewVisitors: RecyclerView
    private lateinit var visitorAdapter: UninvitedVisitorAdaptor
    private lateinit var selectedDate: String
    private val visitors = mutableListOf<uninvitedDetails>() // Correct type here

    @SuppressLint("MissingInflatedId")
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

        etDate = findViewById(R.id.etDate)

        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        etDate.setText("$currentDate")
        selectedDate=etDate.text.toString()

        etDate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val selectedDate = s.toString()
                if (isValidDate(selectedDate)) {
                    loadVisitorsByDate(selectedDate)
                } else {
                    visitors.clear()
                    visitorAdapter.notifyDataSetChanged()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etDate.setOnClickListener { showDatePicker() }

    }

    private fun loadVisitorsByDate(Date: String) {
        val visitorRef = FirebaseDatabase.getInstance().getReference("VisitorsUninvited")
        val query = visitorRef.orderByChild("date").equalTo(Date)

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

    private fun isValidDate(dateStr: String): Boolean {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.isLenient = false
            format.parse(dateStr) != null
        } catch (e: Exception) {
            false
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year: Int
        val month: Int
        val day: Int

        if (selectedDate != null) {
            // Parse the previously selected date
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = format.parse(selectedDate)
            if (date != null) {
                calendar.time = date
            }
        }

        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, R.style.DialogTheme, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDay = if (selectedDay < 10) "0$selectedDay" else "$selectedDay"
            val formattedMonth = if (selectedMonth + 1 < 10) "0${selectedMonth + 1}" else "${selectedMonth + 1}"
            val newDate = "$formattedDay/$formattedMonth/$selectedYear"
            etDate.setText(newDate)
            selectedDate = newDate // Update the selected date
        }, year, month, day)

        datePickerDialog.show()
    }
}
