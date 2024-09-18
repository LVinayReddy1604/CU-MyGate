package com.example.cu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OptionalActivity : AppCompatActivity() {

    private lateinit var teacherDashboardButton: Button
    private lateinit var chiefSecurityDashboardButton: Button
    private lateinit var securityDashboardButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_optional)

        teacherDashboardButton = findViewById(R.id.btn_teacher_dashboard)
        chiefSecurityDashboardButton = findViewById(R.id.btn_chief_security_dashboard)
        securityDashboardButton = findViewById(R.id.btn_security_dashboard)

        teacherDashboardButton.setOnClickListener {
            val intent = Intent(this, TeacherDashboardActivity::class.java)
            startActivity(intent)
        }

        chiefSecurityDashboardButton.setOnClickListener {
            val intent = Intent(this, ChiefSecurityDashboardActivity::class.java)
            startActivity(intent)
        }

        securityDashboardButton.setOnClickListener {
            val intent = Intent(this, SecurityDashboardActivity::class.java)
            startActivity(intent)
        }
    }
}
