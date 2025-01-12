package com.example.litomicisystem.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.example.litomicisystem.LitostemDao
import com.example.litomicisystem.MemberItem
import com.example.litomicisystem.MemberListAdapter
import com.example.litomicisystem.R
import com.example.litomicisystem.SystemDatabase
import com.example.litomicisystem.databinding.FragmentProfileBinding
import com.example.litomicisystem.entities.Member
import com.example.litomicisystem.entities.UserLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Profile : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var famID: Int = 0
    private var userId: String? = null
    private var mems = listOf<Member>()
    private var memRecyclerView: RecyclerView? = null
    private lateinit var dao: LitostemDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            userId = it.getString("userID")
        }
        dao = SystemDatabase.getInstance(requireActivity()).litostemDao
        memRecyclerView = binding.memberList
            setProfileData()


        // Nastavení listeneru na tlačítko changeDataBTN
        binding.chaDataBTN.setOnClickListener {
            handleChaDataButton()
        }

        // Nastavení listeneru na tlačítko saveNewData
        binding.saveNewData.setOnClickListener {
            saveNewData()
        }
        // Nastavení listeneru na tlačítko New_member
        binding.addNewMember.setOnClickListener{
            NewMemberForm()
        }

        // Nastavení listeneru na tlačítko cancleNewData
        binding.NMCancle.setOnClickListener {
            resetMemberForm()
        }
        binding.NMSaver.setOnClickListener{
            AddNewMember()
        }
        binding.addLog.setOnClickListener{
            NewLogForm()
        }
        binding.NLCancle.setOnClickListener{
            NewLogReset()
        }
        binding.NLBTN.setOnClickListener{
            AddNewLog()
        }

    }
/*
*
* funkce přidá k aktuálnímu účtu nový přístup. podmínkou je platný email - obsahující @ a pak . a 2 zadaná hesla se shodují
*
* */
    private fun AddNewLog() {
        val email = binding.NLMail.text.toString().trim()
        val password1 = binding.NLPsswd1.text.toString().trim()
        val password2 = binding.NLPsswd2.text.toString().trim()

        // Validace e-mailu
        if (!validateMail(email)) {
            Toast.makeText(requireContext(), "Neplatný e-mail", Toast.LENGTH_SHORT).show()
            return
        }

        // Kontrola, zda se hesla shodují
        if (password1 != password2) {
            Toast.makeText(requireContext(), "Hesla se neshodují", Toast.LENGTH_SHORT).show()
            return
        }

        if (password1.isEmpty() || password2.isEmpty()) {
            Toast.makeText(requireContext(), "Heslo nesmí být prázdné", Toast.LENGTH_SHORT).show()
            return
        }

        // Vytvoření nového uživatele
        val newUserLog = UserLog(
            email = email,
            password = password1, // Doporučuje se hashovat heslo
            family2log = famID,  // Přiřadit k aktuální rodině
            position = 0         // Výchozí pozice uživatele
        )
        lifecycleScope.launch(Dispatchers.IO) {
            try {

                dao.insertUserLog(newUserLog) // Vložit do databáze

                // Přepnutí na hlavní vlákno pro UI změny
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Uživatel úspěšně vytvořen", Toast.LENGTH_SHORT).show()


                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Chyba při vytváření uživatele: ${e.message}", Toast.LENGTH_SHORT).show()
                    NewLogReset()
                }
                // Skrytí formuláře

            }
        }
    }
