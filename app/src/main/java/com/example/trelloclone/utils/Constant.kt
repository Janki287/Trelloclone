package com.example.trelloclone.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.example.trelloclone.activities.ProfileActivity

object Constant {

    const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2

    const val USERS : String = "USERS" //for USERS collection in fire base database
    const val BOARD : String = "BOARD" //for BOARD collection in fire base database

    const val IMAGE : String = "image"
    const val MOBILE : String = "mobile"
    const val NAME : String = "name"
    const val ASSIGNED_TO : String = "assignedTo"
    const val DOCUMENT_ID : String = "documentId"
    const val TASK_LIST : String = "taskList"
    const val BOARD_DETAILS : String = "board_details"
    const val ID : String = "id"
    const val EMAIL : String = "email"

    const val TASK_LIST_ITEM_POSITION : String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION : String = "card_list_item_position"

    const val ASSIGNED_MEMBERS_LIST : String = "assigned_members_list"

    const val SELECT_MEMBER_FOR_CARD : String = "select"
    const val UNSELECT_MEMBER_FOR_CARD : String = "unselect"

    const val TRELLO_CLONE_PREFS : String = "trello_clone_prefs"
    const val FCM_TOKEN_UPDATED : String = "Fcm_token_updated"
    const val FCM_TOKEN : String = "fcmToken"
    //fcmToken will be same as we defined in our USERS data class otherwise it will create new variable in database

    const val FCM_BASE_URL : String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION : String = "authorization"
    const val FCM_KEY : String = "key"
    const val FCM_SERVER_KEY : String = "AAAAk41PorY:APA91bFoxMmYaoGED4R847r80tCbHBB5tSVBq_23JwwSMRc17Q0Dhnq_HXmXkRYeny3Je0h8ZxOSGa7nPwy-0YInMmSckha522gm1oZP0qL3o0NKWSCSoVjQwCoeQVPjmxA5UVJbMWY6"
    const val FCM_KEY_TITLE  :String ="title"
    const val FCM_KEY_MESSAGE : String = "message"
    const val FCM_KEY_DATA : String = "data"
    const val  FCM_KEY_TO : String = "to"

    fun showImageChooserFromGallery(activity : Activity){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getExtensionOfImageOrFile(activity: Activity,uri : Uri?) : String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
        //this will basically return the type of image or another file(.png, .jpeg)
    }
}