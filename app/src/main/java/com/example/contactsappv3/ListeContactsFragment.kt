package com.example.contactsappv3

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListeContactsFragment : Fragment(R.layout.fragment_liste_contacts) {

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter

    // 🆕
    private lateinit var etRecherche: EditText
    private lateinit var tvAucunResultat: TextView
    private var tousLesContacts: List<Contact> = emptyList()   // liste COMPLÈTE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etRecherche = view.findViewById(R.id.etRecherche)               // 🆕
        tvAucunResultat = view.findViewById(R.id.tvAucunResultat)       // 🆕

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerContacts)
        adapter = ContactAdapter { contact -> ouvrirDetail(contact) }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
        recycler.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        // Quand la liste change dans le ViewModel : on la garde et on refiltre
        viewModel.contacts.observe(viewLifecycleOwner) { liste ->
            tousLesContacts = liste
            filtrer()                                                   // 🆕
        }

        // 🆕 À chaque modification du texte, on refiltre (pas de bouton)
        etRecherche.doAfterTextChanged { filtrer() }
    }

    // 🆕 Le filtre
    private fun filtrer() {
        val texte = etRecherche.text.toString().trim()

        val resultat = if (texte.isEmpty()) {
            tousLesContacts                       // texte vide → tous les contacts
        } else {
            val texteSansEspaces = texte.replace(" ", "")
            tousLesContacts.filter { c ->
                c.nom.contains(texte, ignoreCase = true) ||
                        c.prenom.contains(texte, ignoreCase = true) ||
                        "${c.prenom} ${c.nom}".contains(texte, ignoreCase = true) ||
                        "${c.nom} ${c.prenom}".contains(texte, ignoreCase = true) ||
                        c.telephone.replace(" ", "").contains(texteSansEspaces)
            }
        }

        adapter.afficher(resultat)

        // Message si aucun contact ne correspond
        tvAucunResultat.visibility = if (resultat.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun ouvrirDetail(contact: Contact) {
        // Navigation dynamique : on REMPLACE la liste par le détail
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DetailContactFragment.newInstance(contact.id))
            .addToBackStack(null)     // le bouton Retour reviendra à la liste
            .commit()
    }
}