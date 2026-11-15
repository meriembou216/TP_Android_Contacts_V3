package com.example.contactsappv3

data class Contact(
    val id: Int,                     // identifiant unique, ne change jamais
    var nom: String,
    var prenom: String,
    var telephone: String,
    var email: String,
    var photoUri: String? = null,
    var latitude: Double? = null,    // null = pas de position
    var longitude: Double? = null
)