package com.example.cu

import android.os.Parcel
import android.os.Parcelable

class uninvitedDetails (
    var id: String? = null,
    var uploaderID: String? = null,
    var name: String? = null,
    val purpose: String? = null,
    var vehicle: String? = null,
    var number: String? = null,
    val date: String? = null,
    val time: String? = null,
    val phone: String? = null,
    val assisted: String? = null,

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
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(uploaderID)
        parcel.writeString(vehicle)
        parcel.writeString(number)
        parcel.writeString(name)
        parcel.writeString(purpose)
        parcel.writeString(date)
        parcel.writeString(time)
        parcel.writeString(phone)
        parcel.writeString(assisted)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<uninvitedDetails> {
        override fun createFromParcel(parcel: Parcel) = uninvitedDetails(parcel)
        override fun newArray(size: Int) = arrayOfNulls<uninvitedDetails>(size)
    }
}
