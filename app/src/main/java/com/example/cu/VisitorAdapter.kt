package com.example.cu

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

//imports for email
import android.graphics.Bitmap
import android.util.Base64
import android.widget.ImageButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream


class VisitorAdapter(private val context: Context, private var visitors: MutableList<visitorDetails>) :
    RecyclerView.Adapter<VisitorAdapter.ViewHolder>() {

    private var userType = "dummy"
    private var userStatusCheck=false

    // Method to set the userType
    fun setUserType(type: String,status: Boolean) {
        userType = type
        userStatusCheck=status
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.tv_visitor_name)
        val purposeTextView: TextView = view.findViewById(R.id.tv_visitor_purpose)
        val dateTimeTextView: TextView = view.findViewById(R.id.tv_visitor_date_time)
        val visitorImageView: ImageView = view.findViewById(R.id.iv_visitor_image)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val approvedTextView: TextView = view.findViewById(R.id.tv_approved)
        val btnAccept: Button = view.findViewById(R.id.btn_accept)
        val btnReject: Button = view.findViewById(R.id.btn_reject)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.visitor_card_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val visitor = visitors[position]
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference
        val imageRef = storageReference.child("Visitors/${visitor.id}")

        holder.progressBar.visibility = View.VISIBLE
        holder.visitorImageView.visibility = View.GONE

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(context)
                .load(uri)
                .into(holder.visitorImageView)
            holder.progressBar.visibility = View.GONE
            holder.visitorImageView.visibility = View.VISIBLE
        }.addOnFailureListener {
            holder.progressBar.visibility = View.GONE
            holder.visitorImageView.visibility = View.VISIBLE
            Toast.makeText(context, "Failed to fetch images of visitors", Toast.LENGTH_SHORT).show()
        }

        holder.nameTextView.text = visitor.name
        holder.purposeTextView.text = visitor.purpose
        holder.dateTimeTextView.text = "${visitor.date} ${visitor.time}"

        when (visitor.approved) {
            "true" -> {
                holder.approvedTextView.text = "Approved"
                holder.btnAccept.text = "Disapprove"
                holder.btnAccept.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.btnAccept.backgroundTintList = ContextCompat.getColorStateList(context, R.color.red)
                holder.approvedTextView.setTextColor(ContextCompat.getColor(context, R.color.green))
            }
            "false" -> {
                holder.approvedTextView.text = "Pending"
                holder.btnAccept.text = "Approve"
                holder.btnAccept.backgroundTintList = ContextCompat.getColorStateList(context, R.color.green)
                holder.approvedTextView.setTextColor(ContextCompat.getColor(context, R.color.red))
            }
            else -> {
                holder.approvedTextView.text = "Rejected"
                holder.approvedTextView.setTextColor(ContextCompat.getColor(context, R.color.yellow))
            }
        }

        // Show/hide buttons based on userType
        if ((userType == "Teacher") || ((userType == "Guard") && (userStatusCheck == false))) {
            holder.btnAccept.visibility = View.GONE
            holder.btnReject.visibility = View.GONE
        } else {
            holder.btnAccept.visibility = View.VISIBLE
            holder.btnReject.visibility = View.VISIBLE
        }

        // Set up accept button
        holder.btnAccept.setOnClickListener {
            if (holder.btnAccept.text == "Approve") {
                val visitorId = visitor.id
                if (visitorId != null) {
                    val visitorRef = FirebaseDatabase.getInstance().getReference("Visitors").child(visitorId)
                    visitorRef.child("approved").setValue("true")
                        .addOnSuccessListener {
                            Toast.makeText(context, "Visitor Approved", Toast.LENGTH_SHORT).show()
                            sendEmailWithQRCode(visitor) // Trigger email with QR code
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Approval Failed", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {

                val builder = AlertDialog.Builder(context)
                builder.setTitle("Reject Visitor")
                builder.setMessage("Are you sure you want to reject this visitor?")
                builder.setPositiveButton("Yes") { _, _ ->
                    val visitorId = visitor.id
                    if (visitorId != null) {
                        val visitorRef =
                            FirebaseDatabase.getInstance().getReference("Visitors").child(visitorId)
                        visitorRef.child("approved").setValue("false")
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Visitor's Approval Changed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Failed to change approval state",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }
        }

        holder.btnReject.visibility = View.GONE
        holder.btnReject.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Reject Visitor")
            builder.setMessage("Are you sure you want to reject this visitor?")
            builder.setPositiveButton("Yes") { _, _ ->
                val visitorId = visitor.id
                if (visitorId != null) {
                    val visitorRef = FirebaseDatabase.getInstance().getReference("Visitors").child(visitorId)
                    visitorRef.child("approved").setValue("false")
                        .addOnSuccessListener {
                            Toast.makeText(context, "Visitor Rejected", Toast.LENGTH_SHORT).show()
                            holder.approvedTextView.text = "Rejected"
                            holder.approvedTextView.setTextColor(ContextCompat.getColor(context, R.color.red))
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Rejection Failed", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            builder.setNegativeButton("No", null)
            builder.show()
        }

        if (userType=="Teacher" && visitor.approved=="false" && userStatusCheck==true){
            holder.btnDelete.visibility=View.VISIBLE
            holder.btnDelete.setOnClickListener {
                //delete the visitor details with the visitor id from realtime database and also delete the image in the storage
                val visitorId = visitor.id
                if (visitorId != null) {
                    // Reference to the visitor details in the Realtime Database
                    val visitorRef = FirebaseDatabase.getInstance().getReference("Visitors").child(visitorId)

                    // Reference to the visitor image in Firebase Storage
                    val imageRef = FirebaseStorage.getInstance().getReference("Visitors").child(visitorId)

                    // Show confirmation dialog
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Delete Visitor")
                    builder.setMessage("Are you sure you want to delete this visitor?")
                    builder.setPositiveButton("Yes") { _, _ ->
                        // Delete visitor details from Realtime Database
                        visitorRef.removeValue().addOnSuccessListener {
                            // Successfully deleted visitor details
                            Toast.makeText(context, "Visitor Deleted", Toast.LENGTH_SHORT).show()

                            // Delete visitor image from Firebase Storage
                            imageRef.delete().addOnSuccessListener {
                                // Successfully deleted image
                                Toast.makeText(context, "Visitor Image Deleted", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                // Failed to delete image
                                Toast.makeText(context, "Failed to delete visitor image", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener {
                            // Failed to delete visitor details
                            Toast.makeText(context, "Failed to delete visitor details", Toast.LENGTH_SHORT).show()
                        }
                    }
                    builder.setNegativeButton("No", null)
                    builder.show()
                }
            }
            //removing the delete button if the status is approved
            if (userType=="Teacher" || userType=="Guard" && visitor.approved=="true"){
                holder.btnDelete.visibility=View.GONE
            }
        }

    }


    override fun getItemCount(): Int {
        return visitors.size
    }

    // Method to update the list of visitors
    fun updateData(newVisitors: List<visitorDetails>) {
        this.visitors = newVisitors.toMutableList()
        notifyDataSetChanged()
    }

    //functions for QR code generation and sedning mail
    private fun generateQRCode(text: String): Bitmap? {
        val qrCodeWriter = QRCodeWriter()
        return try {
            val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun sendEmailWithQRCode(visitor: visitorDetails) {
        val db = FirebaseFirestore.getInstance()

        // Generate the QR Code
        val qrCodeBitmap = generateQRCode("Visitor ID: ${visitor.id}")

        if (qrCodeBitmap != null) {
            uploadQRCodeToFirebaseStorage(qrCodeBitmap, visitor.id.toString()) { qrCodeUrl ->
                if (qrCodeUrl != null) {
                    // Create HTML with QR code image URL
                    val htmlBody = """
                <p>Hello ${visitor.name}!</p>
                <p>This is your QR code for visitor access.</p>
                <img src="$qrCodeUrl" alt="QR Code">
                """

                    // Create data to be added to Firestore
                    val emailData = hashMapOf(
                        "to" to listOf(visitor.email),
                        "message" to mapOf(
                            "subject" to "Your Visitor QR Code",
                            "text" to "Please find your QR code attached.",
                            "html" to htmlBody
                        )
                    )

                    // Add data to Firestore with an auto-generated document ID
                    db.collection("mail")
                        .add(emailData)
                        .addOnSuccessListener { documentReference ->
                            // Document was added successfully
                            println("DocumentSnapshot added with ID: ${documentReference.id}")
                        }
                        .addOnFailureListener { e ->
                            // Handle failure
                            println("Error adding document: $e")
                        }
                } else {
                    println("Failed to upload QR Code to Firebase Storage")
                }
            }
        } else {
            println("Failed to generate QR Code")
        }
    }


    private fun uploadQRCodeToFirebaseStorage(bitmap: Bitmap, visitorId: String, callback: (String?) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference
        val qrCodeRef = storageReference.child("QRCodeImages/$visitorId.png")

        // Convert Bitmap to ByteArray
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()

        // Upload to Firebase Storage
        val uploadTask = qrCodeRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            qrCodeRef.downloadUrl.addOnSuccessListener { uri ->
                // Pass the download URL to the callback
                callback(uri.toString())
            }.addOnFailureListener {
                callback(null)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

}
