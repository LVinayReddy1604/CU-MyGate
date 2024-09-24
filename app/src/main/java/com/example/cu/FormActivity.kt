@file:Suppress("DEPRECATION")

package com.example.cu

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class FormActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etPurpose: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var spinnerEnteredThrough: Spinner
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etAssist: EditText
    private lateinit var btnSelectImage: Button
    private lateinit var ivSelectedImage: ImageView
    private var imageUri: Uri? = null
    private lateinit var id: String
    private lateinit var databaseReference: DatabaseReference
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        // Initialize form fields
        etName = findViewById(R.id.etName)
        etPurpose = findViewById(R.id.etPurpose)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)
        spinnerEnteredThrough = findViewById(R.id.spinnerEnteredThrough)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        ivSelectedImage = findViewById(R.id.ivSelectedImage)
        etAssist = findViewById(R.id.etAssist)

        // Initialize progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Selecting image...")
        progressDialog.setMessage("Processing...")

        // Apply input filter for phone number
        etPhone.filters = arrayOf(InputFilter.LengthFilter(10), PhoneNumberInputFilter())
        etAssist.filters = arrayOf(
            InputFilter.LengthFilter(4), AssistInputFilter()
        )
        val options = listOf("1", "2", "3", "4", "5")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, options)
        spinnerEnteredThrough.adapter = adapter

        // Initialize ActivityResultLauncher for image picking
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    imageUri = it
                    ivSelectedImage.setImageURI(it)
                    ivSelectedImage.visibility = View.VISIBLE
                    progressDialog.dismiss()  // Dismiss the progress dialog when image is loaded
                } ?: run {
                    progressDialog.dismiss()  // Dismiss the progress dialog if no image is selected
                    Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show()
                }
            }

        // Set up image picker
        btnSelectImage.setOnClickListener {
            progressDialog.show()  // Show the progress dialog when image selection starts
            imagePickerLauncher.launch("image/*")
        }

        // Set onClickListeners for date and time pickers
        etDate.setOnClickListener { showDatePicker() }
        etTime.setOnClickListener { showTimePicker() }

        // Submit button click listener
        val submitButton: Button = findViewById(R.id.btnSubmit)
        submitButton.setOnClickListener { submitForm() }

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            R.style.DialogTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDay = if (selectedDay < 10) "0$selectedDay" else "$selectedDay"
                val formattedMonth =
                    if (selectedMonth + 1 < 10) "0${selectedMonth + 1}" else "${selectedMonth + 1}"
                etDate.setText("$formattedDay/$formattedMonth/$selectedYear")
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.minDate = calendar.timeInMillis

        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog =
            TimePickerDialog(this, R.style.DialogTheme, { _, selectedHour, selectedMinute ->
                var adjustedHour = selectedHour
                var adjustedMinute = selectedMinute

                // Adjust time if it's outside the range 6 AM to 6 PM
                if (selectedHour < 6) {
                    adjustedHour = 6
                    adjustedMinute = 0
                } else if (selectedHour >= 18) {
                    adjustedHour = 17
                    adjustedMinute = 59
                }

                val amPm = if (adjustedHour < 12) "AM" else "PM"
                val hourIn12Format = if (adjustedHour % 12 == 0) 12 else adjustedHour % 12
                val formattedHour =
                    if (hourIn12Format < 10) "0$hourIn12Format" else "$hourIn12Format"
                val formattedMinute =
                    if (adjustedMinute < 10) "0$adjustedMinute" else "$adjustedMinute"
                etTime.setText("$formattedHour:$formattedMinute $amPm")
            }, hour, minute, false)

        timePickerDialog.show()
    }

    private fun submitForm() {
        // Gather form data
        val name = etName.text.toString()
        val purpose = etPurpose.text.toString()
        val date = etDate.text.toString()
        val time = etTime.text.toString()
        val enteredThrough = spinnerEnteredThrough.selectedItem.toString()
        val phone = etPhone.text.toString()
        val email = etEmail.text.toString()
        val assisted = etAssist.text.toString()

        // Validate fields
        if (name.isEmpty() || purpose.isEmpty() || date.isEmpty() || time.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Visitors")
        id = databaseReference.push().key!!

        val uploaderID = intent.getStringExtra("id")
        val approved = "false"
        val department = intent.getStringExtra("dept")
        val visitor = visitorDetails(
            id,
            uploaderID,
            approved,
            department,
            name,
            purpose,
            date,
            time,
            enteredThrough,
            phone,
            email,
            assisted,
            status = "Unvisited"
        )
        databaseReference.child(id).setValue(visitor).addOnCompleteListener {
            Toast.makeText(this, "Data added Successfully", Toast.LENGTH_SHORT).show()
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading Image...")
            progressDialog.setMessage("Processing...")
            progressDialog.show()

            val ref: StorageReference =
                FirebaseStorage.getInstance().getReference().child("Visitors").child("$id")
            imageUri?.let {
                ref.putFile(it).addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Image Uploaded successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Upload unsuccessful", Toast.LENGTH_SHORT).show()
                }
            }
            progressDialog.cancel()
            onBackPressed()
        }.addOnFailureListener { err ->
            Toast.makeText(this, "Error: ${err.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        if (phone.matches(Regex("^[9876]\\d{9}$"))) {
            return phone.toCharArray().distinct().size > 1
        }
        return false
    }

    private fun uploadImage() {

    }

    private class PhoneNumberInputFilter : InputFilter {
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
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
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            // Combine the current input with the new input
            val input = dest.subSequence(0, dstart).toString() + source.subSequence(
                start,
                end
            ) + dest.subSequence(dend, dest.length)

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
