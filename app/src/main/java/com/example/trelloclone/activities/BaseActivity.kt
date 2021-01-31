package com.example.trelloclone.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.trelloclone.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.custom_progress_bar.*

open class BaseActivity : AppCompatActivity() {
    //we are making all these function here, so that we do not have to create them in other activities multiple times
    //example doubleBackExit(), showProgressDialog(), showProgressBar(), getCurrentUserID()
    //this all will be used multiple times, that is why we are only making it one time and inherit this base activity in all other activity

    private var mDoubleBackButtonPressedOnce : Boolean = false
    private lateinit var mDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    fun doubleBackExit(){
        if(mDoubleBackButtonPressedOnce){
            super.onBackPressed()
            //this super.OnBackPressed() function will exit the app OR will going back to previous activity
            return
        }
        mDoubleBackButtonPressedOnce = true
        Toast.makeText(this,"Press Back Button Again Quickly For Exit",Toast.LENGTH_SHORT).show()

        Handler().postDelayed({
            mDoubleBackButtonPressedOnce = false
        },2000)
    }

    fun showProgressDialog(text : String){
        mDialog = Dialog(this)
        mDialog.setContentView(R.layout.custom_progress_bar)
        mDialog.tv_custom_progress_bar_dialog.text = text
        mDialog.show()
    }

    fun dismissProgressDialog(){
        mDialog.dismiss()
    }

    fun getCurrentUserID() : String{
        return FirebaseAuth.getInstance().currentUser!!.uid
        //this will return the current user id who is currently logged into the app
    }

    fun showSnackBar(message : String){
        val snackBar = Snackbar.make(findViewById(android.R.id.content),message,Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this,R.color.snackBarColor))
        snackBar.show()
    }
}
