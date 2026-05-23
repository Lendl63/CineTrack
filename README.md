# CineTrack

CineTrack est une application Android permettant de rechercher des series televisees et de suivre sa progression de visionnage. Elle utilise les donnees de la plateforme TMDB pour fournir des informations detaillees sur les oeuvres.

## Fonctionnalites

- Recherche de series via l'API TMDB.
- Consultation des synopsis, notes et statistiques (saisons, episodes).
- Gestion d'un journal de visionnage (note personnelle, avis et date).
- Interface utilisateur basee sur Material Design.

## Configuration de la Cle API TMDB

L'application necessite une cle API valide de The Movie Database pour fonctionner. Pour garantir la securite, cette cle n'est pas stockee directement dans le code source de l'application.

### Methode 1 : Fichier local.properties (Developpement)

Cette methode permet d'integrer la cle lors de la compilation de l'application. 
1. Ouvrez le fichier local.properties a la racine du projet.
2. Ajoutez la ligne suivante : TMDB_API_KEY=votre_cle_ici
3. Le systeme de build (Gradle) injectera automatiquement cette valeur dans la classe BuildConfig.

### Methode 2 : Reglages de l'application (Utilisateur)

Il est possible de configurer ou de modifier la cle directement depuis l'application :
1. Lancez l'application et accedez a l'ecran Settings.
2. Saisissez votre cle API dans le champ reserve.
3. La cle sera enregistree de facon persistante dans les SharedPreferences du telephone.
4. Cette cle saisie manuellement sera utilisee en priorite par rapport a celle definie dans local.properties.

## Technologies utilisees

- Langage : Java.
- Gestion d'images : Glide.
- API de donnees : TMDB (The Movie Database).
- Design : Material Components.
- Build system : Gradle (Kotlin DSL).

## Developpeur

Dessap Sa'a Yvan Lendl
