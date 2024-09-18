package com.example.cu

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class SecurityApprovedActivity : AppCompatActivity() {

    private lateinit var etDate: EditText
    private lateinit var recyclerViewVisitors: RecyclerView
    private lateinit var visitorAdapter: VisitorAdapter
    private lateinit var databaseRef: DatabaseReference
    private var visitors = mutableListOf<visitorDetails>()
    private lateinit var selectedDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security_approved)

        etDate = findViewById(R.id.etDate)
        recyclerViewVisitors = findViewById(R.id.recyclerViewVisitors)
        recyclerViewVisitors.layoutManager = LinearLayoutManager(this)

        visitorAdapter = VisitorAdapter(this, visitors)
        recyclerViewVisitors.adapter = visitorAdapter

        databaseRef = FirebaseDatabase.getInstance().getReference("Visitors")

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
        val visitorRef = FirebaseDatabase.getInstance().getReference("Visitors")
        val query = visitorRef.orderByChild("date").equalTo(Date)

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
                // Sort visitors by date and time in descending order
                // visitors.sortWith(compareByDescending<visitorDetails> { parseDate(it.date.toString()) }
                //     .thenByDescending { parseTime(it.time.toString()) })

                // Set user type via the public method
                visitorAdapter.setUserType("Guard",false)
                visitorAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SecurityApprovedActivity, "Failed to fetch visitor data", Toast.LENGTH_SHORT).show()
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
