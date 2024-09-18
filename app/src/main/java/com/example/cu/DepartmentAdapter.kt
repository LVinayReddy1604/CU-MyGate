package com.example.cu

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter class for displaying departments
class DepartmentAdapter(
    private val departments: List<Department>,
    private val onItemClick: (Department) -> Unit // Required callback for item clicks
) : RecyclerView.Adapter<DepartmentAdapter.ViewHolder>() {

    // ViewHolder class for holding department item views
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val departmentName: TextView = view.findViewById(R.id.departmentName)
        val departmentImage: ImageView = view.findViewById(R.id.departmentImage)
        val departmentCount: TextView = view.findViewById(R.id.departmentCount)
    }

    // Inflate the layout for each department item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_department, parent, false)
        return ViewHolder(view)
    }

    // Bind department data to the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val department = departments[position]
        Log.d("DepartmentAdapter", "Binding department: ${department.name}, Icon ID: ${department.iconResId}")

        holder.departmentName.text = department.name
        holder.departmentImage.setImageResource(department.iconResId)
        holder.departmentCount.text = department.count.toString()

        // Set the click listener for the item
        holder.itemView.setOnClickListener {
            onItemClick(department)
        }
    }

    // Return the total number of items
    override fun getItemCount(): Int = departments.size
}
