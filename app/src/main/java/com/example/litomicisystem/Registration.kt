package com.example.litomicisystem

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.litomicisystem.databinding.ActivityMainBinding
import com.example.litomicisystem.databinding.ActivityRegistrationBinding
import com.example.litomicisystem.entities.Family
import com.example.litomicisystem.entities.UserLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Registration : AppCompatActivity() {

    // Deklarace DAO
    private lateinit var dao: LitostemDao

    // ViewBinding
    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializace DAO

        dao = SystemDatabase.getInstance(this).litostemDao

        // Listener pro návrat do logovací obrazovky
        binding.back2log.setOnClickListener {
            goBackToLog()
        }

        // Listener pro dokončení registrace
        binding.regFinish.setOnClickListener {
            if (checkValues()) {
                registerUser()
            } else {
                Toast.makeText(this, "Zkontrolujte zadané údaje", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Funkce pro návrat do přihlašovací obrazovky
    private fun goBackToLog() {
        val intent = Intent(this, MainActivity::class.java) // Ujistěte se, že máte správnou třídu pro login
        startActivity(intent)
        finish() // Zavřete tuto aktivitu
    }

    // Funkce pro registraci uživatele
    private fun registerUser() {
        val email = binding.NewMail.text.toString()
        val password = binding.newPassword.text.toString()
        val address = binding.NewAddr.text.toString()
        val city = binding.NewCity.text.toString()
        val psc = binding.NewPSC.text.toString()
        val tel = binding.NewCtel.text.toString()
        val cmail=binding.NewCMail.text.toString()
        //kontrola vstupu
        //telefoní číslo
        if(phoneInvalid(tel)){
            Toast.makeText(this@Registration, "Chyba při registraci: neplatné nebo špatné telefoní číslo", Toast.LENGTH_LONG).show()
            return
        }
        //psč
        if(pscInvalid(psc)){
            Toast.makeText(this@Registration, "Chyba při registraci: neplatné nebo špatné PSČ", Toast.LENGTH_LONG).show()
            return
        }
        //mail
        if(!validateMail(email)){
            Toast.makeText(this@Registration, "Chyba při registraci: neplatný nebo špatný email", Toast.LENGTH_LONG).show()
            return
        }
        if(!validateMail(cmail)){
            Toast.makeText(this@Registration, "Chyba při registraci: neplatný nebo špatný krizový mmail", Toast.LENGTH_LONG).show()
            return
        }
        lifecycleScope.launch(Dispatchers.IO) { // Spouštíme na Dispatchers.IO
            try {
                // Vytvoření rodiny
                val family = Family(
                    phone = tel,
                    street = address,
                    city = city,
                    psc = psc,
                    email = cmail
                )

                // Vkládáme rodinu do databáze a získáváme ID
                val familyId = dao.insertFamily(family)
                var tmpID:Int=0
                tmpID= dao.getFamilyWithMaxId()?.id!!


                // Vytvoření uživatelského logu s použitím ID rodiny
                val userLog = UserLog(
                    email = email,
                    password = password,
                    position = 0,
                    family2log = tmpID // Používáme ID vrácené z insert metody
                )

                // Vkládáme uživatelský log
                var tmpur = dao.insertUserLog(userLog)
                var compur = dao.getUserLogById("t@t.tt")

                    // Přepnutí zpět na hlavní vlákno pro práci s UI
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Registration, "Registrace proběhla úspěšně. Nyní se přihlašte!", Toast.LENGTH_LONG).show()
                    goBackToLog() // Vracíme se na přihlašovací obrazovku
                }
            } catch (e: Exception) {
                // Zpracování případné chyby
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Registration, "Chyba při registraci: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun validateMail(email: String): Boolean {
        val atIndex = email.indexOf('@')
        return atIndex > 0 && email.indexOf('.', atIndex) > atIndex
    }
    private fun pscInvalid(psc: String): Boolean {
        val validCharacters = "[0-9 ]+".toRegex()
        return !validCharacters.matches(psc)
    }

    private fun phoneInvalid(tel: String): Boolean {
        val validCharacters = "[0-9+ ]+".toRegex()
        return !validCharacters.matches(tel)
    }


    // Funkce pro ověření hodnot
    private fun checkValues(): Boolean {
        return binding.NewMail.text.isNotEmpty() &&
                binding.newPassword.text.isNotEmpty() &&
                binding.newPassword2.text.toString() == binding.newPassword.text.toString() &&
                binding.NewAddr.text.isNotEmpty() &&
                binding.NewCity.text.isNotEmpty() &&
                binding.NewPSC.text.isNotEmpty() &&
                binding.NewCMail.text.isNotEmpty() &&
                binding.NewCtel.text.isNotEmpty() &&
                binding.NewGDPR.isChecked
    }
}
