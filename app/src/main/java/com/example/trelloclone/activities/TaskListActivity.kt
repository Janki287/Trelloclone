package com.example.trelloclone.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapter.TaskListItemsAdapter
import com.example.trelloclone.firestore.FireStoreClass
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.Cards
import com.example.trelloclone.models.Task
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constant
import kotlinx.android.synthetic.main.activity_task_list.*

class TaskListActivity : BaseActivity() {

    private lateinit var mBoardDetails : Board
    private lateinit var mBoardId : String
    lateinit var mAssignedToMembersList : ArrayList<User>
    //we have make this object open so that we can use it in the cardItemsAdapter

    companion object{
        const val MEMBERS_REQUEST_CODE : Int = 13
        const val CARD_DETAILS_REQUEST_CODE : Int = 14
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if(intent.hasExtra(Constant.DOCUMENT_ID)){
            mBoardId = intent.getStringExtra(Constant.DOCUMENT_ID)
        }

        showProgressDialog("Please Wait...")
        FireStoreClass().getBoardDetailsFromFireStore(this,mBoardId)

    }

    private fun setActionBar(){
        setSupportActionBar(tb_task_list_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name
        }
        tb_task_list_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?) : Boolean {
        when(item!!.itemId) {
            R.id.action_menu_members -> {
                val intent = Intent(this,MembersActivity::class.java)
                intent.putExtra(Constant.BOARD_DETAILS,mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE){
            showProgressDialog("Please Wait...")
            FireStoreClass().getBoardDetailsFromFireStore(this,mBoardId)
            //the board details will only change if we make any changes on the members activity
            //(mAnyChangesMadeInMembersActivity = true)
        }else if(resultCode == Activity.RESULT_OK && requestCode == CARD_DETAILS_REQUEST_CODE){
            showProgressDialog("Please Wait...")
            FireStoreClass().getBoardDetailsFromFireStore(this,mBoardId)
            //the board details will only change if we make any changes on the card details activity also like above if statement
            // ( mAnyChangesMadeInMembersActivity = true)
        }else{
            Toast.makeText(this,"Cancelled OR Back Pressed",Toast.LENGTH_SHORT).show()
            //mAnyChangesMadeInMembersActivity = false then simple back pressed will work and this toast will be shown
        }
    }

    fun getBoardDetailsFromFireStore(board : Board){

        mBoardDetails = board

        dismissProgressDialog()
        setActionBar()

//        val task = Task("ADD TASK")
//        board.taskList.add(task)
//        //TODO what are this two for???
//
//        rv_for_tasks_task_list_activity.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
//        rv_for_tasks_task_list_activity.setHasFixedSize(true)
//
//        val taskListItemsAdapter = TaskListItemsAdapter(this,board.taskList)
//        rv_for_tasks_task_list_activity.adapter = taskListItemsAdapter
        //TODO why we cut the above code and paste it in the getAssignedMembersList() function

        showProgressDialog("Please Wait...")
        FireStoreClass().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)
    }

    fun addOrUpdateTaskListSuccess(){
        dismissProgressDialog()
        //here we are closing one progress dialog and opening another
        //because one progress dialog is for adding or updating task list
        //where another one is for updating board details again(because we add new task to a task list and task list is inside the board,so we also have to update the board again)
        //so basically two progress dialog for two different operations
        showProgressDialog("Please Wait...")
        FireStoreClass().getBoardDetailsFromFireStore(this,mBoardDetails.boardID )
    }

    fun createNewTask(taskName : String){
        val task = Task(taskName,FireStoreClass().getCurrentUserID())
        mBoardDetails.taskList.add(0,task)

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        //TODO why we are doing above sentence???
        // (here we are removing `val task = Task("ADD TASK")` and `mBoardDetails.taskList.add(task)` from task list

        showProgressDialog("Please Wait...")
        FireStoreClass().addOrUpdateTaskListIntoFireStore(this,mBoardDetails)
    }

    fun updateTaskList(position : Int,name : String,model : Task){
        val task = Task(name,model.createdBy)

        mBoardDetails.taskList[position] = task
        //for update the task
        //mBoardDetails.taskList.add(position,task) if we do this then it will create new task with edited name and old(un updated) task will also exist

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        //TODO why we are doing above sentence???

        showProgressDialog("Please Wait...")
        FireStoreClass().addOrUpdateTaskListIntoFireStore(this,mBoardDetails)
    }

    fun deleteTaskFromTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        //TODO why we are doing above sentence???

        showProgressDialog("Please Wait...")
        FireStoreClass().addOrUpdateTaskListIntoFireStore(this,mBoardDetails)
    }

    fun addCardToTaskListAtPosition(position: Int,name: String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        //TODO why we are doing above sentence???
        //TODO above sentence is use to remove `val task = Task("ADD TASK")` and `mBoardDetails.taskList.add(task)` from task list
        // because every time we call `FireStoreClass().addOrUpdateTaskListIntoFireStore(this,mBoardDetails)` this function
        //TODO it also call `getBoardDetailsFromFireStore(board : Board)` this function and `val task = Task("ADD TASK")`
        // this will be added, so to remove `val task = Task("ADD TASK")` this we are doing above line
        //same for others TODO in above's functions

        val cardAssignedTo : ArrayList<String> = ArrayList()
        cardAssignedTo.add(FireStoreClass().getCurrentUserID())
        //here createdBy and assignedTo is same which is current user
        //assignedTo for BOARD CLASS can be different and assignedTo for CARDS CLASS can be different
        //because both have field assignedTo in them

        val card = Cards(name,FireStoreClass().getCurrentUserID(),cardAssignedTo)
        //here createdBy and assignedTo is same which is current user
        //assignedTo for BOARD CLASS can be different and assignedTo for CARDS CLASS can be different
        //because both have field assignedTo in them

        val cardList = mBoardDetails.taskList[position].cards
        cardList.add(card)

        val task = Task(mBoardDetails.taskList[position].name,mBoardDetails.taskList[position].createdBy,cardList)

        mBoardDetails.taskList[position] = task

        showProgressDialog("Please Wait...")
        FireStoreClass().addOrUpdateTaskListIntoFireStore(this,mBoardDetails)
    }

    fun cardDetailsActivity(taskPosition : Int,cardPosition : Int){
        val intent = Intent(this,CardDetailsActivity::class.java)
        intent.putExtra(Constant.BOARD_DETAILS,mBoardDetails)
        intent.putExtra(Constant.TASK_LIST_ITEM_POSITION,taskPosition)
        intent.putExtra(Constant.CARD_LIST_ITEM_POSITION,cardPosition)
        intent.putExtra(Constant.ASSIGNED_MEMBERS_LIST,mAssignedToMembersList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    fun getAssignedMembersList(list : ArrayList<User>){
        mAssignedToMembersList = list
        dismissProgressDialog()

        val task = Task("ADD TASK")
        mBoardDetails.taskList.add(task)

        rv_for_tasks_task_list_activity.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        rv_for_tasks_task_list_activity.setHasFixedSize(true)

        val taskListItemsAdapter = TaskListItemsAdapter(this,mBoardDetails.taskList)
        rv_for_tasks_task_list_activity.adapter = taskListItemsAdapter
    }
    fun updateCardsPositionAfterDragging(taskPosition: Int,cardList : ArrayList<Cards>){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        //TODO why we are doing above sentence???

        mBoardDetails.taskList[taskPosition].cards = cardList

        showProgressDialog("Please Wait...")
        FireStoreClass().addOrUpdateTaskListIntoFireStore(this,mBoardDetails)
        //after dragging we also want to update new cards position in the database that is why we are calling this function
    }
}
