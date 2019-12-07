package com.mcc.g22

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class ProjectListAdapter(private val mDataList: ArrayList<Project>) : RecyclerView.Adapter<ProjectListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.projects_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val p = mDataList[position]
        val ctx = holder.itemView.context
        holder.ptitle.text = p.name
        p.loadBadgeIntoImageView(ctx, holder.pbadge)
        holder.lastModified.text = p.lastModificationDate.toString()

        for ((i, memId) in p.membersIds.withIndex()) {
            if (i == 0) {
                User.showProfileImageOfUserWithId(memId, ctx, holder.profileImgUsr1)
            } else if (i == 1) {
                User.showProfileImageOfUserWithId(memId, ctx, holder.profileImgUsr2)
            } else if (i == 2) {
                User.showProfileImageOfUserWithId(memId, ctx, holder.profileImgUsr3)
            } else break
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var ptitle: TextView = itemView.findViewById<View>(R.id.project_title) as TextView
        internal var pbadge: ImageView = itemView.findViewById(R.id.profile_picture)
        internal var profileImgUsr1: ImageView = itemView.findViewById(R.id.member1)
        internal var profileImgUsr2: ImageView = itemView.findViewById(R.id.member2)
        internal var profileImgUsr3: ImageView = itemView.findViewById(R.id.member3)
        internal var lastModified: TextView = itemView.findViewById(R.id.modified_date)
    }
}