package com.example.trelloclone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.models.SelectedMembers
import kotlinx.android.synthetic.main.rv_items_for_assigned_members_list_in_card_details_activity.view.*

open class AssignedMembersListAdapter(private val context: Context,
                                      private val list : ArrayList<SelectedMembers>,private val assignedMembers : Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_items_for_assigned_members_list_in_card_details_activity,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
            if(position == list.size - 1 && assignedMembers){
                holder.itemView.civ_for_add_btn_assigned_members_card_details.visibility = View.VISIBLE
                holder.itemView.civ_for_user_assigned_members_card_details.visibility = View.GONE
            }else{
                holder.itemView.civ_for_add_btn_assigned_members_card_details.visibility = View.GONE
                holder.itemView.civ_for_user_assigned_members_card_details.visibility = View.VISIBLE

                Glide.with(context)
                    .load(model.image)
                    .fitCenter()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(holder.itemView.civ_for_user_assigned_members_card_details)
            }

            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick()
                }
            }
        }
    }

    fun setOnClickListener(onClickListener : OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick()
    }

    private inner class MyViewHolder(view : View) : RecyclerView.ViewHolder(view)
}