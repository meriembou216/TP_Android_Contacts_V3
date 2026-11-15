package com.example.contactsappv3

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import java.io.File

class DetailContactFragment : Fragment(R.layout.fragment_detail_contact) {

    // Création du fragment avec l'id dans un Bundle
    companion object {
        private const val ARG_ID = "contact_id"
        private const val KEY_PHOTO = "photo_en_cours"

        fun newInstance(id: Int): DetailContactFragment {
            val fragment = DetailContactFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_ID, id)
            fragment.arguments = bundle
            return fragment
        }
    }

    // Résultat de la validation GPS
    private data class ResultatGps(val valide: Boolean, val latitude: Double?, val longitude: Double?)

    private val viewModel: ContactViewModel by activityViewModels()
    private var contact: Contact? = null
    private var photoUri: String? = null
    private var photoChargee = false

    private lateinit var imgPhoto: ImageView
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText

    // ---------- Lanceurs ----------
    private val galerieLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            message("Aucune photo choisie")
            return@registerForActivityResult
        }
        try {
            val fichier = File(requireContext().filesDir, "photo_${System.currentTimeMillis()}.jpg")
            requireContext().contentResolver.openInputStream(uri)?.use { entree ->
                fichier.outputStream().use { sortie -> entree.copyTo(sortie) }
            }
            photoUri = Uri.fromFile(fichier).toString()
            afficherPhoto()
        } catch (e: Exception) {
            message("Impossible de lire cette image")
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap == null) {
            message("Photo annulée")
            return@registerForActivityResult
        }
        val fichier = File(requireContext().filesDir, "photo_${System.currentTimeMillis()}.jpg")
        fichier.outputStream().use { sortie ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, sortie)
        }
        photoUri = Uri.fromFile(fichier).toString()
        afficherPhoto()
    }

    private val permissionCameraLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { accordee: Boolean ->
        if (accordee) lancerCamera() else message("Permission caméra refusée")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgPhoto = view.findViewById(R.id.imgPhoto)
        etNom = view.findViewById(R.id.etNom)
        etPrenom = view.findViewById(R.id.etPrenom)
        etTelephone = view.findViewById(R.id.etTelephone)
        etEmail = view.findViewById(R.id.etEmail)
        etLatitude = view.findViewById(R.id.etLatitude)
        etLongitude = view.findViewById(R.id.etLongitude)

        // 1) On lit l'id dans le Bundle et on cherche le contact dans le ViewModel
        val id = requireArguments().getInt(ARG_ID)
        val c = viewModel.getContact(id)
        if (c == null) {
            message("Contact introuvable")
            parentFragmentManager.popBackStack()
            return
        }
        contact = c

        // 2) On remplit les champs à partir du modèle
        etNom.setText(c.nom)
        etPrenom.setText(c.prenom)
        etTelephone.setText(c.telephone)
        etEmail.setText(c.email)
        etLatitude.setText(c.latitude?.toString() ?: "")
        etLongitude.setText(c.longitude?.toString() ?: "")

        // La photo en cours de modification n'est chargée qu'une seule fois
        if (!photoChargee) {
            photoUri = if (savedInstanceState != null) {
                savedInstanceState.getString(KEY_PHOTO)
            } else {
                c.photoUri
            }
            photoChargee = true
        }
        afficherPhoto()

        // 3) Les boutons
        view.findViewById<Button>(R.id.btnAppeler).setOnClickListener { appeler() }
        view.findViewById<Button>(R.id.btnEmail).setOnClickListener { envoyerEmail() }
        view.findViewById<Button>(R.id.btnGalerie).setOnClickListener { choisirPhoto() }
        view.findViewById<Button>(R.id.btnCamera).setOnClickListener { prendrePhoto() }
        view.findViewById<Button>(R.id.btnEnregistrer).setOnClickListener { enregistrer() }
        view.findViewById<Button>(R.id.btnCarte).setOnClickListener { ouvrirCarte() }

        // Annuler : retour à la liste sans rien modifier
        view.findViewById<Button>(R.id.btnAnnuler).setOnClickListener {
            message("Modifications annulées")
            parentFragmentManager.popBackStack()
        }
    }

    // Sauvegarde de la photo en cours avant une rotation
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_PHOTO, photoUri)
    }

    // ---------- Validation GPS (utilisée par Enregistrer ET par la carte) ----------
    private fun validerGps(): ResultatGps {
        val latTexte = etLatitude.text.toString().trim().replace(",", ".")
        val lngTexte = etLongitude.text.toString().trim().replace(",", ".")

        // Les deux vides = pas de position : autorisé
        if (latTexte.isEmpty() && lngTexte.isEmpty()) return ResultatGps(true, null, null)

        if (latTexte.isEmpty() || lngTexte.isEmpty()) {
            if (latTexte.isEmpty()) etLatitude.error = "Latitude manquante"
            if (lngTexte.isEmpty()) etLongitude.error = "Longitude manquante"
            return ResultatGps(false, null, null)
        }

        var valide = true
        val latitude = latTexte.toDoubleOrNull()
        val longitude = lngTexte.toDoubleOrNull()
        if (latitude == null) {
            etLatitude.error = "Nombre attendu"; valide = false
        } else if (latitude < -90 || latitude > 90) {
            etLatitude.error = "Entre -90 et 90"; valide = false
        }
        if (longitude == null) {
            etLongitude.error = "Nombre attendu"; valide = false
        } else if (longitude < -180 || longitude > 180) {
            etLongitude.error = "Entre -180 et 180"; valide = false
        }
        return ResultatGps(valide, latitude, longitude)
    }

    // ---------- Voir sur la carte ----------
    private fun ouvrirCarte() {
        val gps = validerGps()
        if (!gps.valide) {
            message("Coordonnées GPS invalides : corrigez les champs en rouge")
            return
        }
        if (gps.latitude == null || gps.longitude == null) {
            message("Ce contact n'a pas de coordonnées GPS")
            return
        }
        val nomComplet = "${etPrenom.text.toString().trim()} ${etNom.text.toString().trim()}"
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                CarteContactFragment.newInstance(gps.latitude, gps.longitude, nomComplet)
            )
            .addToBackStack(null)     // Retour → revient au détail
            .commit()
    }

    // ---------- Enregistrer ----------
    private fun enregistrer() {
        val c = contact ?: return
        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val telephone = etTelephone.text.toString().trim()
        val email = etEmail.text.toString().trim()

        var valide = true
        if (nom.isEmpty()) { etNom.error = "Nom obligatoire"; valide = false }
        if (prenom.isEmpty()) { etPrenom.error = "Prénom obligatoire"; valide = false }
        if (telephone.isEmpty()) { etTelephone.error = "Téléphone obligatoire"; valide = false }
        if (email.isEmpty()) {
            etEmail.error = "E-mail obligatoire"; valide = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "E-mail invalide"; valide = false
        }

        val gps = validerGps()
        if (!gps.valide) valide = false

        if (!valide) {
            message("Veuillez corriger les champs en rouge")
            return
        }

        // On envoie une COPIE modifiée du contact au ViewModel
        viewModel.mettreAJour(
            c.copy(
                nom = nom, prenom = prenom, telephone = telephone, email = email,
                photoUri = photoUri, latitude = gps.latitude, longitude = gps.longitude
            )
        )
        message("Contact enregistré")
        parentFragmentManager.popBackStack()   // retour à la liste (déjà actualisée)
    }

    // ---------- Photo ----------
    private fun choisirPhoto() {
        try {
            galerieLauncher.launch("image/*")
        } catch (e: ActivityNotFoundException) {
            message("Aucune application galerie trouvée")
        }
    }

    private fun prendrePhoto() {
        if (!requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            message("Aucune caméra disponible")
            return
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            lancerCamera()
        } else {
            permissionCameraLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun lancerCamera() {
        try {
            cameraLauncher.launch(null)
        } catch (e: ActivityNotFoundException) {
            message("Aucune application caméra trouvée")
        }
    }

    private fun afficherPhoto() {
        if (photoUri != null) imgPhoto.setImageURI(Uri.parse(photoUri))
        else imgPhoto.setImageResource(R.drawable.ic_person)
    }

    // ---------- Appel / E-mail ----------
    private fun appeler() {
        val numero = etTelephone.text.toString().trim()
        if (numero.isEmpty()) { etTelephone.error = "Numéro obligatoire"; return }
        try {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numero")))
        } catch (e: ActivityNotFoundException) {
            message("Aucune application de téléphone trouvée")
        }
    }

    private fun envoyerEmail() {
        val adresse = etEmail.text.toString().trim()
        if (adresse.isEmpty()) { etEmail.error = "E-mail obligatoire"; return }
        try {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$adresse")))
        } catch (e: ActivityNotFoundException) {
            message("Aucune application de messagerie trouvée")
        }
    }

    private fun message(texte: String) {
        Toast.makeText(requireContext(), texte, Toast.LENGTH_SHORT).show()
    }
}