/*
* Zobrazí formulář pro přidání přístupu
* */
    private fun NewLogForm() {
        dispOff()
        binding.NLForm.visibility = View.VISIBLE

    }
    /*
    Vynuluje formulář a vrátí se na původní vzhled
    * */
    private fun NewLogReset(){
        binding.NLMail.text.clear()
        binding.NLPsswd1.text.clear()
        binding.NLPsswd2.text.clear()
        dispOn()
        binding.NLForm.visibility = View.GONE
    }
    /*
     * vytvoří ado databáze přidá člena
     * */
    private fun AddNewMember() {
        val name = binding.NMName.text.toString().trim()
        val lastName = binding.NMLastname.text.toString().trim()
        val bornDate = binding.NMBorn.text.toString().trim()

        // Step 1: Validate that all fields are filled
        if (name.isEmpty() || lastName.isEmpty() || bornDate.isEmpty()) {
            Toast.makeText(requireContext(), "Všechna pole musí být vyplněna.", Toast.LENGTH_SHORT).show()
            return
        }

        // Step 2: Validate the date format (DD/MM/YYYY)
        val dateRegex = "^([0-9]{2})/([0-9]{2})/([0-9]{4})$".toRegex()
        if (!bornDate.matches(dateRegex)) {
            Toast.makeText(requireContext(), "Datum musí být ve formátu DD/MM/YYYY.", Toast.LENGTH_SHORT).show()
            return
        }

        // Step 3: Create a new Member object
        val newMember = Member(
            name = name,
            lastName = lastName,
            birthday = bornDate,
            healthCondition = binding.NMHealth.text.toString(),
            gdpr = binding.NMGDPR.isChecked,
            familyID=famID
        )

        // Step 4: Insert the new member into the database in the background thread
        lifecycleScope.launch(Dispatchers.IO) {
            // Insert the member into the database
            dao.insertMember(newMember)

            // Inform the user that the member was successfully added
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Nový člen byl úspěšně přidán.", Toast.LENGTH_SHORT).show()
            }
        }
        setProfileData()
        // Reset the form (if you want to clear the input fields after adding the member)
        resetMemberForm()

    }
    /**
     * přehodí pohled hlavního obsahu pryč
     * */
    private fun dispOff(){
        binding.gridLayout.visibility=View.GONE
        binding.chaDataBTN.visibility=View.GONE
        binding.memberList.visibility=View.GONE
        binding.addNewMember.visibility=View.GONE
        binding.addLog.visibility=View.GONE
    }
    /**
     * přehodí pohled hlavního obsahu zpět
     * */
    private fun dispOn(){
        binding.gridLayout.visibility = View.VISIBLE
        binding.chaDataBTN.visibility = View.VISIBLE
        binding.memberList.visibility = View.VISIBLE
        binding.addNewMember.visibility = View.VISIBLE
        binding.addLog.visibility = View.VISIBLE
    }
    /**
    * zobrazí formulář pro přidání člena
     * */
    private fun NewMemberForm() {
        dispOff()
        binding.NMName.isEnabled=true
        binding.newMemberForm.visibility=View.VISIBLE
        binding.NMTools.visibility=View.VISIBLE



    }
    /**
     * vyčistí formulář a vrátí se na původní vzhled
    * */
    private fun resetMemberForm() {
        binding.NMName.text.clear()
        binding.NMLastname.text.clear()
        binding.NMBorn.text.clear()
        binding.NMHealth.setText("Zdravotní problémy:\nžádné nemá")
        binding.NMGDPR.isChecked = false
        dispOn()
        binding.newMemberForm.visibility = View.GONE
        binding.NMTools.visibility = View.GONE
    }

    /**
     *  umožní začít měnit kontaktní údaje účtu a členů s ním spojených
     * */
    private fun handleChaDataButton() {
        if (binding.chaDataBTN.text == "Změnit údaje") {
            enableEditTexts(true)
            binding.saveNewData.visibility = View.VISIBLE
            binding.chaDataBTN.text = "Zrušit"
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                resetProfileData()
            }
            enableEditTexts(false)
            binding.saveNewData.visibility = View.GONE
            binding.chaDataBTN.text = "Změnit údaje"
        }
    }

    /**
     * z databáze načte data profilu a zobrazí je
     * */
    private fun setProfileData() {
        lifecycleScope.launch(Dispatchers.IO) {
            if (userId != null) {
                val user = dao.getUserLogById(userId!!)
                if (user != null) {
                    famID = user.family2log!!
                }
            }

            // Načítání dat rodiny
            val family = dao.getFamilyById(famID)

            withContext(Dispatchers.Main) {
                if (family != null) {
                    binding.street.setText(family.street)
                    binding.city.setText(family.city)
                    binding.psc.setText(family.psc)
                    binding.phone.setText(family.phone)
                    binding.mail.setText(family.email)
                }
            }

            // Načítání a zobrazení členů rodiny
            mems = dao.getMembersByFamilyId(famID)
            val memberItems = mems.map { member ->
                MemberItem(
                    name = member.name,
                    lastName = member.lastName,
                    born = member.birthday,
                    hp = member.healthCondition,
                    gdprChecked = member.gdpr
                )
            }

            withContext(Dispatchers.Main) {
                val adapter = MemberListAdapter(memberItems.toMutableList())
                memRecyclerView?.adapter = adapter
                memRecyclerView?.layoutManager = LinearLayoutManager(requireActivity())
                adapter.notifyDataSetChanged()
            }
        }
    }
    /**
     * uloží změnu kontaktních údajů účtu
     * */
    private fun saveNewData() {
        lifecycleScope.launch {
            val street = binding.street.text.toString().trim()
            val city = binding.city.text.toString().trim()
            val psc = binding.psc.text.toString().trim()
            val phone = binding.phone.text.toString().trim()
            val email = binding.mail.text.toString().trim()
var t = "familyID = {$famID} a userID = {$userId}"
            if (street.isEmpty() || city.isEmpty() || psc.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), t, Toast.LENGTH_SHORT).show()
                return@launch
            }
            if (pscInvalid(psc)) {
                Toast.makeText(requireContext(), "nesprávné psc", Toast.LENGTH_SHORT).show()
                return@launch
            }
            if (phoneInvalid(phone)) {
                Toast.makeText(requireContext(), "Toto není telefonní číslo", Toast.LENGTH_SHORT).show()
                return@launch
            }
            if (!validateMail(email)) {
                Toast.makeText(requireContext(), "Email nebyl rozeznán.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            withContext(Dispatchers.IO) {
                val family = dao.getFamilyById(famID)
                if (family != null) {
                    val updatedFamily = family.copy(
                        street = street,
                        city = city,
                        psc = psc,
                        phone = phone,
                        email = email
                    )
                    dao.updateFamily(updatedFamily)
                }
            }

            Toast.makeText(requireContext(), "Údaje byly úspěšně uloženy.", Toast.LENGTH_SHORT).show()
            enableEditTexts(false)
            binding.saveNewData.visibility = View.GONE
            binding.chaDataBTN.text = "Změnit údaje"
        }
    }
    /**
    * odemkne edittexty pro možnost úprav
    * */
    private fun enableEditTexts(enable: Boolean) {
        binding.street.isEnabled = enable
        binding.city.isEnabled = enable
        binding.psc.isEnabled = enable
        binding.phone.isEnabled = enable
        binding.mail.isEnabled = enable
    }
    /**
    * ověření správné formy pro mail
    * */
    private fun validateMail(email: String): Boolean {
        val atIndex = email.indexOf('@')
        return atIndex > 0 && email.indexOf('.', atIndex) > atIndex
    }
    /**
    *  obnový kontaktní data z databáze
    * */
    private fun resetProfileData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val family = dao.getFamilyById(famID)
            withContext(Dispatchers.Main) {
                if (family != null) {
                    binding.street.setText(family.street)
                    binding.city.setText(family.city)
                    binding.psc.setText(family.psc)
                    binding.phone.setText(family.phone)
                    binding.mail.setText(family.email)
                }
            }
        }
    }
}
private fun pscInvalid(psc: String): Boolean {
    val validCharacters = "[0-9 ]+".toRegex()
    return !validCharacters.matches(psc)
}
private fun phoneInvalid(tel: String): Boolean {
    val validCharacters = "[0-9+ ]+".toRegex()
    return !validCharacters.matches(tel)
}

