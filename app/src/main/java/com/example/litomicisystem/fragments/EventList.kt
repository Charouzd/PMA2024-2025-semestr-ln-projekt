package com.example.litomicisystem.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.litomicisystem.LitostemDao
import com.example.litomicisystem.R
import com.example.litomicisystem.SystemDatabase
import com.example.litomicisystem.databinding.FragmentEventListBinding
import com.example.litomicisystem.placeholder.PlaceholderItem
import com.example.litomicisystem.MyItemRecyclerViewAdapter
import com.example.litomicisystem.entities.relations.MemberEventCrossRefference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventList : Fragment(R.layout.fragment_event_list), MyItemRecyclerViewAdapter.OnItemClickListener {

    private var _binding: FragmentEventListBinding? = null
    private val binding get() = _binding!!
    private var userId: String? = null
    private lateinit var dao: LitostemDao
    private lateinit var adapter: MyItemRecyclerViewAdapter
    private var famID: Int=0
    private var curEv: Int=0
    private val eventList = ArrayList<PlaceholderItem>() // Prázdný seznam na začátek

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    /**
     * funkce hlídá, které akce byla vybrána pro bližší informace a následeně její detaily a členy účtu předá ke zpracování
     * */
    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            userId = it.getString("userID")
        }
        dao = SystemDatabase.getInstance(requireActivity()).litostemDao

        // Inicializace adapteru s prázdným seznamem
        adapter = MyItemRecyclerViewAdapter(eventList, this)
        binding.recycledViewEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.recycledViewEvents.adapter = adapter

        // Načtení eventů z databáze
        loadEvents()
        loadMembers()
        binding.EDCancle.setOnClickListener {
            ShowEventList()
        }
        binding.EDSign.setOnClickListener {
            assignMembersToEvent()
        }
    }
    /**
     * zobrazí list eventů
     * */
    private fun ShowEventList() {
        binding.recycledViewEvents.visibility = View.VISIBLE
        binding.ED.visibility = View.GONE
    }
    /**
     * vyčistí a načte členy účtu. pokud jsou přihlášení zobrazí to také
     * */
    private fun loadMembers() {
        lifecycleScope.launch {
            // Přesunutí databázových operací na IO vlákno
            val members = withContext(Dispatchers.IO) {
                if (userId != null) {
                    val user = dao.getUserLogById(userId!!)
                    if (user != null) {
                        famID = user.family2log!!
                    }
                }
                dao.getMembersByAccountId(famID) // Přizpůsobte DAO metodu
            }

            // Aktualizace UI (v hlavním vlákně)
            val container = binding.memberCheckboxContainer
            container.removeAllViews() // Vyčistí staré checkboxy, pokud existují

            // Dynamické vytvoření checkboxů
            members.forEach { member ->
                val checkBox = CheckBox(requireContext()).apply {
                    text = "${member.name} (${member.birthday})"
                    id = View.generateViewId()
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                container.addView(checkBox)
            }
        }
    }
    /**
     * vyčistí a načte nový recyclerview eventů
     * */
    private fun loadEvents() {
        lifecycleScope.launch {
            // Načtení eventů z databáze na pozadí
            val events = withContext(Dispatchers.IO) {
                dao.getAllEvents() // Přizpůsobte názvu metody ve vašem DAO
            }

            // Aktualizace seznamu a oznámení adapteru
            eventList.clear()
            eventList.addAll(events.map { event ->
                PlaceholderItem(
                    id = event.id.toString(),
                    content = event.eventName,
                    date = event.starts,
                    capacity = event.capacity.toString()
                )
            })

            adapter.notifyDataSetChanged() // Oznámení adapteru o změně dat
        }
    }
    /**
     * zapíše členy na akce
     * */
    private fun assignMembersToEvent() {
        lifecycleScope.launch(Dispatchers.IO) {
            val selectedMemberIds = mutableListOf<Int>()

            // Prochází všechny děti v kontejneru a hledá zaškrtnuté checkboxy
            for (i in 0 until binding.memberCheckboxContainer.childCount) {
                val view = binding.memberCheckboxContainer.getChildAt(i)
                if (view is CheckBox && view.isChecked) {
                    // Předpokládáme, že id checkboxu odpovídá ID člena
                    selectedMemberIds.add(view.id)
                }
            }

            // Načteme aktuálně přiřazené členy k této události
            val currentlyAssignedMemberIds = withContext(Dispatchers.IO) {
                dao.getMembersForEvent(curEv).map { it.id }
            }

            // Členové, kteří jsou nově přiřazeni
            val membersToAdd = selectedMemberIds.filter { it !in currentlyAssignedMemberIds }

            // Členové, kteří jsou odepsáni
            val membersToRemove = currentlyAssignedMemberIds.filter { it !in selectedMemberIds }

            // Přidání nových členů
            membersToAdd.forEach { memberId ->
                val crossReference = MemberEventCrossRefference(
                    memberID = memberId,
                    eventID = curEv
                )
                dao.insertMemberEventCrossRefference(crossReference)
            }

            // Odstranění odepsaných členů
            membersToRemove.forEach { memberId ->
                dao.deleteMemberEventCrossReference(curEv, memberId)
            }

            // Aktualizace UI na hlavním vlákně
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "Členové byli aktualizováni pro tuto událost.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * settup litneru
     * */
    override fun onItemClick(item: PlaceholderItem, position: Int) {
        lifecycleScope.launch {
            val event = withContext(Dispatchers.IO) {
                // Načteme event podle ID z DAO
                dao.getEventById(item.id.toInt())
            }

            if (event != null) {
                curEv = event.id

                // Načteme členy spojené s touto událostí
                val membersInEvent = withContext(Dispatchers.IO) {
                    dao.getMembersForEvent(curEv)
                }

                // Naplnění dat do prvků v ED
                binding.apply {
                    ED.visibility = View.VISIBLE // Zobrazíme ED
                    EDName.text = event.eventName
                    EDStart.setText(event.starts) // Naplníme datum začátku
                    EDEnd.setText(event.ends) // Naplníme datum konce
                    EDStime.setText("8:00") // Naplníme čas začátku
                    EDEtime.setText("15:30") // Naplníme čas konce
                    EDMeetpoint.setText(event.meetingPoint) // Naplníme místo srazu
                    EDPacked.text = "S sebou: ${event.checklist}" // Naplníme položky
                    EDDetails.text = "Popis akce:\n${event.description}" // Popis akce
                }

                // Zobrazíme členy a zaškrtneme ty, kteří jsou v události
                withContext(Dispatchers.Main) {
                    val container = binding.memberCheckboxContainer
                    container.removeAllViews() // Vyčistíme staré checkboxy

                    val allMembers =withContext(Dispatchers.IO) { dao.getMembersByAccountId(famID) }
                    allMembers.forEach { member ->
                        var isChecked = membersInEvent.any { it.id == member.id }
                        val checkBox = CheckBox(requireContext()).apply {
                            text = "${member.name} (${member.birthday})"
                            id = member.id // Nastavíme ID člena
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            this.isChecked = isChecked // Zaškrtneme, pokud je člen přiřazen
                        }
                        container.addView(checkBox)
                    }
                }
            }

            binding.recycledViewEvents.visibility = View.GONE
            binding.ED.visibility = View.VISIBLE
        }
    }
}
