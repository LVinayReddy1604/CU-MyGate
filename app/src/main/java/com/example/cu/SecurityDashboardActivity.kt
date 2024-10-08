package com.example.cu

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
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

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

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

        val sh = getSharedPreferences(keyNAME, MODE_PRIVATE)
        user = sh.getString("user", "").toString()

        // Set click listener for Add Visitor button
        addVisitorButton.setOnClickListener {
            val intent = Intent(this, DirectFormActivity::class.java)
            intent.putExtra("id", user)
            startActivity(intent)
        }

        // Set click listener for View Approved Visitors button
        viewApprovedVisitorsButton.setOnClickListener {
            viewApprovedVisitors()
        }

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Set click listener for Start Scanning button
        startScanningButton.setOnClickListener {
//            startQRCodeScanner()
            val intent=Intent(this,QR_scanner::class.java)
                startActivity(intent)
        }
    }

    private fun startQRCodeScanner() {
        barcodeScanner = BarcodeScanning.getClient()
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview setup
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
            }

            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Image analysis setup
            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, { imageProxy ->
                    processImageProxy(imageProxy)
                })
            }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        @androidx.camera.core.ExperimentalGetImage
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (rawValue != null) {
                            handleQRCodeResult(rawValue)
                        }
                    }
                }
                .addOnFailureListener {
                    // Handle any errors
                    Log.e("BarcodeScanner", "Error processing image: ${it.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun handleQRCodeResult(qrCodeValue: String) {
        val intent = Intent(this, Security_Scanned_QR::class.java)
        intent.putExtra("visitorID", qrCodeValue)
        startActivity(intent)
    }

    private fun viewApprovedVisitors() {
        val intent = Intent(this, SecurityApprovedActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
