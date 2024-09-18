package com.example.cu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class DepartmentDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_department_detail)

        val departmentName = intent.getStringExtra("department_name")
        val textView = findViewById<TextView>(R.id.textView)
        textView.text = departmentName
    }
}
