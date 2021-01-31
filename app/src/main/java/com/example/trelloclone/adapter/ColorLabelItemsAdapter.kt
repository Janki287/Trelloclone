package com.example.trelloclone.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.R
import kotlinx.android.synthetic.main.rv_for_label_color_items.view.*

open class ColorLabelItemsAdapter(private val context: Context,
                                  private val list : ArrayList<String>, private val mSelectedColor : String) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_for_label_color_items,parent,false))
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val color = list[position]

        if(holder is MyViewHolder){
            holder.itemView.view_for_label_color.setBackgroundColor(Color.parseColor(color))

            if(color == mSelectedColor){
                holder.itemView.iv_done_btn_for_label_color.visibility = View.VISIBLE
            }else{
                holder.itemView.iv_done_btn_for_label_color.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position, color)
                }
            }
        }
    }

    fun setOnClickListener(onClickListener : OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(position: Int,color : String)
    }

    private inner class MyViewHolder(view : View) : RecyclerView.ViewHolder(view)
}