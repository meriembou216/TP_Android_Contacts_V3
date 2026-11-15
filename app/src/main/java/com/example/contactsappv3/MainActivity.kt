package com.example.contactsappv3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // On ajoute la liste SEULEMENT au premier lancement.
        // Après une rotation, Android recrée lui-même les fragments :
        // sans ce "if", on empilerait des fragments en double !
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListeContactsFragment())
                .commit()
        }
    }
}