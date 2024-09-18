package com.example.cu

import android.os.Parcel
import android.os.Parcelable

data class visitorDetails(
    var id: String? = null,
    var uploaderID: String? = null,
    var approved: String? = null,
    var department: String? = null,
    var name: String? = null,
    val purpose: String? = null,
    val date: String? = null,
    val time: String? = null,
    val enteredThrough: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val assisted: String? = null,
    val details: String? = null,
    val status: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(uploaderID)
        parcel.writeString(approved)
        parcel.writeString(department)
        parcel.writeString(name)
        parcel.writeString(purpose)
        parcel.writeString(date)
        parcel.writeString(time)
        parcel.writeString(enteredThrough)
        parcel.writeString(phone)
        parcel.writeString(email)
        parcel.writeString(assisted)
        parcel.writeString(details)
        parcel.writeString(status)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<visitorDetails> {
        override fun createFromParcel(parcel: Parcel) = visitorDetails(parcel)
        override fun newArray(size: Int) = arrayOfNulls<visitorDetails>(size)
    }
}
