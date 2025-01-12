package com.example.litomicisystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MemberListAdapter(var members: List<MemberItem>) : RecyclerView.Adapter<MemberListAdapter.MemberListViewHolder>(){

    inner class  MemberListViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val name: EditText = itemView.findViewById(R.id.M_name)
        val lastName: EditText = itemView.findViewById(R.id.M_lastname)
        val born: EditText = itemView.findViewById(R.id.M_born)
        val health: TextView = itemView.findViewById(R.id.M_health)
        val checkGDPR: CheckBox = itemView.findViewById(R.id.M_GDPR)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberListViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.member_list_item,parent,false)
        return MemberListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return members.size
    }

    override fun onBindViewHolder(holder: MemberListViewHolder, position: Int) {
        val member = members[position]

        // Naplň jednotlivé položky daty
        holder.name.setText(member.name)
        holder.lastName.setText(member.lastName)
        holder.born.setText(member.born)
        holder.health.setText(member.hp)
        holder.checkGDPR.isChecked = member.gdprChecked


    }
}