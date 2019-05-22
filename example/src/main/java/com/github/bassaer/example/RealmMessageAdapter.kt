package com.github.bassaer.example

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import io.realm.OrderedRealmCollection
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RealmMessageAdapter internal constructor(context: Context): BaseAdapter(){
    private val mLayoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var mMessageList: ArrayList<MessageDataBase>? = null

    fun setMessageArrayList(messageArrayList: ArrayList<MessageDataBase>){
        mMessageList = messageArrayList
    }

    override fun getCount(): Int {
        return mMessageList!!.size
    }

    override fun getItem(position: Int): Any {
        return mMessageList!![position]
    }

    override fun getItemId(position: Int): Long {
        return mMessageList!![position].id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null)
        }

        val textView1 = convertView!!.findViewById<TextView>(android.R.id.text1)
        val textView2 = convertView.findViewById<TextView>(android.R.id.text2)

        textView1.text = mMessageList!![position].title

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE)
        val date = mMessageList!![position].date
        textView2.text = simpleDateFormat.format(date)

        return convertView    }
}
