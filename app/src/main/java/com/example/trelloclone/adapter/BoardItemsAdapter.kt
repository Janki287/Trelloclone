package com.example.trelloclone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.models.Board
import kotlinx.android.synthetic.main.rv_for_board_item_main_content.view.*

open class BoardItemsAdapter(private var context: Context,private var list : ArrayList<Board>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_for_board_item_main_content,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
            Glide.with(context)
                .load(model.image)
                .fitCenter()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.itemView.civ_for_rv)

            holder.itemView.tv_board_name_for_rv_.text = model.name
            holder.itemView.tv_created_by_for_rv.text = "Created By: ${model.createdBy}"

            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position,model)//when we click on items of the recycler view this will be called
                    //and it's implementation is derived in the main activity
                }
            }
        }
    }

    fun setOnClickListener(onClickListener : OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(position : Int,model : Board)
    }

    private inner class MyViewHolder(view : View) : RecyclerView.ViewHolder(view)
}