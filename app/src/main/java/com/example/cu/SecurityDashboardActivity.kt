package com.example.cu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SecurityDashboardActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var startScanningButton: Button
    private lateinit var scannedResultTextView: TextView
    private lateinit var addVisitorButton: Button
    private lateinit var viewApprovedVisitorsButton: Button
    private lateinit var user: String

    private val keyNAME = "MYGATE_CRED"
    private val keyUSERNAME = "user"
    private val keyTYPE = "type"

    private lateinit var sharedPreferences: android.content.SharedPreferences


    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Visitors")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security_dashboard)

        // Initialize views
        backButton = findViewById(R.id.backButton)
        startScanningButton = findViewById(R.id.btnStartScanning)
        scannedResultTextView = findViewById(R.id.tvScannedResult)
        addVisitorButton = findViewById(R.id.btnAddVisitor)
        viewApprovedVisitorsButton = findViewById(R.id.btnViewApprovedVisitors)

        // Set click listener for back button
        backButton.setOnClickListener {
            sharedPreferences = getSharedPreferences(keyNAME, MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(keyUSERNAME, null)
            editor.putString(keyTYPE, null)
            editor.apply()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Set click listener for Start Scanning button
        startScanningButton.setOnClickListener {
            startQRScanner()
        }

        val sh = getSharedPreferences(keyNAME, MODE_PRIVATE)
        user = sh.getString("user", "").toString()

        // Set click listener for Add Visitor button
        addVisitorButton.setOnClickListener {
            val intent = Intent(this, DirectFormActivity::class.java)
            intent.putExtra("id",user)
            startActivity(intent)
        }

        // Set click listener for View Approved Visitors button
        viewApprovedVisitorsButton.setOnClickListener {
            viewApprovedVisitors()
        }
    }

    private fun viewApprovedVisitors() {
        // Fetch approved visitors from Firebase and start a new Activity to display them
        val intent = Intent(this, SecurityApprovedActivity::class.java)
        startActivity(intent)
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(false)
        integrator.setPrompt("Scan a QR Code")
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    // Handle the result of QR code scanning
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                // Handle cancel
                scannedResultTextView.text = "Scan canceled"
            } else {
                // Handle successful scan
                val intent=Intent(this,Security_Scanned_QR::class.java)
                intent.putExtra("visitorID",result.contents)
                startActivity(intent)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
