package com.example.contactsappv3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContactViewModel : ViewModel() {

    // La liste des contacts, partagée par tous les fragments
    private val _contacts = MutableLiveData(
        listOf(
            Contact(1, "Dupont", "Claire", "06 12 34 56 78", "claire.dupont@example.com",
                latitude = 33.8754, longitude = 10.8575),   // Djerba
            Contact(2, "Martin", "Thomas", "07 23 45 67 89", "thomas.martin@example.com",
                latitude = 36.8065, longitude = 10.1815),   // Tunis
            Contact(3, "Bernard", "Sophie", "06 98 76 54 32", "sophie.bernard@example.com",
                latitude = 34.7406, longitude = 10.7603),   // Sfax
            Contact(4, "Petit", "Lucas", "07 11 22 33 44", "lucas.petit@example.com",
                latitude = 48.8566, longitude = 2.3522),    // Paris
            Contact(5, "Moreau", "Julien", "06 55 44 33 22", "julien.moreau@example.com")
            // Julien n'a pas de position : pour tester le message d'erreur
        )
    )
    val contacts: LiveData<List<Contact>> = _contacts

    // Trouver un contact grâce à son id
    fun getContact(id: Int): Contact? = _contacts.value?.find { it.id == id }

    // Remplacer un contact modifié dans la liste
    fun mettreAJour(contactModifie: Contact) {
        _contacts.value = _contacts.value?.map {
            if (it.id == contactModifie.id) contactModifie else it
        }
    }
}