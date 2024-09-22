@file:Suppress("DEPRECATION")

package com.example.cu

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.InputFilter
import android.text.Spanned
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DirectFormActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etPurpose: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var spinnerEnteredThrough: Spinner
    private lateinit var etPhone: EditText
    private lateinit var etAssist: EditText
    private lateinit var btnSelectImage: Button
    private lateinit var ivSelectedImage: ImageView
    private var imageUri: Uri? = null
    private lateinit var id: String
    private lateinit var databaseReference: DatabaseReference
    private lateinit var progressDialog: ProgressDialog
    private lateinit var currentPhotoPath: String
    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direct_form)

        // Initialize form fields
        etName = findViewById(R.id.etName)
        etPurpose = findViewById(R.id.etPurpose)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)
        spinnerEnteredThrough = findViewById(R.id.spinnerEnteredThrough)
        etPhone = findViewById(R.id.etPhone)
        etAssist = findViewById(R.id.etAssist)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        ivSelectedImage = findViewById(R.id.ivSelectedImage)

        // Initialize progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Selecting image...")
        progressDialog.setMessage("Processing...")


        etPhone.filters = arrayOf(InputFilter.LengthFilter(10),PhoneNumberInputFilter())
        etAssist.filters = arrayOf(InputFilter.LengthFilter(4),AssistInputFilter())

        val options = listOf("2", "3", "4")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, options)
        spinnerEnteredThrough.adapter = adapter

        btnSelectImage.setText("Click Photo")
        btnSelectImage.setOnClickListener {
            dispatchTakePictureIntent()
        }

        // Set onClickListeners for date and time pickers
        var currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        etDate.setText(currentDate)
        etDate.isEnabled = false
        etDate.setOnClickListener{
            currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            etDate.setText(currentDate)
        }

        var currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        etTime.setText(currentTime)
        etTime.isEnabled = false
        etTime.setOnClickListener{
            currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            etTime.setText(currentTime)
        }

        val submitButton: Button = findViewById(R.id.btnSubmit)
        submitButton.setOnClickListener { submitForm() }

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                null
            }
            // Continue only if the File was successfully created
            photoFile?.let {
                imageUri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            ivSelectedImage.setImageBitmap(bitmap)
            ivSelectedImage.visibility = View.VISIBLE
        }
    }

    private fun submitForm() {
        // Gather form data
        val name = etName.text.toString()
        val purpose = etPurpose.text.toString()
        val vehicle = spinnerEnteredThrough.selectedItem.toString()
        val number = findViewById<EditText>(R.id.etReceivedBy).text.toString()
        val date = etDate.text.toString()
        val time = etTime.text.toString()
        val phone = etPhone.text.toString()
        val assisted = etAssist.text.toString()

        // Validate fields
        if (name.isEmpty() || purpose.isEmpty() || date.isEmpty() || time.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading Image...")
        progressDialog.setMessage("Processing...")
        progressDialog.show()

        databaseReference = FirebaseDatabase.getInstance().getReference("VisitorsUninvited")
        id = databaseReference.push().key!!

        val uploaderID = intent.getStringExtra("id")
        val visitor = uninvitedDetails(
            id,
            uploaderID,
            name,
            purpose,
            vehicle,
            number,
            date,
            time,
            phone,
            assisted
        )
        databaseReference.child(id).setValue(visitor).addOnCompleteListener {
            uploadImage()
            Handler().postDelayed({
                Toast.makeText(this, "Data Added successfully", Toast.LENGTH_SHORT).show()
            }, 2000)
            progressDialog.dismiss()
        }.addOnFailureListener { err ->
            Toast.makeText(this, "Error: ${err.message}", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        if (phone.matches(Regex("^[9876]\\d{9}$"))) {
            return phone.toCharArray().distinct().size > 1
        }
        return false
    }

    private fun uploadImage() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading Image...")
        progressDialog.setMessage("Processing...")
        progressDialog.show()

        val ref: StorageReference = FirebaseStorage.getInstance().getReference().child("Visitors").child("$id")
        imageUri?.let {
            ref.putFile(it).addOnSuccessListener {
                progressDialog.dismiss()
                finish()
            }.addOnFailureListener { err ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private class PhoneNumberInputFilter : InputFilter {
        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
            val input = (dest.toString() + source.toString()).trim()
            // Allow only digits, ensure the total length is <= 10, and ensure the first digit is 9, 8, 7, or 6
            return if (input.matches(Regex("^[9876]\\d{0,9}$"))) {
                null
            } else {
                ""
            }
        }
    }

    private class AssistInputFilter : InputFilter {
        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
            // Combine the current input with the new input
            val input = dest.subSequence(0, dstart).toString() + source.subSequence(start, end) + dest.subSequence(dend, dest.length)

            // Check if the input length exceeds 10 digits
            if (input.length > 3) {
                return ""
            }

            // Ensure the input contains only digits
            return if (input.matches(Regex("\\d*"))) {
                null  // Accept input if it's valid
            } else {
                ""    // Reject input if it contains non-digit characters
            }
        }
    }

}