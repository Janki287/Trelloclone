package com.example.trelloclone.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.firestore.FireStoreClass
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constant
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.IOException

class ProfileActivity : BaseActivity() {

    private var mSelectedImageFromGallery : Uri? = null //this should be URI because we choose pic from gallery in uri form
    //this is image uri which we are selecting from the gallery
    private var mFireBaseStorageImage : String = "" //this should be string because we are using this variable to create board(database fire base)
    //where the image variable is in string
    //this is the uri of a image that we are store on fire base storage and in return it is giving the fire base storage uri(then we are converting that uri into string)

    private var mUserDetails : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setActionBar()

        FireStoreClass().loadUserDataAfterLoggedIn(this)

        civ_profile_activity.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED){
                //if permission is granted earlier then direct open the image chooser
                Constant.showImageChooserFromGallery(this)
            }else{
                //if permission is not granted(denied) then request the permission
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constant.READ_EXTERNAL_STORAGE_REQUEST_CODE)
            }
        }

        btn_update_profile_activity.setOnClickListener {
            if(mSelectedImageFromGallery != null){
                //if we select the image from gallery then we want to update
                //if we do not select the image from gallery then it is the default image that is stored on database(Fire base)
                storeImageOnFireBaseStorage()
                //here we are adding the new image(which we have selected from gallery) on fire base storage and updating user image on fire base database
            }else{
                showProgressDialog("Please Wait....")
                updateUserIntoFireBaseDatabase()
                //here we are only updating the user details(like name,mobile) on fire base database
                //here the image is not updated because we have not selected the new image(from gallery)
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constant.READ_EXTERNAL_STORAGE_REQUEST_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constant.showImageChooserFromGallery(this)
            }else{
                Toast.makeText(this,"You have just denied the permission",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == Constant.PICK_IMAGE_REQUEST_CODE && data!!.data !== null){
            mSelectedImageFromGallery = data!!.data
            try {
                Glide.with(this)
                    .load(mSelectedImageFromGallery) // this will take input of image as in URI form, so our image should be in URI form (web OR another URI)
                    .fitCenter()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(civ_profile_activity)
            }catch (e : IOException){
                println("Error::${e.printStackTrace()}")
            }
        }
    }

    private fun setActionBar(){
        setSupportActionBar(tb_profile_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "My Profile"
        }
        tb_profile_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setupProfileUI(user : User){

        mUserDetails = user

        Glide.with(this)
            .load(user.image) // this will take input of image as in URI form, so our image should be in URI form (web OR another URI)
            .fitCenter()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(civ_profile_activity)

        et_name_profile_activity.setText(user.name)
        et_email_profile_activity.setText(user.email)
        if(user.mobile != 0L){
            //this basically check if user has mobile number if not then it will not displayed anything
            et_mobile_profile_activity.setText(user.mobile.toString())
        }
    }

    private fun storeImageOnFireBaseStorage(){
        showProgressDialog("Please Wait...")

        if(mSelectedImageFromGallery != null){
            //basically we are selecting the image from gallery and we also want to update this image on database(fire store) and storage(fire base)
            //and also we store the image on fire base storage if select the from gallery, if we do not then the image is already stored on storage and database
            val srf : StorageReference = FirebaseStorage
                .getInstance()
                .reference.
                child("USER_PROFILE_IMAGE" + System.currentTimeMillis() + "." + Constant.getExtensionOfImageOrFile(this,mSelectedImageFromGallery))
            srf.putFile(mSelectedImageFromGallery!!).addOnSuccessListener {
                    taskSnapshot ->
                dismissProgressDialog()
                println("ID of image that is store on Storage(Fire base):::${taskSnapshot.metadata!!.reference!!.downloadUrl}")
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    println("actual uri of image that we can store(update) on fire base data base:::${uri}")
                    mFireBaseStorageImage = uri.toString()
                    updateUserIntoFireBaseDatabase()
                }
            }.addOnFailureListener{
                e ->
                dismissProgressDialog()
                println("Error:::${e.message}")
            }
        }
    }

    private fun updateUserIntoFireBaseDatabase(){
        val hashMap = HashMap<String,Any>()

        if(mFireBaseStorageImage.isNotEmpty() && mFireBaseStorageImage != mUserDetails!!.image){
            hashMap[Constant.IMAGE] = mFireBaseStorageImage
        }
        if(et_name_profile_activity.text.toString() != mUserDetails!!.name){
            hashMap[Constant.NAME] = et_name_profile_activity.text.toString()
        }
        if(et_mobile_profile_activity.text.toString() != mUserDetails!!.mobile.toString()){
            hashMap[Constant.MOBILE] = et_mobile_profile_activity.text.toString().toLong()
        }
        FireStoreClass().updateUserProfileData(this,hashMap)
    }

    fun profileUpdatedSuccessfully(){
        dismissProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}
