package com.example.litomicisystem

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.example.litomicisystem.placeholder.PlaceholderItem

class MyItemRecyclerViewAdapter(
    private val dataList: ArrayList<PlaceholderItem>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: PlaceholderItem, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)

        // Nastavení listeneru na celou položku
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(item, position)
        }

        // Pokud chcete zachytit kliknutí jen na tlačítko
        holder.EB.setOnClickListener {
            itemClickListener.onItemClick(item, position) // Případně jiná akce
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val EN = itemView.findViewById<Button>(R.id.event_name)
        val ED = itemView.findViewById<TextView>(R.id.event_date)
        val EC = itemView.findViewById<TextView>(R.id.event_capacity)
        val EB = itemView.findViewById<Button>(R.id.event_signup)

        fun bind(item: PlaceholderItem) {
            EN.text = item.content
            ED.hint = item.id
            ED.text = item.date
            EC.text = item.capacity
            EB.text = "Přihlásit členy"
        }
    }
}

