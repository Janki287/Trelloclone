package com.example.trelloclone.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.R
import com.example.trelloclone.activities.TaskListActivity
import com.example.trelloclone.models.Task
import kotlinx.android.synthetic.main.activity_task_list.view.*
import kotlinx.android.synthetic.main.rv_for_task_list_items.view.*
import java.util.*
import kotlin.collections.ArrayList

open class TaskListItemsAdapter(private val context: Context,private val list : ArrayList<Task>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mDraggedFrom = -1
    private var mDraggedTo = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //this function means, how we want our items to be showed in recycler view
        //basically it is responsible for view part
        val view = LayoutInflater.from(context).inflate(R.layout.rv_for_task_list_items,parent,false)
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
        //we only want width=70% of screen and height=wrap content
        layoutParams.setMargins(
            (15.toDp()).toPx(),
            0,
            (40.toDp()).toPx(),
            0)//by this code we only set the left=15 and right=40 margins
        view.layoutParams = layoutParams
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //it is responsible for all the operation we are doing in recycler view(on click,item visible,item gone)
        val model = list[position]

        if(holder is MyViewHolder){
            if(position == list.size - 1){ //here list size is 1 but position is index based so that is 0,that is why we are subtract one from list.size
                //TODO what is in this if condition
                holder.itemView.tv_add_list_in_task_list_activity.visibility = View.VISIBLE
                holder.itemView.ll_task_list_all_items.visibility = View.GONE
            }else{
                holder.itemView.tv_add_list_in_task_list_activity.visibility = View.GONE
                holder.itemView.ll_task_list_all_items.visibility = View.VISIBLE
            }
            holder.itemView.tv_task_title.text = model.name
            holder.itemView.tv_add_list_in_task_list_activity.setOnClickListener {
                holder.itemView.tv_add_list_in_task_list_activity.visibility = View.GONE
                holder.itemView.cv_for_give_task_list_name.visibility = View.VISIBLE
            }
            holder.itemView.ib_cancel_give_task_title.setOnClickListener {
                holder.itemView.tv_add_list_in_task_list_activity.visibility = View.VISIBLE
                holder.itemView.cv_for_give_task_list_name.visibility = View.GONE
            }
            holder.itemView.ib_check_give_task_title.setOnClickListener {
                val taskListName = holder.itemView.et_give_task_title.text.toString()
                if(taskListName .isNotEmpty()){
                    if(context is TaskListActivity){
                        context.createNewTask(taskListName)
                    }
                }else{
                    Toast.makeText(context,"Please Enter a task list name",Toast.LENGTH_SHORT).show()
                }
            }
            holder.itemView.ib_edit_task_title.setOnClickListener {
                holder.itemView.cv_for_editing_task_title.visibility = View.VISIBLE
                holder.itemView.et_edit_title_of_task.setText(model.name)
                holder.itemView.ll_edit_or_delete_task.visibility = View.GONE
            }
            holder.itemView.ib_cancel_update_task_title.setOnClickListener {
                holder.itemView.cv_for_editing_task_title.visibility = View.GONE
                holder.itemView.ll_edit_or_delete_task.visibility = View.VISIBLE
            }
            holder.itemView.ib_done_update_task_title.setOnClickListener {
                val name = holder.itemView.et_edit_title_of_task.text.toString()
                if(name.isNotEmpty()){
                    if(context is TaskListActivity){
                        context.updateTaskList(position,name,model)
                    }
                }else{
                    Toast.makeText(context,"Please enter name for edit a task title",Toast.LENGTH_SHORT).show()
                }
            }
            holder.itemView.ib_delete_task_from_task_list.setOnClickListener {
                alertDialogForDeleteTask(position,model.name)
            }
            holder.itemView.tv_add_card_button.setOnClickListener {
                holder.itemView.tv_add_card_button.visibility = View.GONE
                holder.itemView.cv_for_add_card.visibility =View.VISIBLE
            }
            holder.itemView.ib_cancel_add_card.setOnClickListener {
                holder.itemView.tv_add_card_button.visibility = View.VISIBLE
                holder.itemView.cv_for_add_card.visibility =View.GONE
            }
            holder.itemView.ib_done_add_card.setOnClickListener {
                val cardName = holder.itemView.et_title_add_card.text.toString()
                if(cardName.isNotEmpty()){
                    if(context is TaskListActivity){
                        context.addCardToTaskListAtPosition(position,cardName)
                    }
                }else{
                    Toast.makeText(context,"Please Enter a card name",Toast.LENGTH_SHORT).show()
                }
            }

            holder.itemView.rv_for_cards_task_list.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
            holder.itemView.rv_for_cards_task_list.setHasFixedSize(true)

            val cardListAdapter = CardListItemsAdapter(context,model.cards)
            holder.itemView.rv_for_cards_task_list.adapter = cardListAdapter

            cardListAdapter.setOnClickListener(object  : CardListItemsAdapter.OnClickListener{
                override fun onClick(cardPosition: Int) {
                    if(context is TaskListActivity){
                        context.cardDetailsActivity(position,cardPosition)
                        //here position is particular task's position we are on
                        //here cardPosition is particular card's position we are on
                        //because there is multiple task in each board, each task have it;s own position
                        //and there is multiple cards in each task,so each card have also it's own position
                    }
                }
            })

            //TODO we want to drag the cards inside the particular task so that is why we have to implement drag feature inside the taskListItemsAdapter
            // if we want drag feature inside the cardDetails then we have to implement the drag feature inside the cardListItemsAdapter

            val dividerItemDecoration = DividerItemDecoration(context,DividerItemDecoration.VERTICAL)
            holder.itemView.rv_for_cards_task_list.addItemDecoration(dividerItemDecoration)
            //we want to move the cards inside the cards recycler view, so we have to give rv_for_cards_task_list here

            val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,0){
                //ItemTouchHelper.UP or ItemTouchHelper.DOWN this is IMP we have to write it like this only to work
                override fun onMove(
                    recyclerView: RecyclerView,
                    dragged: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder): Boolean {
                    val draggedPosition = dragged.adapterPosition //old position before dragging
                    val targetPosition = target.adapterPosition //new position after dragging

                    if(mDraggedFrom == -1){
                        mDraggedFrom = draggedPosition
                    }
                    mDraggedTo = targetPosition
                    Collections.swap(list[position].cards,draggedPosition,targetPosition)
                    cardListAdapter.notifyItemChanged(draggedPosition,targetPosition)
                    return true
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)
                    if(mDraggedFrom != -1 && mDraggedTo != -1 && mDraggedFrom != mDraggedTo){
                        //this means we have dragged the cards(alter the position of cards in the particular task)
                        //so we have to update this new position into database
                        (context as TaskListActivity).updateCardsPositionAfterDragging(position,list[position].cards)
                    }

                    //after updating new position of the cards in the database we reset the two variable
                    //so that we can again perform the drag operation
                    mDraggedFrom = -1
                    mDraggedTo = -1
                }
            })

            itemTouchHelper.attachToRecyclerView(holder.itemView.rv_for_cards_task_list)
        }
    }
    private inner class MyViewHolder(view : View) : RecyclerView.ViewHolder(view)

    private fun Int.toDp() : Int = (this / Resources.getSystem().displayMetrics.density).toInt()
    //this will convert pixels to density pixels

    private fun Int.toPx() : Int = (this * Resources.getSystem().displayMetrics.density).toInt()
    //this will convert density pixels to pixels

    private fun alertDialogForDeleteTask(position : Int, name : String){
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle("Alert")
        alertDialog.setIcon(R.drawable.ic_announcement_black_24dp)
        alertDialog.setMessage("Are You Sure You want to delete a $name task?")
        alertDialog.setPositiveButton("YES") { dialog, _ ->
            dialog.dismiss()
            if(context is TaskListActivity){
                context.deleteTaskFromTaskList(position)
            }
        }
        alertDialog.setNegativeButton("NO") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = alertDialog.create()
        dialog.setCancelable(false) //user can not close this alert dialog by touching outside
        dialog.show()
    }
}