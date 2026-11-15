# TP Android Contacts V3 – Bouaoun Meriem (L3DSI)

Application Android (Kotlin, XML) de gestion de contacts :
une seule activité (MainActivity) et trois fragments dynamiques.

## Fonctionnalités
- Liste des contacts (RecyclerView) avec recherche en temps réel (nom, prénom, téléphone, sans tenir compte de la casse)
- Détail et modification d'un contact (Bundle + ViewModel partagé)
- Appeler, envoyer un e-mail, choisir une photo dans la galerie, prendre une photo
- Coordonnées GPS (latitude/longitude) avec validation
- Affichage de la position du contact sur Google Maps (CarteContactFragment)

## Architecture
- `MainActivity` : héberge les fragments dans un `FragmentContainerView`
- `ListeContactsFragment` : liste + filtrage
- `DetailContactFragment` : consultation, modification, actions
- `CarteContactFragment` : carte Google Maps avec marqueur
- `ContactViewModel` : liste partagée entre les fragments (LiveData)

## Configuration de la clé Google Maps
La clé API n'est **pas** publiée dans ce dépôt.
1. Créer une clé « Maps SDK for Android » dans Google Cloud
2. Ajouter dans le fichier `local.properties` (à la racine du projet, ignoré par Git) :
   MAPS_API_KEY=VOTRE_CLE_ICI
3. Synchroniser Gradle et lancer l'application

Sans clé, l'application fonctionne mais affiche un message
« Clé API Google Maps non configurée » sur l'écran de la carte.