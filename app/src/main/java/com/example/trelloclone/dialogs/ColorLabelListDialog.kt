package com.example.trelloclone.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapter.ColorLabelItemsAdapter
import kotlinx.android.synthetic.main.layout_for_select_label_color.*
import kotlinx.android.synthetic.main.layout_for_select_label_color.view.*
import kotlinx.android.synthetic.main.layout_for_select_label_color.view.rv_for_selecting_color_label

abstract class ColorLabelListDialog(
    context: Context,
    private var list : ArrayList<String>,
    private var title : String= "",
    private var mSelectedColor : String = "") : Dialog(context){

    private var colorLabelItemsAdapter : ColorLabelItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.layout_for_select_label_color,null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupRecyclerView(view)
    }

    private fun setupRecyclerView(view : View){
        view.tv_selected_color_from_color_list.text = title
        view.rv_for_selecting_color_label.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        view.rv_for_selecting_color_label.setHasFixedSize(true)

        colorLabelItemsAdapter = ColorLabelItemsAdapter(context,list,mSelectedColor)
        rv_for_selecting_color_label.adapter = colorLabelItemsAdapter

        colorLabelItemsAdapter!!.setOnClickListener(object : ColorLabelItemsAdapter.OnClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss() //we are dismiss this dialog after selecting the color from color list
                onItemSelected(color) //when we select color from color list then this function should run
            }
        })
    }
    protected abstract fun onItemSelected(color : String)
}