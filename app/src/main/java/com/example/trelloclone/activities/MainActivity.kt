package com.example.trelloclone.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.adapter.BoardItemsAdapter
import com.example.trelloclone.firestore.FireStoreClass
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constant
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_for_main_activity.*
import kotlinx.android.synthetic.main.header_layout_for_main_activity.*
import kotlinx.android.synthetic.main.main_activity_content.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mUserName : String = ""

    companion object{
        private const val MY_PROFILE_REQUEST_CODE : Int = 11
        private const val CREATE_BOARD_REQUEST_CODE : Int = 12
    }

    private lateinit var mSharedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nav_view_main_activity.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(Constant.TRELLO_CLONE_PREFS,Context.MODE_PRIVATE)

        val tokenUpdatedOrNot = mSharedPreferences.getBoolean(Constant.FCM_TOKEN_UPDATED,false)
        //first time when user logged in tokenUpdatedOrNot=false (by default)
        //but when user is already logged in ,it means tokenUpdatedOrNot=true
        if(tokenUpdatedOrNot){
            showProgressDialog("Please Wait...")
            FireStoreClass().loadUserDataAfterLoggedIn(this,true)
        }else{
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {instanceIdResult ->
                updateFcmTokenInFireStore(instanceIdResult.token)
            }
        }
        //basically we are check if user logged in first time then token will be updated using updateFcmTokenInFireStore()
        //and if user already logged in but made any changes in other activity then only load the user data using loadUserDataAfterLoggedIn()

        setActionBar()

        FireStoreClass().loadUserDataAfterLoggedIn(this,true)//this is first time user opening his account
        //so that board list should be refreshed(uploaded)

        fab_main_activity.setOnClickListener {
            val intent = Intent(this,CreateBoardActivity::class.java)
            intent.putExtra(Constant.NAME,mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

    }

    private fun setActionBar(){
        setSupportActionBar(tb_for_app_bar)
        tb_for_app_bar.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        tb_for_app_bar.setNavigationOnClickListener {
            toggleDrawer()
        }
        //this is same as setOnClickListener
        //setNavigationItemSelectedListener has also two versions like setOnClickListener
        //example: setNavigationItemSelectedListener( ) and setNavigationItemSelectedListener{ }
    }

    private fun toggleDrawer(){
        if(drawer_layout_for_main_activity.isDrawerOpen(GravityCompat.START)){
            drawer_layout_for_main_activity.closeDrawer(GravityCompat.START)
        }else{
            drawer_layout_for_main_activity.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        //if do not comment out super.onBackPressed() then it will call the normal backPressed,not this backPressed that we are going to prepare
        if(drawer_layout_for_main_activity.isDrawerOpen(GravityCompat.START)){
            drawer_layout_for_main_activity.closeDrawer(GravityCompat.START)
        }else{
            doubleBackExit()
        }
    }

    fun updateUserDetailsInHeaderLayout(user : User,boardListUpdate : Boolean){
        dismissProgressDialog()
        //otherwise progress dialog that is opened by another function will never be closed
        mUserName = user.name

        Glide.with(this)
            .load(user.image)
            .fitCenter()
            .placeholder(R.drawable.ic_user_place_holder) //if image is not there then it will be show this placeholder as a default image
            .into(civ_user_for_menu_main_activity) //and if there is a image then it will show the image
        tv_user_for_menu_main_activity.text = user.name

        if(boardListUpdate){
            showProgressDialog("Please Wait...")
            FireStoreClass().getBoardListFromFireStore(this)//we only want to update the board list recycler view if boardListUpdate=true
        }
    }

    fun populateBoardList(boardList : ArrayList<Board>){
        dismissProgressDialog()

        if(boardList.size > 0){
            rv_main_activity_content.visibility = View.VISIBLE
            tv_main_activity_content.visibility = View.GONE

            rv_main_activity_content.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            rv_main_activity_content.setHasFixedSize(true)
            //if we want to show the recycler view then we HAVE to assign LayoutManager to it
            //otherwise it will throw an error (E/RecyclerView: No layout manager attached; skipping layout)

            val boardAdapter = BoardItemsAdapter(this,boardList)
            rv_main_activity_content.adapter = boardAdapter

            boardAdapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constant.DOCUMENT_ID,model.boardID)
                    startActivity(intent)
                }
            })
        }else{
            tv_main_activity_content.visibility = View.VISIBLE
            rv_main_activity_content.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FireStoreClass().loadUserDataAfterLoggedIn(this)//here we are not giving boardListUpdate=true or false
            //because by default it is false
            //and we do not want to update the board list if user only changes his user name
            //we only want to change the user name in header layout(in drawer)
        }else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            FireStoreClass().getBoardListFromFireStore(this)
            //we are updating(getting) the board list after get into main activity from create board activity
        }else{
            Toast.makeText(this,"Cancelled the update task OR back pressed",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_drawer_my_profile -> {
                startActivityForResult(Intent(this,ProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.menu_drawer_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()
                //after current user logged out then it's token is erased (removed) from this mSharedPreferences variable
                //and next time if another user sign in then mSharedPreferences variable will get that user's fcm token

                val intent = Intent(this,IntroActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or  Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
                //we are finishing this main activity(User Board) so other user can sign in
                //and we are going to intro activity so other user can register or sign in
            }
        }
        drawer_layout_for_main_activity.closeDrawer(GravityCompat.START)
        //if user is clicking on other than this two options(my profile and sign out) then drawer will close
        return true
    }

    fun tokenUpdateSuccess(){
        dismissProgressDialog()
        val editor : SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constant.FCM_TOKEN_UPDATED,true)
        editor.apply()

        showProgressDialog("Please Wait...")
        FireStoreClass().loadUserDataAfterLoggedIn(this,true)
        //we get the update OR get the token successfully
        //so we have to load user data with updated token that is why we passing true in the loadUserDataAfterLoggedIn()
    }

    private fun updateFcmTokenInFireStore(token : String){
        val userHashMap = HashMap<String,Any>()
        userHashMap[Constant.FCM_TOKEN] = token

        showProgressDialog("Please Wait...")
        FireStoreClass().updateUserProfileData(this,userHashMap)
    }
}
