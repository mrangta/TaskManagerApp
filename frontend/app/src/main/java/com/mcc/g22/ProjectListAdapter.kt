package com.mcc.g22

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.util.ArrayList

class ProjectListAdapter(private val mDataList: ArrayList<ProjectListDetails>) : RecyclerView.Adapter<ProjectListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.projects_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.ptitle.text = mDataList[position].project_title
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var ptitle: TextView

        init {
            ptitle = itemView.findViewById<View>(R.id.project_title) as TextView
        }
    }
}