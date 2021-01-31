package com.example.trelloclone.firestore

import android.app.Activity
import android.widget.Toast
import com.example.trelloclone.activities.*
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FireStoreClass {

    private val mFireStore: FirebaseFirestore = FirebaseFirestore.getInstance() //fire store means data base in fire base

    fun registerUserIntoFireStore(activity: SignUpActivity,userInfo : User){
        mFireStore.collection(Constant.USERS).document(getCurrentUserID()).set(userInfo, SetOptions.merge()).addOnSuccessListener {
            activity.userRegisterIntoFireStoreSuccess()
            //activity.userRegisterIntoFireStoreSuccess() this function will tells us that we have successfully added user and sign out that user
            //and finish signOutActivity and go into intro activity for signIn the user
        }
    }

    fun createNewBoardIntoFireStore(activity: CreateBoardActivity, board: Board){
        mFireStore.collection(Constant.BOARD).document().set(board, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(activity,"Board Created Successfully",Toast.LENGTH_SHORT).show()
            activity.boardCreatedSuccessfully()
        }.addOnFailureListener {
            e ->
            activity.dismissProgressDialog()
            println("Error:::${e.message}")
        }
    }

    fun getBoardListFromFireStore(activity: MainActivity){
        mFireStore.collection(Constant.BOARD).whereArrayContains(Constant.ASSIGNED_TO,getCurrentUserID()).get().addOnSuccessListener { document->
            Toast.makeText(activity,"Getting Board list",Toast.LENGTH_SHORT).show()
            val boardList : ArrayList<Board> = ArrayList<Board>()
            for(i in document.documents){
                val board = i.toObject(Board::class.java)
                board!!.boardID = i.id//this i.id is board's UID
                boardList.add(board)
            }
            activity.populateBoardList(boardList)
        }.addOnFailureListener {
            e->
            Toast.makeText(activity,"Error getting board list",Toast.LENGTH_SHORT).show()
            println("Error::${e.message}")
        }
    }

    fun getBoardDetailsFromFireStore(activity: TaskListActivity,boardId : String){
        mFireStore.collection(Constant.BOARD).document(boardId).get().addOnSuccessListener { document->
            val board = document.toObject(Board::class.java)
            if(board != null){
                board.boardID = document.id
                activity.getBoardDetailsFromFireStore(board)
            }
        }.addOnFailureListener {
                e->
            activity.dismissProgressDialog()
            Toast.makeText(activity,"Error getting board Details",Toast.LENGTH_SHORT).show()
            println("Error::${e.message}")
        }
    }

    fun addOrUpdateTaskListIntoFireStore(activity: Activity, board : Board){
        val taskListHashMap = HashMap<String,Any>()
        taskListHashMap[Constant.TASK_LIST] = board.taskList

        mFireStore.collection(Constant.BOARD).document(board.boardID).update(taskListHashMap).addOnSuccessListener {
            if(activity is TaskListActivity){
                activity.addOrUpdateTaskListSuccess()
            }else if(activity is CardDetailsActivity){
                activity.updateOrDeleteCardSuccess()
            }
        }.addOnFailureListener {
            e ->
            if(activity is TaskListActivity){
                activity.dismissProgressDialog()
            }else if(activity is CardDetailsActivity){
                activity.dismissProgressDialog()
            }
            println("Error:::${e.message}")
        }
    }

    fun getAssignedMembersListDetails(activity: Activity,assignedTo : ArrayList<String>){
        mFireStore.collection(Constant.USERS).whereIn(Constant.ID,assignedTo).get().addOnSuccessListener {document ->
            Toast.makeText(activity,"Getting Members List",Toast.LENGTH_SHORT).show()
            val userList : ArrayList<User> = ArrayList()

            for(i in document.documents){
                val user = i.toObject(User::class.java)!!
                userList.add(user)
            }
            //println("UserLIST :::${userList}")
            if(activity is MembersActivity){
                activity.setupMembersList(userList)
            }else if(activity is TaskListActivity){
                activity.getAssignedMembersList(userList)
            }
        }.addOnFailureListener {
            e ->
            if(activity is MembersActivity){
                activity.dismissProgressDialog()
            }else if(activity is TaskListActivity){
                activity.dismissProgressDialog()
            }
            Toast.makeText(activity,"Failed Getting Members List",Toast.LENGTH_SHORT).show()
            println("Error:::${e.message}")
        }
    }

    fun getMembersFromEmailID(activity: MembersActivity,email : String){
        mFireStore.collection(Constant.USERS).whereEqualTo(Constant.EMAIL,email).get().addOnSuccessListener {document ->
            Toast.makeText(activity,"Getting User By Email ID",Toast.LENGTH_SHORT).show()
            if(document.documents.size > 0){
                val user = document.documents[0].toObject(User::class.java)!!
                activity.getMembersFromEmail(user)
            }else{
                activity.dismissProgressDialog()
                activity.showSnackBar("No User Found Of Such Email ID")
            }
        }.addOnFailureListener {
            e ->
            activity.dismissProgressDialog()
            Toast.makeText(activity,"Failed Getting User By Email ID",Toast.LENGTH_SHORT).show()
            println("Error:::${e.message}")
        }
    }

    fun assignNewAddedMemberToBoard(activity: MembersActivity,board: Board,user: User){
        val assignedHashMap : HashMap<String,Any> = HashMap()
        assignedHashMap[Constant.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constant.BOARD).document(board.boardID).update(assignedHashMap).addOnSuccessListener {
            Toast.makeText(activity,"Adding new member into assignedTo list",Toast.LENGTH_SHORT).show()
            activity.memberAssignedSuccess(user)
        }.addOnFailureListener {
            e ->
            activity.dismissProgressDialog()
            Toast.makeText(activity,"Failed to add new member into assignedTo list",Toast.LENGTH_SHORT).show()
            println("Error:::${e.message}")
        }
    }

    fun loadUserDataAfterLoggedIn(activity : Activity,boardListUpdate : Boolean = false){ //by default it is false,you can change it passing the values
        //this function basically load the user data from database(Fire store) into different activity
        mFireStore.collection(Constant.USERS).document(getCurrentUserID()).get().addOnSuccessListener {
            documentSnapshot ->
            val loggedInUser = documentSnapshot.toObject(User::class.java)
            //this will get the all the user details from fire store (database)
            //details like: name,email,mobile,fcm token,image,user ID

            when(activity){
                is SignInActivity ->{
                    if (loggedInUser != null) {
                        activity.userSignIntoFireStoreSuccess(loggedInUser)
                    }
                }
                is MainActivity ->{
                    if(loggedInUser != null) {
                        activity.updateUserDetailsInHeaderLayout(loggedInUser,boardListUpdate)
                    }
                }
                is ProfileActivity ->{
                    if (loggedInUser != null) {
                        activity.setupProfileUI(loggedInUser)
                    }
                }
            }
        }.addOnFailureListener {
            e->
            Toast.makeText(activity,"User Sign In Failed : Check your email and password",Toast.LENGTH_SHORT).show()
            println("Error:::${e.message}")
            //this failure function will throw the error if fireStore(database) will not send the user whether email and password is correct or not
        }
    }

    fun updateUserProfileData(activity: Activity,hashMap : HashMap<String,Any>){
        mFireStore.collection(Constant.USERS).document(getCurrentUserID()).update(hashMap)
            .addOnSuccessListener {
                when(activity){
                    is ProfileActivity -> {
                        Toast.makeText(activity,"Profile Updated Successfully",Toast.LENGTH_SHORT).show()
                        activity.profileUpdatedSuccessfully()
                    }
                    is MainActivity -> {
                        Toast.makeText(activity,"Token Updated Successfully",Toast.LENGTH_SHORT).show()
                        activity.tokenUpdateSuccess()
                    }
                }
            }.addOnFailureListener {
                e->
                when(activity){
                    is ProfileActivity -> {
                        activity.dismissProgressDialog()
                        println("Error:::${e.message}")
                        Toast.makeText(activity,"Profile is not updated",Toast.LENGTH_SHORT).show()
                    }
                    is MainActivity -> {
                        activity.dismissProgressDialog()
                        println("Error:::${e.message}")
                        Toast.makeText(activity,"Token is not updated",Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    fun getCurrentUserID() : String{
        val user = FirebaseAuth.getInstance().currentUser
        var currentUserUID = ""
        if(user != null){
            currentUserUID = user.uid
        }
        return currentUserUID
        //this function will return the current user id who is currently logged into the app
    }
}