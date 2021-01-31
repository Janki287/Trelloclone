package com.example.trelloclone.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapter.AssignedMembersListAdapter
import com.example.trelloclone.dialogs.AssignedMembersListDialog
import com.example.trelloclone.dialogs.ColorLabelListDialog
import com.example.trelloclone.firestore.FireStoreClass
import com.example.trelloclone.models.*
import com.example.trelloclone.utils.Constant
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.layout_for_select_label_color.*
import kotlinx.android.synthetic.main.rv_for_label_color_items.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private lateinit var mBoardDetails : Board

    private var mTaskPosition : Int = -1
    private var mCardPosition : Int = -1

    private var mSelectedColor : String = ""

    private lateinit var mAssignedMembersList : ArrayList<User>

    private var mDueDateInMilliSeconds: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()
        setActionBar()

        et_card_name_card_details.setText(mBoardDetails.taskList[mTaskPosition].cards[mCardPosition].name)
        et_card_name_card_details.setSelection(et_card_name_card_details.text.toString().length)
        //above line will assign CURSOR to the end of the card name in edit text
        //in other words focus will directly go to the end of the card name in edit text

        mSelectedColor = mBoardDetails.taskList[mTaskPosition].cards[mCardPosition].color
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }//here we are getting the color that stored in database and in below functions( setColor() ) we are setting new color

        mDueDateInMilliSeconds = mBoardDetails.taskList[mTaskPosition].cards[mCardPosition].dueDate
        if(mDueDateInMilliSeconds > 0L){
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val date = sdf.format(Date(mDueDateInMilliSeconds))
            tv_select_due_date.text = date
        }//here we are getting the date from database and in below functions( setDatePickerDialog() ) we are setting new date

        tv_select_due_date.setOnClickListener {
            setDatePickerDialog()
        }

        tv_select_color.setOnClickListener {
            colorLabelListDialog()
        }

        tv_select_members.setOnClickListener {
            assignedMembersListDialog()
        }

        btn_update_card_details.setOnClickListener {
            if(et_card_name_card_details.text.toString().isNotEmpty()){
                updateCardDetails()
            }else{
                Toast.makeText(this,"Please enter a card name",Toast.LENGTH_SHORT).show()
            }
        }

        setupAssignedMembersRecyclerView()

    }

    private fun setActionBar(){
        setSupportActionBar(tb_card_details_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.taskList[mTaskPosition].cards[mCardPosition].name
        }
        tb_card_details_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun getIntentData(){
        if(intent.hasExtra(Constant.BOARD_DETAILS)){
            mBoardDetails = intent.getParcelableExtra(Constant.BOARD_DETAILS)
        }
        if(intent.hasExtra(Constant.TASK_LIST_ITEM_POSITION)){
            mTaskPosition = intent.getIntExtra(Constant.TASK_LIST_ITEM_POSITION,-1)  //default value is necessary
            //because this position is array based so if we get nothing then array index(position) is default -1
        }
        if(intent.hasExtra(Constant.CARD_LIST_ITEM_POSITION)){
            mCardPosition = intent.getIntExtra(Constant.CARD_LIST_ITEM_POSITION,-1) //default value is necessary
            //because this position is array based so if we get nothing then array index(position) is default -1
        }
        if(intent.hasExtra(Constant.ASSIGNED_MEMBERS_LIST)){
            mAssignedMembersList = intent.getParcelableArrayListExtra(Constant.ASSIGNED_MEMBERS_LIST)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.delete_card_menu_for_card_details_activity,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.delete_icon_for_card_details -> {
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskPosition].cards[mCardPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun updateOrDeleteCardSuccess(){
        dismissProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
        //we want to finish this activity after deleting card
        //so that we can go back to task list activity with updated tasks and deleted cards
    }

    private fun updateCardDetails(){
        val card = Cards(et_card_name_card_details.text.toString(),
        mBoardDetails.taskList[mTaskPosition].cards[mCardPosition].createdBy,
        mBoardDetails.taskList[mTaskPosition].cards[mCardPosition].assignedTo,
        mSelectedColor,mDueDateInMilliSeconds)

        val taskList : ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(mBoardDetails.taskList.size - 1)
        //TODO why we are adding above line (because when we update the card in card details activity then ADD TASK is also added in database)
        //we do not want this to happen so that is why we have to delete the last entry
        //because this function also calling the addOrUpdateTaskListIntoFireStore() function and if we do not write the above line
        //then ADD TASK will be also added in the database

        mBoardDetails.taskList[mTaskPosition].cards[mCardPosition] = card

        showProgressDialog("Please Wait...")
        FireStoreClass().addOrUpdateTaskListIntoFireStore(this,mBoardDetails)
    }

    private fun deleteCard(){
        val cardList = mBoardDetails.taskList[mTaskPosition].cards
        cardList.removeAt(mCardPosition)

        val taskList = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        //TODO why we are doing above line (because of same reason as deleting OR updating task in TaskListActivity)

        taskList[mTaskPosition].cards = cardList
        //updating task list with new card list where card at mCardPosition is deleted
        //as we can not make any changes to card(because they are part of TASKs) that is why we also have to update the task list

        showProgressDialog("Please Wait...")
        FireStoreClass().addOrUpdateTaskListIntoFireStore(this,mBoardDetails)
        //as we deleted the card from task list, we also want to update the task list activity

    }

    private fun alertDialogForDeleteCard(cardName : String){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Alert")
        alertDialog.setIcon(R.drawable.ic_announcement_black_24dp)
        alertDialog.setMessage("Are You Sure You want to delete a card :: $cardName ?")
        alertDialog.setPositiveButton("YES") { dialog, _ ->
            dialog.dismiss()
            deleteCard()
        }
        alertDialog.setNegativeButton("NO") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = alertDialog.create()
        dialog.setCancelable(false) //user can not close this alert dialog by touching outside
        dialog.show()
    }

    private fun colorList() : ArrayList<String>{
        val colorList : ArrayList<String> = ArrayList()
        colorList.add("#43C86F")
        colorList.add("#0C90F1")
        colorList.add("#F72400")
        colorList.add("#7A8089")
        colorList.add("#D57C1D")
        colorList.add("#770000")
        colorList.add("#0022F8")
        return colorList
    }

    private fun setColor(){
        tv_select_color.text = ""
        tv_select_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun colorLabelListDialog(){
        val colorList : ArrayList<String> = colorList()

        val colorListDialog = object : ColorLabelListDialog(this,colorList,"Select Color",mSelectedColor){
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        colorListDialog.show()
    }

    private fun assignedMembersListDialog(){
        val cardAssignedMembers = mBoardDetails.taskList[mTaskPosition].cards[mCardPosition].assignedTo

        if(cardAssignedMembers.size > 0){
            for(i in cardAssignedMembers){
                for(j in mAssignedMembersList.indices){
                    if(i == mAssignedMembersList[j].id){
                        mAssignedMembersList[j].selected = true
                    }
                }
            }
        }else{
            for(i in mAssignedMembersList.indices){
                mAssignedMembersList[i].selected = false
            }
        }

        val dialog = object : AssignedMembersListDialog(this,"Select Member",mAssignedMembersList){
            override fun onItemSelected(model: User, action: String) {
                if(action == Constant.SELECT_MEMBER_FOR_CARD){
                    if(!mBoardDetails.taskList[mTaskPosition].cards[mCardPosition].assignedTo.contains(model.id)){
                        mBoardDetails.taskList[mTaskPosition].cards[mCardPosition].assignedTo.add(model.id)
                    }
                }else{
                    mBoardDetails.taskList[mTaskPosition].cards[mCardPosition].assignedTo.remove(model.id)

                    for(i in mAssignedMembersList.indices){
                        if(mAssignedMembersList[i].id == model.id){
                            mAssignedMembersList[i].selected = false
                        }
                    }
                }
                setupAssignedMembersRecyclerView()//we are refreshing the view after adding or deleting the members
            }
        }
        dialog.show()
    }

    private fun setupAssignedMembersRecyclerView(){
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskPosition].cards[mCardPosition].assignedTo

        val selectedMembersList : ArrayList<SelectedMembers> = ArrayList()

        if(cardAssignedMembersList.size > 0){
            for(i in cardAssignedMembersList){
                for(j in mAssignedMembersList.indices){
                    if(i == mAssignedMembersList[j].id){
                        val selectedMembers = SelectedMembers(mAssignedMembersList[j].id,mAssignedMembersList[j].image)
                        selectedMembersList.add(selectedMembers)
                    }
                }
            }
        }

        if(selectedMembersList.size > 0){
            selectedMembersList.add(SelectedMembers("",""))
            //TODO why we are adding above line(to add back the plus sign at the end of the members images in recycler view)
            tv_select_members.visibility = View.GONE
            //TODO why we are adding above line(if there is a item in recycler view then we want to show directly recycler view)
            //if there is no item in recycler view then we want to show the text view(select members)
            //and add the assigned members to the list

            rv_for_assigned_members_card_details_activity.visibility = View.VISIBLE
            rv_for_assigned_members_card_details_activity.layoutManager = GridLayoutManager(this,6)
            rv_for_assigned_members_card_details_activity.setHasFixedSize(true)

            val adapter = AssignedMembersListAdapter(this,selectedMembersList,true)
            //true = because we want to show the plus (blue add button) icon in the recycler view
            rv_for_assigned_members_card_details_activity.adapter = adapter

            adapter.setOnClickListener(object : AssignedMembersListAdapter.OnClickListener{
                override fun onClick() {
                    assignedMembersListDialog()
                }
            })
        }else{
            tv_select_members.visibility = View.VISIBLE
            //TODO why we are adding above line(if there is a item in recycler view then we want to show directly recycler view)
            //if there is no item in recycler view then we want to show the text view(select members)
            //and add the assigned members to the list
            rv_for_assigned_members_card_details_activity.visibility = View.GONE
        }
    }

    private fun setDatePickerDialog(){
        val calender = Calendar.getInstance()
        val mYear = calender.get(Calendar.YEAR)
        val mMonth = calender.get(Calendar.MONTH)
        val mDayOfMonth = calender.get(Calendar.DAY_OF_MONTH)
        //these three variables are for to get the current date from the laptop(machine)

        val dpd = DatePickerDialog(this,DatePickerDialog.OnDateSetListener{ _, year, month, dayOfMonth ->
            //in this year, month, dayOfMonth three variables the selected date is stored (the date that we are select from calender)
            val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
            val sMonth = if ((month +  1) < 10) "0${month + 1}" else "${month + 1}"
            //because Month in the android starts from 0 (Example january=0, february=1)

            val selectedDate = "$sDayOfMonth/${sMonth}/$year"
            tv_select_due_date.text = selectedDate

            val sdf = SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH)    //or Locale.getDefault()
            val date = sdf.parse(selectedDate)  //this will convert string date into actual DATE object
            mDueDateInMilliSeconds = date.time
        },mYear,mMonth,mDayOfMonth)

        dpd.show()
    }
}
