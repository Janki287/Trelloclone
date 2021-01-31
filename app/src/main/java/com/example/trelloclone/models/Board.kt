package com.example.trelloclone.models

import android.os.Parcel
import android.os.Parcelable

data class Board(
    val name : String = "",
    val image : String = "",
    val createdBy : String = "",
    //this two are string because user UID is in string form
    val assignedTo : ArrayList<String> = ArrayList(),
    //this is array list because we can assign multiple people to it
    var boardID : String = "",  //which is also string
    val taskList : ArrayList<Task> = ArrayList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!, //we have to add this line manually
        parcel.createTypedArrayList(Task.CREATOR)!!
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(createdBy)
        parcel.writeStringList(assignedTo) // ww have to add this line manually
        parcel.writeString(boardID)
        parcel.writeTypedList(taskList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Board> {
        override fun createFromParcel(parcel: Parcel): Board {
            return Board(parcel)
        }

        override fun newArray(size: Int): Array<Board?> {
            return arrayOfNulls(size)
        }
    }
}