package com.example.litomicisystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.litomicisystem.entities.Event

class ToolsAdapter(
    var tools: List<Event>, // Seznam Event objektů
    private val onEditClick: (Event) -> Unit, // Funkce pro zpracování kliknutí na Edit
    private val onDeleteClick: (Event) -> Unit // Funkce pro zpracování kliknutí na Delete
) : RecyclerView.Adapter<ToolsAdapter.ToolViewHolder>() {

    inner class ToolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.EventItemName)
        val date: EditText = itemView.findViewById(R.id.EventItemDate)
        val editButton: Button = itemView.findViewById(R.id.EditEvent)
        val deleteButton: Button = itemView.findViewById(R.id.DeleteEvent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_tools_item, parent, false)
        return ToolViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        val tool = tools[position]

        holder.name.setText(tool.eventName) // Naplnění názvem akce
        holder.date.setText(tool.starts) // Naplnění datem akce

        // Nastavení listenerů pro tlačítka
        holder.editButton.setOnClickListener {
            onEditClick(tool) // Volání edit funkce předané z fragmentu
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(tool) // Volání delete funkce předané z fragmentu
        }
    }

    override fun getItemCount(): Int {
        return tools.size
    }
    fun updateData(newTools: List<Event>) {
        tools = newTools
        notifyDataSetChanged() // Oznámí RecyclerView, že data byla změněna
    }
}
