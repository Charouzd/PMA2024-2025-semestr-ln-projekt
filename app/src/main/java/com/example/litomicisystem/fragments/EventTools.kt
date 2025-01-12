package com.example.litomicisystem.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.litomicisystem.LitostemDao
import com.example.litomicisystem.R
import com.example.litomicisystem.SystemDatabase
import com.example.litomicisystem.ToolsAdapter
import com.example.litomicisystem.databinding.FragmentEventToolsBinding
import com.example.litomicisystem.entities.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventTools : Fragment(R.layout.fragment_event_tools) {

    private var _binding: FragmentEventToolsBinding? = null
    private val binding get() = _binding!!

    private lateinit var dao: LitostemDao
    private lateinit var adapter: ToolsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEventToolsBinding.inflate(inflater, container, false)
        dao = SystemDatabase.getInstance(requireContext()).litostemDao

        setupRecyclerView()

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nastavení listeneru na tlačítko NewEvent
        binding.NewEvent.setOnClickListener {
            newEventForm()
        }
        binding.NESave.setOnClickListener(){
            saveNewEvent()
        }
        // Další logika, například naplnění RecyclerView
        setupRecyclerView()
        loadEvents()
    }
    /**
     * ulož nově vytvtořenou akci do databáze, provede kontrolu, že jsou všechny inputy vyplněny
    * */
    private fun saveNewEvent() {
        // Získání hodnot z formuláře
        val name = binding.NEName.text.toString()
        val starts = binding.NEStarts.text.toString()
        val ends = binding.NEEnds.text.toString()
        val meetingPoint = binding.NEMeetingPoint.text.toString()
        val priceText = binding.NEPrice.text.toString()
        val capText = binding.NECap.text.toString()
        val typeText = binding.NEType.text.toString()
        val checklist = binding.NEChecklist.text.toString().removePrefix("S sebou:\n")
        val description = binding.NEDescription.text.toString().removePrefix("Více detailů o akci:\n")

        // Kontrola povinných polí
        if (name.isEmpty() || starts.isEmpty() || ends.isEmpty() || meetingPoint.isEmpty() || priceText.isEmpty() || capText.isEmpty() || typeText.isEmpty()) {
            Log.e("NewEvent", "Některá pole jsou prázdná")
            return
        }

        // Převod hodnot na vhodné datové typy
        val price = priceText.toFloatOrNull() ?: 0f
        val capacity = capText.toIntOrNull() ?: 0
        val type = typeText.toShortOrNull() ?: 0

        // Vytvoření objektu Event
        val newEvent = Event(
            id = 0, // ID může být auto-incrementované, pokud je nastavené v databázi
            eventName = name,
            starts = starts,
            ends = ends,
            meetingPoint = meetingPoint,
            price = price,
            capacity = capacity,
            departure = "", // Zadej správnou hodnotu, pokud je potřeba
            destination = "", // Zadej správnou hodnotu, pokud je potřeba
            checklist = checklist,
            description = description,
            type = type
        )

        // Uložení do databáze
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                dao.insertEvent(newEvent)
                withContext(Dispatchers.Main) {
                    Log.d("NewEvent", "Event byl úspěšně uložen")
                    // Skrytí formuláře a obnovení seznamu
                    binding.NEForm.visibility = View.GONE
                    setupRecyclerView()
                    loadEvents()
                    binding.toollistRecycler.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("NewEvent", "Chyba při ukládání eventu: ${e.message}")
            }
        }
    }
/**
 * zobrazí formulář pro vytvoření nového eventu
 * */
    private fun newEventForm() {
        binding.NEForm.visibility=View.VISIBLE
        binding.EEForm.visibility=View.GONE
        binding.toollistRecycler.visibility=View.GONE
    }

/**
 * vyčistí a načte recyclerview
 * */
    private fun setupRecyclerView() {
        adapter = ToolsAdapter(
            tools = emptyList(),
            onEditClick = { event ->
                showEditForm(event)
            },
            onDeleteClick = { event ->
                deleteEvent(event)
            }
        )
        binding.toollistRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.toollistRecycler.adapter = adapter
    }
    /**
     * vloží do vyčištěného recycleru data z databáze o eventech
     * */
    private fun loadEvents() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val events = dao.getAllEvents() // Načítání všech eventů z databáze
                withContext(Dispatchers.Main) {
                    adapter.updateData(events) // Aktualizace dat v adapteru
                }
            } catch (e: Exception) {
                Log.e("EventTools", "Error loading events: ${e.message}")
            }
        }
    }
    /**
     * zobrazí možnost upravit data o akci
     * */
    private fun showEditForm(event: Event) {
        // Make the EE_form visible and populate it with event data
        binding.EEForm.visibility = View.VISIBLE
        binding.EEName.setText(event.eventName)
        binding.EEStarts.setText(event.starts)
        binding.EEEnds.setText(event.ends)
        binding.EEMeetingPoint.setText(event.meetingPoint)
        binding.EEPrice.setText(event.price.toString())
        binding.EECap.setText(event.capacity.toString())
        binding.EEType.setText(event.type.toString())
        binding.EEChecklist.setText("S sebou: ${event.checklist}")
        binding.EEDescription.setText("Více detailů o akci: ${event.description}")

        // Handle the save button click to update the event
        binding.EESave.setOnClickListener {
            val updatedEvent = Event(
                id = event.id,
                eventName = binding.EEName.text.toString(),
                starts = binding.EEStarts.text.toString(),
                ends = binding.EEEnds.text.toString(),
                meetingPoint = binding.EEMeetingPoint.text.toString(),
                price = binding.EEPrice.text.toString().toFloat(),
                capacity = binding.EECap.text.toString().toInt(),
                departure = "",
                destination = "",
                checklist = binding.EEChecklist.text.toString(),
                description = binding.EEDescription.text.toString(),
                type = binding.EEType.text.toString().toShort()
            )

            updateEvent(updatedEvent)
            loadEvents()
        }
    }
    /**
     * zapíše změny o akci do databáze
     * */
    private fun updateEvent(event: Event) {
        lifecycleScope.launch(Dispatchers.IO) {
            dao.updateEvent(event) // Assuming you have an update method in your DAO
            withContext(Dispatchers.Main) {
                // Hide the form after saving
                binding.EEForm.visibility = View.GONE
                setupRecyclerView() // Refresh the RecyclerView
            }
        }
    }
    /**
     * smaže akci a všechny její reference
     * */
    private fun deleteEvent(event: Event) {
        lifecycleScope.launch(Dispatchers.IO) {
            dao.deleteEvent(event) // Assuming you have a delete function in your DAO
            withContext(Dispatchers.Main) {
                setupRecyclerView() // Refresh the RecyclerView
                loadEvents()
            }
        }
    }
    /**
     * destruktor fragmentu
     * */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

