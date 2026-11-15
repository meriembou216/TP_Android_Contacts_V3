package com.example.contactsappv3

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(
    private val onClick: (Contact) -> Unit     // ce qu'on fait quand on clique sur un contact
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private var contacts: List<Contact> = emptyList()

    // Le ViewHolder garde les vues d'UNE ligne (pour ne pas refaire findViewById à chaque fois)
    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPhoto: ImageView = view.findViewById(R.id.imgPhoto)
        val tvNomPrenom: TextView = view.findViewById(R.id.tvNomPrenom)
        val tvTelephone: TextView = view.findViewById(R.id.tvTelephone)
    }

    // 1) Créer une nouvelle ligne
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    // 2) Remplir une ligne avec un contact
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvNomPrenom.text = "${contact.nom} ${contact.prenom}"
        holder.tvTelephone.text = contact.telephone

        if (contact.photoUri != null) {
            holder.imgPhoto.setImageURI(Uri.parse(contact.photoUri))
        } else {
            holder.imgPhoto.setImageResource(R.drawable.ic_person)
        }

        // On envoie le CONTACT (donc son id), pas la position
        holder.itemView.setOnClickListener { onClick(contact) }
    }

    // 3) Nombre de lignes
    override fun getItemCount(): Int = contacts.size

    // Changer la liste affichée
    fun afficher(nouvelleListe: List<Contact>) {
        contacts = nouvelleListe
        notifyDataSetChanged()
    }
}