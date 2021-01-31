package com.example.trelloclone.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.firestore.FireStoreClass
import com.example.trelloclone.models.Board
import com.example.trelloclone.utils.Constant
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create_board.*
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private var mSelectedImageFromGalleryForBoard : Uri? = null //this should be URI because we choose pic from gallery in uri form

    private lateinit var mUserName : String
    private var mImageUriOfBoard : String = "" //this should be string because we are using this variable to create board(database fire base)
    //where the image variable is in string

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setActionBar()

        if(intent.hasExtra(Constant.NAME)){
            mUserName = intent.getStringExtra(Constant.NAME)
        }

        civ_create_board_activity.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED){
                //if permission is granted earlier then direct open the image chooser
                Constant.showImageChooserFromGallery(this)
            }else{
                //if permission is not granted(denied) then request the permission
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constant.READ_EXTERNAL_STORAGE_REQUEST_CODE)
            }
        }

        btn_create_board_activity.setOnClickListener {
            if(mSelectedImageFromGalleryForBoard != null){
                uploadBoardImageIntoFireBaseStorage()
                //this block is for creating board in the database as well as to store the image on fire base storage that we are selected from gallery
            }else{
                showProgressDialog("Please Wait...")
                createBoardIntoFireBaseDatabase()
                //this block is for only creating board in the database
                //here we are not selecting image from the gallery so no need to store empty image on fire base storage
            }
        }
    }

    private fun setActionBar(){
        setSupportActionBar(tb_create_board_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "Create New Board"
        }
        tb_create_board_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun createBoardIntoFireBaseDatabase(){
        val assignedTo : ArrayList<String> = ArrayList<String>()
        assignedTo.add(getCurrentUserID())

        val board = Board(
            et_name_create_board_activity.text.toString(),
            mImageUriOfBoard,
            mUserName,
            assignedTo)
        FireStoreClass().createNewBoardIntoFireStore(this,board)
    }

    private fun uploadBoardImageIntoFireBaseStorage(){
        showProgressDialog("Please Wait...")

        if(mSelectedImageFromGalleryForBoard != null){
            //basically we are selecting the image from gallery and we also want to update this image on database(fire store) and storage(fire base)
            //and also we store the image on fire base storage if select the from gallery, if we do not then the image is already stored on storage and database
            val srf : StorageReference = FirebaseStorage
                .getInstance()
                .reference.
                child("BOARD_IMAGE" + System.currentTimeMillis() + "." + Constant.getExtensionOfImageOrFile(this,mSelectedImageFromGalleryForBoard))
            srf.putFile(mSelectedImageFromGalleryForBoard!!).addOnSuccessListener {
                    taskSnapshot ->
                println("ID of image that is store on Storage(Fire base):::${taskSnapshot.metadata!!.reference!!.downloadUrl}")
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                    println("actual uri of image that we can store(update) on fire base data base:::${uri}")
                    mImageUriOfBoard = uri.toString()
                    createBoardIntoFireBaseDatabase()
                }
            }.addOnFailureListener{
                    e ->
                dismissProgressDialog()
                println("Error:::${e.message}")
            }
        }
    }

    fun boardCreatedSuccessfully(){
        dismissProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constant.READ_EXTERNAL_STORAGE_REQUEST_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constant.showImageChooserFromGallery(this)
            }else{
                Toast.makeText(this,"You have just denied the permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == Constant.PICK_IMAGE_REQUEST_CODE && data!!.data !== null){
            mSelectedImageFromGalleryForBoard = data!!.data
            try {
                Glide.with(this)
                    .load(mSelectedImageFromGalleryForBoard) // this will take input of image as in URI form, so our image should be in URI form (web OR another URI)
                    .fitCenter()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(civ_create_board_activity)
            }catch (e : IOException){
                println("Error::${e.printStackTrace()}")
            }
        }
    }
}
