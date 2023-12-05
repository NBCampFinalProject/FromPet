package com.pet.frompet.ui.login

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.pet.frompet.R
import com.pet.frompet.data.model.CommunityHomeData


class MemberInfoAdapter(private val context: Context, private var data: List<CommunityHomeData>
):BaseAdapter() {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_info_type,null)
        val imageView = view.findViewById<ImageView>(R.id.iv_animal)
        val textView = view.findViewById<TextView>(R.id.sp_pet)

        val item = data[position]
        imageView.setImageResource(item.petLogo)
        textView.text = item.petType

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return super.getDropDownView(position, convertView, parent)
    }

    fun updateData(newData: List<CommunityHomeData>){
        data = newData
        notifyDataSetChanged()
    }
}
