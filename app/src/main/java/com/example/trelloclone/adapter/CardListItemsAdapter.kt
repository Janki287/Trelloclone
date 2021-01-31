package com.example.trelloclone.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.R
import com.example.trelloclone.activities.TaskListActivity
import com.example.trelloclone.models.Cards
import com.example.trelloclone.models.SelectedMembers
import kotlinx.android.synthetic.main.rv_for_card_list_items.view.*

class CardListItemsAdapter(private val context: Context,private val list: ArrayList<Cards>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_for_card_list_items,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){

            if(model.color.isNotEmpty()){
                holder.itemView.view_for_card_color.visibility = View.VISIBLE
                holder.itemView.view_for_card_color.setBackgroundColor(Color.parseColor(model.color))
            }else{
                holder.itemView.view_for_card_color.visibility = View.GONE
            }

            holder.itemView.tv_card_name.text = model.name

            if((context as TaskListActivity).mAssignedToMembersList.size > 0){
                val selectedMembersList : ArrayList<SelectedMembers> = ArrayList()
                for(i in context.mAssignedToMembersList.indices){
                    for(j in model.assignedTo){
                        if(context.mAssignedToMembersList[i].id == j){
                            val selectedMembers = SelectedMembers(context.mAssignedToMembersList[i].id,context.mAssignedToMembersList[i].image)
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }
                if(selectedMembersList.size > 0){
                    if(selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                        holder.itemView.rv_for_member_in_task_list_activity.visibility = View.GONE
                    }else{
                        holder.itemView.rv_for_member_in_task_list_activity.visibility = View.VISIBLE

                        holder.itemView.rv_for_member_in_task_list_activity.layoutManager = GridLayoutManager(context,4)
                        holder.itemView.rv_for_member_in_task_list_activity.setHasFixedSize(true)

                        val adapter = AssignedMembersListAdapter(context,selectedMembersList,false)
                        //false = because we do not want to show the plus sign(blue add button) in this recycler view
                        holder.itemView.rv_for_member_in_task_list_activity.adapter = adapter

                        adapter.setOnClickListener(object : AssignedMembersListAdapter.OnClickListener{
                            override fun onClick() {
                                if(onClickListener != null){
                                    onClickListener!!.onClick(position)
                                    //if we click on the image of the members then we also want to open the cardDetailsActivity
                                    //that is why we are giving onClickListener!!.onClick(position) inside override fun onClick()
                                }
                            }
                        })
                    }
                }else{
                    holder.itemView.rv_for_member_in_task_list_activity.visibility = View.GONE
                }
            }


            holder.itemView.setOnClickListener {
                //here we are giving each card (OR itemView) a setOnClickListener
                if(onClickListener != null){
                    onClickListener!!.onClick(position)
                    //here position is card position in particular task
                }
            }
        }
    }

    fun setOnClickListener(onClickListener : OnClickListener){
        this.onClickListener = onClickListener
    }

    private inner class MyViewHolder(view : View) : RecyclerView.ViewHolder(view)

    interface OnClickListener{
        fun onClick(cardPosition: Int)
    }
}