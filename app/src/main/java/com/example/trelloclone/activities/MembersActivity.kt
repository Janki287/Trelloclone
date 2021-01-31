package com.example.trelloclone.activities

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapter.MembersListItemsAdapter
import com.example.trelloclone.firestore.FireStoreClass
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constant
import kotlinx.android.synthetic.main.activity_members.*
import kotlinx.android.synthetic.main.activity_task_list.*
import kotlinx.android.synthetic.main.add_members_dialog_box.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MembersActivity : BaseActivity() {

    private lateinit var mBoardDetails : Board
    private lateinit var mAssignedUsersList : ArrayList<User>
    private var mAnyChangesMadeInMembersActivity : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        if(intent.hasExtra(Constant.BOARD_DETAILS)){
            mBoardDetails = intent.getParcelableExtra(Constant.BOARD_DETAILS)
        }
        setActionBar()
        showProgressDialog("Please Wait....")
        FireStoreClass().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)
    }

    private fun setActionBar(){
        setSupportActionBar(tb_members_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "Members"
        }
        tb_members_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_for_add_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.menu_for_add_members ->{
                addMembersDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setupMembersList(list : ArrayList<User>){
        mAssignedUsersList = list
        dismissProgressDialog()

        rv_members_list.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        rv_members_list.setHasFixedSize(true)

        val adapter = MembersListItemsAdapter(this,list)
        rv_members_list.adapter = adapter
    }

    private fun addMembersDialog(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.add_members_dialog_box)
        dialog.tv_add_members_button.setOnClickListener {
            val email = dialog.et_add_members_by_email.text.toString()
            if(email.isNotEmpty()){
                dialog.dismiss()
                showProgressDialog("Please Wait...")
                FireStoreClass().getMembersFromEmailID(this,email)
            }else{
                Toast.makeText(this,"Please Enter A Email Address",Toast.LENGTH_SHORT).show()
            }
        }
        dialog.tv_cancel_add_members_button.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun getMembersFromEmail(user : User){
        mBoardDetails.assignedTo.add(user.id)
        FireStoreClass().assignNewAddedMemberToBoard(this,mBoardDetails,user)
    }

    fun memberAssignedSuccess(user : User){
        dismissProgressDialog()
        mAssignedUsersList.add(user)
        mAnyChangesMadeInMembersActivity = true
        setupMembersList(mAssignedUsersList)

        //after assigning the new user X successfully in the board's members list,we are sending notification to the user X
        SendNotificationToUserAsyncTask(mBoardDetails.name,user.fcmToken).execute()
        //for working of the AsyncTask we have to execute() it,otherwise it will not work
    }

    override fun onBackPressed() {
        if(mAnyChangesMadeInMembersActivity){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    private inner class SendNotificationToUserAsyncTask(val boardName : String,val token : String) : AsyncTask<Any,Void,String>(){
        //we are creating this class here because whenever new members assign to the board OR members list, they will get a notification
        //and notification is a background process that is why we are using AsyncTask here
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog("Please Wait...")
        }

        override fun doInBackground(vararg params: Any?): String {
            var result : String =""
            var connection : HttpsURLConnection? = null
            try {
                val url = URL(Constant.FCM_BASE_URL)
                connection = url.openConnection() as HttpsURLConnection
                connection.doInput = true
                connection.doOutput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type","application/json")
                connection.setRequestProperty("charset","utf-8")
                connection.setRequestProperty("Accept","application/json")
                //whenever we add the user X in the members list,we are telling this to firebase server
                //and firebase server then send notification to user X
                //so basically we are sending data to the firebase server

                connection.setRequestProperty(Constant.FCM_AUTHORIZATION,"${Constant.FCM_KEY}=${Constant.FCM_SERVER_KEY}")

                connection.useCaches = false

                val wr = DataOutputStream(connection.outputStream)
                //we are sending data to the server so we have to do dataOutputStream
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constant.FCM_KEY_TITLE,"Assigned to the board:: $boardName")
                dataObject.put(Constant.FCM_KEY_MESSAGE,"You have been assigned to the board by ${mAssignedUsersList[0].name}")
                //${mAssignedUsersList[0].name} it means the creator of the board

                jsonRequest.put(Constant.FCM_KEY_DATA,dataObject)
                jsonRequest.put(Constant.FCM_KEY_TO,token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult : Int = connection.responseCode
                //we are sending data to the server,so anything could go wrong,so we have to check the response from the firebase server
                //it is OK(200) or NOT_FOUND(404)
                if(httpResult == HttpURLConnection.HTTP_OK){
                    val inputStream = connection.inputStream
                    //we are having response(data) from the firebase server,so we have to use the inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val sb = StringBuilder()
                    var line : String?
                    try {
                        while (reader.readLine().also { line=it } != null){
                            sb.append(line+"\n")
                        }
                    }catch (e : IOException){
                        e.printStackTrace()
                    }finally {
                        try {
                            inputStream.close()
                        }catch (e : IOException){
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                }else{
                    result = connection.responseMessage
                }

            }catch (e : SocketTimeoutException){
                result = "Connection Time Out"
            }catch (e : Exception){
                result = "Error:::${e.message}"
            }finally {
                connection?.disconnect()
                //if connection exists then disconnect it,we do not want connection to remain open
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            dismissProgressDialog()
            println("RESULT:::$result")
        }

    }
}
