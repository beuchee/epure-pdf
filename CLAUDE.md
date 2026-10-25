# CLAUDE.md — Épure PDF

Consignes pour l'assistant de code. À lire en entier au début de chaque session.

## 1. Avant toute tâche

1. Lire `docs/ROADMAP.md` : identifier le jalon et la session en cours.
2. Lire dans `docs/SPEC.md` les exigences de cette session.
3. Lire `docs/DECISIONS.md` (décisions prises) et `docs/CHARTE.md` (règles visuelles et d'interaction).
4. Ne travailler **que** sur le jalon et la session en cours. Ne pas anticiper les jalons suivants.

## 2. Projet

Lecteur PDF Android minimaliste, entièrement hors ligne. Lire sans distraction, sans réseau, sans collecte de données.

- Nom affiché « Épure PDF » ; nom technique `EpurePdf` ; `applicationId` `com.alain.epure.pdf`.
- Licence GPL v3. Dépôt public `epure-pdf`.
- Application distincte du launcher Épure : aucune dépendance de code entre les deux.
- Le porteur du projet n'est pas développeur Android de métier : expliquer clairement, sans jargon inutile, ce qui a été fait et pourquoi.

## 3. Choix techniques figés (DEC-0004)

- Kotlin uniquement ; aucun code natif ajouté (pas de NDK, pas de Rust).
- Jetpack Compose, Material 3, thème sombre personnalisé (trois jetons : `Black`, `TextPrimary`, `TextSecondary`).
- `minSdk` 31 ; `targetSdk` / `compileSdk` 36.
- Une seule activité ; `ViewModel` + `StateFlow` ; navigation par état simple (pas de `navigation-compose`) ; pas de framework d'injection (`AppContainer`).
- Paquets : `ui/` (écrans), `data/` (persistance), `system/` (API Android, moteur de rendu, SAF, intents), `domain/` (logique pure, sans dépendance Android, testée).
- Le moteur de rendu est isolé derrière une interface dans `system/`.
- `allowBackup="false"`.
- Edge-to-edge géré par `WindowInsets` ; retour par `BackHandler`, jamais `onBackPressed()`.

## 4. Règles absolues

### Aucune permission `INTERNET` (DEC-0005)

- `INTERNET` et `ACCESS_NETWORK_STATE` absentes du manifeste source **et** du manifeste fusionné.
- Après tout ajout ou mise à jour de dépendance, contrôler :
  ```
  ./gradlew assembleDebug
  $ANDROID_HOME/build-tools/<version>/aapt2 dump permissions app/build/outputs/apk/debug/app-debug.apk
  ```
  (`aapt2` : `$ANDROID_HOME/build-tools/<version>/`, DEC-0032). Résultat attendu : aucune permission, sauf celles du tableau `SPEC.md` §8.
- Permission injectée par une bibliothèque : la signaler, puis la retirer avec `tools:node="remove"` ou écarter la bibliothèque, avec entrée dans `DECISIONS.md`.

### Fichiers et permissions (DEC-0006, DEC-0007, DEC-0008)

- Accès aux fichiers uniquement par le sélecteur système (`ACTION_OPEN_DOCUMENT`) et l'intent `VIEW`.
- Interdits : `MANAGE_EXTERNAL_STORAGE`, `READ_EXTERNAL_STORAGE`, `READ_MEDIA_*`, `QUERY_ALL_PACKAGES`, `SYSTEM_ALERT_WINDOW`, `POST_NOTIFICATIONS`, `WAKE_LOCK`.
- Aucun `Service`, aucun `WorkManager`, aucune notification.
- Aucune bibliothèque d'analytics, de suivi de plantage ou de publicité.
- Le contenu des documents n'est pas copié. Seuls sont stockés : URI, métadonnées des récents, position de lecture, marque-pages, réglages.

### Dépendances

- Toute nouvelle bibliothèque est **proposée** puis justifiée dans `DECISIONS.md` (contexte, alternative, licence, compatibilité GPL v3, effet sur le manifeste fusionné) **avant** d'être ajoutée. Ne jamais l'ajouter de sa propre initiative.
- Solution simple d'abord ; API Android intégrée de préférence à une bibliothèque.

## 5. Méthode

- Un point `[À DÉCIDER]` du `SPEC.md` n'est **jamais** implémenté comme s'il était tranché. Le signaler, proposer les options, attendre la décision consignée dans `DECISIONS.md`.
- Un point `[À VÉRIFIER]` se teste sur l'appareil ; proposer le protocole de test, ne pas présumer du résultat.
- Ne pas modifier `SPEC.md` pour l'aligner sur le code : signaler l'écart.
- Ne pas modifier `CHARTE.md` ; toute dérogation passe par `DECISIONS.md`.
- Ne pas cocher une tâche de `ROADMAP.md` : elle n'est cochée qu'après validation sur l'appareil par le porteur du projet.
- Signaler clairement les limites des API Android (notamment PdfRenderer selon la version d'Android), les particularités de One UI et les contraintes du Play Store.

## 6. Interface (résumé de `CHARTE.md` A)

- Fond `Black` partout ; texte `TextPrimary` ou `TextSecondary` ; aucune couleur d'accent ; aucune couleur en dur dans un écran.
- Pages PDF dans leurs couleurs d'origine (inversées en mode nuit).
- Texte seul : pas d'icône, pas de pictogramme, sauf décision consignée.
- Aucune animation décorative ; apparitions et disparitions immédiates ; pas d'indicateur de chargement animé.
- Tailles de texte en `sp` ; cibles tactiles ≥ 48 dp ; libellé TalkBack pour tout élément interactif.
- Composants types de la charte (action en liste, fenêtre de menu, boîte de saisie, liste de réglages) réutilisés, pas réinventés.
- Toutes les chaînes dans `res/values/strings.xml` ; français, tutoiement, ton neutre ; libellés d'action à l'infinitif.
- Aucun plantage : toute erreur (fichier absent, droit retiré, PDF mal formé, mémoire) affiche un message en texte.

## 7. Code

- Identifiants et commentaires en **anglais**.
- Logique pure dans `domain/`, avec tests unitaires (cycle de zoom, page courante, résolution de rendu, cache, limites).
- Rendu des pages hors du fil principal ; un seul rendu à la fois avec PdfRenderer (une page ouverte à la fois, pas d'appels concurrents).
- Cache d'images de pages borné en octets ; libérer les bitmaps non visibles.

## 8. Commandes

```
./gradlew assembleDebug          # build
./gradlew testDebugUnitTest      # unit tests
./gradlew lint                   # lint
./gradlew installDebug           # install on connected device
```
Sous Windows : `gradlew.bat` au lieu de `./gradlew`.

## 9. Fin de tâche (`CHARTE.md` A.13)

1. Build, tests et lint sans erreur (pas de nouvelle erreur de lint).
2. Contrôle `aapt2 dump permissions` des permissions.
3. Donner au porteur du projet la liste de contrôle à passer sur l'appareil (reprise de `ROADMAP.md`).
4. Proposer un message de commit, sans committer avant validation sur l'appareil.

## 10. Git et dépôt public

- Jamais versionnés : `*.jks`, `*.keystore` et leurs mots de passe, `local.properties`, informations personnelles, PDF personnels ou sous droits d'auteur, captures de produits tiers.
- Avant chaque commit : `git status` et contenu indexé relus.
- Commit seulement après validation sur l'appareil ; adresse e-mail « noreply » de GitHub.
- Messages de commit en anglais, courts, à l'impératif (ex. `Add continuous page scrolling`).
- Pas de `git push`, pas de réécriture d'historique, pas de `--force` sans demande explicite.
- Ressources tierces seulement sous licence compatible (Google Fonts, Material Symbols, Apache 2.0…) ; origine et avis de copyright conservés.
