package com.example.litomicisystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.litomicisystem.databinding.ActivityLoggedProfileBinding
import com.example.litomicisystem.fragments.EventList
import com.example.litomicisystem.fragments.EventTools
import com.example.litomicisystem.fragments.IntroScreen
import com.example.litomicisystem.fragments.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoggedProfile : AppCompatActivity() {
    //viewbinding
    private lateinit var binding: ActivityLoggedProfileBinding
    private var userId: String? = null
    private var userRank: Int? = 0

    // Deklarace DAO
    private lateinit var dao: LitostemDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logged_profile)

        /*
        *
        * Settup při pnačítání
        *
        * */

        //Nastavení Binding
        binding = ActivityLoggedProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // fragmenty
        val profile_fragment = Profile()
        supportFragmentManager.beginTransaction()
            .replace(R.id.profileActivities, IntroScreen())
            .commit()


        //Init databaze
        dao = SystemDatabase.getInstance(this).litostemDao
        //Načtení dat z předchozí aktivity
        userId = intent.getStringExtra("user")


        // Iniciujeme ViewBinding
        val navbar = findViewById<LinearLayout>(R.id.navbar)
        checkUserRankAndSetVisibility()

        // Toggle navbar (zobrazit/skrytí)
        navbar.visibility = View.GONE
        //nastavení výchozího screenu


        /*
         *
         * Začátek programové logiky
         *
         * */

        //loggout
        binding.logout.setOnClickListener {
            // Přechod zpět na původní aktivitu
            val intent = Intent(this, MainActivity::class.java) // Původní aktivita
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Uzavře aktuální aktivitu
        }

        //ovládání navigačního menu
        binding.navButton.setOnClickListener {
            if (navbar.visibility == View.VISIBLE) {
                navbar.visibility = View.GONE
            } else {
                navbar.visibility = View.VISIBLE
            }
        }

        // otevřít Profile section
        binding.profileBtn.setOnClickListener {
            var tmp = Profile()
            var args = Bundle()
            args.putString("userID", userId)
            tmp.arguments = args
            supportFragmentManager.beginTransaction()
                .replace(R.id.profileActivities, tmp)
                .commit()

            //otevřít Event table section
            binding.events.setOnClickListener {
                var tmp = EventList()
                var args = Bundle()
                args.putString("userID", userId)
                tmp.arguments = args
                supportFragmentManager.beginTransaction()
                    .replace(R.id.profileActivities, tmp)
                    .commit()
            }
            //otevřít Eventtools table section
            binding.eventsTools.setOnClickListener {
                var tmp = EventTools()
                var args = Bundle()
                args.putString("userID", userId)
                tmp.arguments = args
                supportFragmentManager.beginTransaction()
                    .replace(R.id.profileActivities, tmp)
                    .commit()
            }


        }
    }
        /**
         * Funkce ověří posici uživatele. pokud je dotyčný 1 - vedoucí dostane přístup k nástroji pro úpravu plánu akcí
         *
         * */
        private fun checkUserRankAndSetVisibility() {
            // Zajistíme, že metoda je volána až po inicializaci bindingu
            if (!::binding.isInitialized || userId.isNullOrEmpty()) {
                binding.eventsTools.visibility = View.GONE
                return
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val tmpUser = userId?.let { dao.getUserLogById(it) }

                val isRankOne =
                    tmpUser?.position == 1 // Pokud je tmpUser null, isRankOne bude false

                withContext(Dispatchers.Main) {
                    binding.eventsTools.visibility = if (isRankOne) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            }
        }
}

