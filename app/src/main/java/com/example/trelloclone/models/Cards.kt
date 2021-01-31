package com.example.trelloclone.models

import android.os.Parcel
import android.os.Parcelable

data class Cards(
    val name : String = "",
    val createdBy : String = "",
    val assignedTo : ArrayList<String> = ArrayList(),
    val color : String = "",
    val dueDate : Long = 0L
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readLong()!!
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(createdBy)
        parcel.writeStringList(assignedTo)
        parcel.writeString(color)
        parcel.writeLong(dueDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Cards> {
        override fun createFromParcel(parcel: Parcel): Cards {
            return Cards(parcel)
        }

        override fun newArray(size: Int): Array<Cards?> {
            return arrayOfNulls(size)
        }
    }
}