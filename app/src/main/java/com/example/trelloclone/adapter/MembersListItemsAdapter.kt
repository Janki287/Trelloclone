package com.example.trelloclone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constant
import kotlinx.android.synthetic.main.rv_for_members_activity_items.view.*

open class MembersListItemsAdapter(private val context: Context,private val list : ArrayList<User>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener  : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_for_members_activity_items,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
            Glide.with(context)
                .load(model.image)
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.itemView.civ_rv_for_members)

            holder.itemView.tv_name_rv_for_members.text = model.name
            holder.itemView.tv_email_rv_for_members.text = model.email

            if(model.selected){
                holder.itemView.iv_for_select_member.visibility = View.VISIBLE
                //if user is selected then show the CHECK MARK on it
            }else{
                holder.itemView.iv_for_select_member.visibility = View.GONE
                //if user is not selected then do not show the CHECK MARK on it
            }

            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    if(model.selected){
                        onClickListener!!.onClick(position,model,Constant.UNSELECT_MEMBER_FOR_CARD)
                        //if already member is selected and we again click on it, then it will be unselected
                    }else{
                        onClickListener!!.onClick(position,model,Constant.SELECT_MEMBER_FOR_CARD)
                        //if already member is not selected and we click on it, it will be selected
                    }
                }
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(position: Int,model : User,action : String)
    }

    private inner class MyViewHolder(view : View) : RecyclerView.ViewHolder(view)
}