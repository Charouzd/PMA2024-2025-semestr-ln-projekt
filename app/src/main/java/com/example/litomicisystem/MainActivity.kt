package com.example.litomicisystem

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.litomicisystem.databinding.ActivityMainBinding
import com.example.litomicisystem.entities.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    // Deklarace DAO
    private lateinit var dao: LitostemDao

    // Shared preferences
    private val user_session by lazy {
        getSharedPreferences("logged_user_session", Context.MODE_PRIVATE)
    }
    private val session_editor by lazy { user_session.edit() }

    // ViewBinding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializace bindingu
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializace DAO
        dao = SystemDatabase.getInstance(this).litostemDao

        // Spuštění vložení dat pozor provede se pokaždém načtení mainactivity, takže i při logoutu...
        // potřeba spustit poprvé a pak vypnout
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val eventCount = dao.getAllEvents().size
                val userLogCount = dao.getAllUserLogs().size

                if (eventCount == 0 || userLogCount == 0) {
                    db_reset(dao)
                    put_in_data(dao)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Chyba při inicializaci DB", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Inicializace bindingu
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Nastavení obsahu z bindingu (namísto setContentView())
        setContentView(binding.root)

        // Nastavení listeneru pro tlačítko přihlášení
        binding.loginButton.setOnClickListener {
            // Získání vstupních údajů
            val username = binding.username.text.toString().trim()
            val password = binding.password.text.toString().trim()

            // 1. Kontrola, jestli jsou pole prázdná
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Chybí přihlašovací údaje", Toast.LENGTH_SHORT).show()
            } else {
                // Spuštění koroutiny pro ověření přihlášení
                lifecycleScope.launch {
                    val isAuthenticated = withContext(Dispatchers.IO) {
                        user_log_auth(username, password)
                    }
                    if (isAuthenticated) {
                        // Zápis do sessionu
                        val userStatus = withContext(Dispatchers.IO) {
                            dao.getUserLogById(username)?.position?.toInt() ?: 0
                        }
                        session_editor.putBoolean("auth", true) // Uložení hodnoty true pro auth
                        session_editor.putString("user", username) // Uložení username
                        session_editor.putInt("user_status", userStatus) // Převod Short na Int
                        session_editor.apply()

                        // Přesměrování na novou aktivitu
                        val intent = Intent(this@MainActivity, LoggedProfile::class.java).also { it.putExtra("user",username) } // Zavolání LoggedProfile aktivity
                        startActivity(intent) // Spuštění aktivity
                    } else {
                        // Pokud jsou údaje nesprávné, zobrazí se chybová hláška
                        Toast.makeText(this@MainActivity, "Špatné přihlašovací údaje", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

//register button
binding.registerButton.setOnClickListener{
    val intent = Intent(this, Registration::class.java)  // Zavolání LoggedProfile aktivity
    startActivity(intent)  // Spuštění aktivity
}
    }
    /*
    * autorizace uživatele do aplikace. ověření, zda takový uživatel v databázi je a pokud ano tak pomocí hesla ověří zda je to on.
    *
    * Input:
    *   username: String - uživatelův email
    *   password: String - uživatelovo heslo
    * Output: Boolean - true => podařilo se ověřit; false => neexistuje nebo jméno a heslo nesedí
    * */
    private fun user_log_auth(username: String, password: String): Boolean {
        val tmp = dao.getUserLogById(username) // Získání uživatele podle username synchronně
        return tmp != null && tmp.password == password // Porovnání hesla
    }

    /*
    * Reset dat z databáze. Postupně smaže data všech tabulek.
    *
    * Input:  Instance dao Litostem
    * Output: None
    * */
    private fun db_reset(dao: LitostemDao) {
        dao.deleteAllFamilies()
        dao.deleteAllUserLogs()
        dao.deleteAllEvents()
        dao.deleteAllMembers()
        dao.deleteAllMemberEventCrossRefs()
        dao.resetFamilyAutoIncrement()
        dao.resetUserLogAutoIncrement()
        dao.resetEventAutoIncrement()
        dao.resetMemberAutoIncrement()
        dao.resetMemberEventCrossRefAutoIncrement()
        dao.fullClear()

    }
    /*
    * Vytvoří před nějaké data. primárně pro účel testování je nutnté tuto funci zavolat pouze jednou
    *
    * Input:  Instance dao Litostem
    * Output: None
    * */
    private suspend fun put_in_data(dao: LitostemDao) {
        val family = Family(
            phone = "123456789",
            street = "Main Street",
            city = "Prague",
            psc = "11000",
            email = "example@example.com"
        )
        val efamily = Family(
            phone = "666999666",
            street = "problemek",
            city = "nefunguje",
            psc = "666",
            email = "fail@error.null"
        )

        val users = listOf(
            UserLog("sibik@seznam.cz", "heslo123", 0, 1),
            UserLog("host123", "12345", 1, 1)
        )

        val eventList = listOf(
            Event(
                eventName = "Mountain Hiking Adventure",
                starts = "2025-05-01 08:00:00",
                ends = "2025-05-01 18:00:00",
                meetingPoint = "Main Square, City Center",
                price = 49.99f,
                capacity = 20,
                departure = " ",
                destination = " " ,
                checklist = "Hiking boots, Water, Snacks, Jacket",
                description = "An exciting day hike through the scenic High Tatras. Suitable for all skill levels.",
                type = 1
            ),
            Event(
                eventName = "Wine Tasting Tour",
                starts = "2025-06-10 10:00:00",
                ends = "2025-06-10 17:00:00",
                meetingPoint = "Central Station, Platform 3",
                price = 75.0f,
                capacity = 15,
                departure = "",
                destination = "",
                checklist = "Comfortable clothing, Notebook for tasting notes",
                description = "A day of wine tasting in the beautiful Moravian countryside. Includes transport and lunch.",
                type = 2
            ),
            Event(
                eventName = "Beach Yoga Retreat",
                starts = "2025-07-20 06:00:00",
                ends = "2025-07-20 20:00:00",
                meetingPoint = "Yoga Studio Downtown",
                price = 89.99f,
                capacity = 10,
                departure = "",
                destination = "",
                checklist = "Yoga mat, Towel, Sunscreen, Water bottle",
                description = "A rejuvenating day of yoga on the beach, with guided sessions and time to relax by the sea.",
                type = 3
            )
        )

        // Vložení dat do databáze
        dao.insertFamily(family)
        dao.insertFamily(efamily)
        val familyId = 1
        val mem = Member(
            name = "Filip",
            lastName = "Charouzd",
            birthday = "16/09/2000",
            gdpr = true,
            familyID = familyId,
            healthCondition = "Astma, alergie na Kiwi, prach a břízu"
        )

        users.forEach { dao.insertUserLog(it) }
        eventList.forEach { dao.insertEvent(it) }
       dao.insertMember(mem)
    }
}