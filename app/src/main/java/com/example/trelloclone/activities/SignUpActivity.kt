package com.example.trelloclone.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.trelloclone.R
import com.example.trelloclone.firestore.FireStoreClass
import com.example.trelloclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setupActionBar()

        btn_sign_up_activity.setOnClickListener {
            registerUser()
        }
    }
    private fun setupActionBar() {
        setSupportActionBar(tb_sign_up_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_button_24dp)
            actionBar.title = "Sign Up"
        }
        tb_sign_up_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun userRegisterIntoFireStoreSuccess(){
        dismissProgressDialog()
        Toast.makeText(this,"Welcome , You have registered successfully",Toast.LENGTH_SHORT).show()
        FirebaseAuth.getInstance().signOut()
        finish()
        //here we are finish this activity, so that we are going to intro activity where user can sign in
    }

    private fun registerUser(){
        val name = et_sign_up_name.text.toString().trim { it <= ' '}
        val email = et_sign_up_email.text.toString().trim { it <= ' ' }
        val password = et_sign_up_password.text.toString().trim { it <= ' ' }

        if(validateUserDetails(name, email, password)){
            showProgressDialog("Please Wait!!")
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                task ->
                if (task.isSuccessful){
                    val fireBaseUser : FirebaseUser? = task.result!!.user
                    if(fireBaseUser != null){
                        val registeredEmail = fireBaseUser.email
                        val user = User(fireBaseUser.uid,name,registeredEmail!!)
                        //fireBaseUser.uid will get us the Unique user id of registered user
                        //first this block will create new user in Authentication section of Firebase
                        FireStoreClass().registerUserIntoFireStore(this,user)
                        //FireStoreClass().registerUserIntoFireStore this function will create new user in database(FireStore)

                    }
                }else{
                    dismissProgressDialog()
                    Toast.makeText(this,"Registration Failed",Toast.LENGTH_SHORT).show()
                    println("Error:::${task.exception!!.message}")
                }
            }
        }
    }

    private fun validateUserDetails(name : String,email : String, password : String) : Boolean{
        return when{
            TextUtils.isEmpty(name) -> {
                //this block will run if name is empty
                showSnackBar("Please enter your name")
                false
            }
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
