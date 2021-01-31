package com.example.trelloclone.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.trelloclone.R
import com.example.trelloclone.firestore.FireStoreClass
import com.example.trelloclone.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setupActionBar()

        auth = FirebaseAuth.getInstance()

        btn_sign_in_activity.setOnClickListener {
            signInWithRegisteredEmail()
        }
    }
    private fun setupActionBar(){
        setSupportActionBar(tb_sign_in_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_button_24dp)
            actionBar.title = "Sign In"
        }
        tb_sign_in_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    fun userSignIntoFireStoreSuccess(user : User){
        dismissProgressDialog()
        Toast.makeText(this,"Sign In Success",Toast.LENGTH_SHORT).show()
        println("The user is $user")
        startActivity(Intent(this,MainActivity::class.java))
        finish()
        //we are finish this activity and goes into our main activity OR user board
    }

    private fun signInWithRegisteredEmail(){
        val email = et_sign_in_email.text.toString().trim { it <= ' ' }
        val password = et_sign_in_password.text.toString().trim { it <= ' ' }

        if(validateUserDetails(email, password)){
            showProgressDialog("Please Wait")
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                task ->
                if(task.isSuccessful){
                    //this will only authenticate the user and return the user email ID and its user ID
                    //val user = auth.currentUser
                    //This line of code is now not necessary
                    FireStoreClass().loadUserDataAfterLoggedIn(this)
                }else{
                    dismissProgressDialog()
                    Toast.makeText(this,"Authentication Failed",Toast.LENGTH_SHORT).show()
                    println("Error:::${task.exception!!.message}")
                    //this will check the email and password and authenticate them and if they are wrong then it will throw error
                }
            }
        }
    }

    private fun validateUserDetails(email : String, password : String) : Boolean{
        return when{
            TextUtils.isEmpty(email) -> {
                //this block will run if email is empty
                showSnackBar("Please enter your email")
                false
            }
            TextUtils.isEmpty(password) -> {
                //this block will run if password is empty
                showSnackBar("Please enter your password")
                false
            }
            else -> {
                //this block will run if all fields are filled
                true
            }
        }
    }
}
