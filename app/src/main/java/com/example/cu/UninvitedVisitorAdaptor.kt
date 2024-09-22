package com.example.cu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UninvitedVisitorAdaptor(private val context: Context, private var visitors: MutableList<uninvitedDetails>) :
    RecyclerView.Adapter<UninvitedVisitorAdaptor.ViewHolder>() {


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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_uninvited_visitor_adaptor, parent, false)
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
    }


    override fun getItemCount(): Int {
        return visitors.size
    }

    // Method to update the list of visitors
    fun updateData(newVisitors: List<uninvitedDetails>) {
        this.visitors = newVisitors.toMutableList()
        notifyDataSetChanged()
    }

}
