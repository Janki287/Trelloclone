package com.example.trelloclone.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapter.MembersListItemsAdapter
import com.example.trelloclone.models.User
import kotlinx.android.synthetic.main.layout_for_select_label_color.*
import kotlinx.android.synthetic.main.layout_for_select_label_color.view.*
import kotlinx.android.synthetic.main.layout_for_select_label_color.view.rv_for_selecting_color_label

abstract class AssignedMembersListDialog(context: Context,private var title : String,private var list : ArrayList<User>) : Dialog(context) {

    private var membersListItemsAdapter : MembersListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.layout_for_select_label_color,null)
        //layout_for_select_label_color is used for multiple colors dialog
        //here we also using it for multiple members dialog
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setRecyclerView(view)
    }

    private fun setRecyclerView(view : View){
        view.tv_selected_color_from_color_list.text = title

        if(list.size > 0){
            view.rv_for_selecting_color_label.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
            view.rv_for_selecting_color_label.setHasFixedSize(true)

            val adapter = MembersListItemsAdapter(context,list)
            rv_for_selecting_color_label.adapter = adapter

            adapter.setOnClickListener(object : MembersListItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: User, action: String) {
                    dismiss()
                    onItemSelected(model,action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(model : User,action : String)
}