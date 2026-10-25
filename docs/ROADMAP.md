# Feuille de route — Épure PDF

> Version 0.1 — 24 octobre 2026
> Référence : `SPEC.md` 0.1, `CHARTE.md` 1.0.
> Règles : une tâche n'est cochée qu'une fois **validée sur l'appareil** (Samsung, Android 16 / One UI 8.0). Un jalon n'est commencé que lorsque le précédent est terminé. Une session Claude Code par session listée ci-dessous. Commit après chaque étape validée.
> Les décisions préalables d'un jalon (« À trancher avant ») sont consignées dans `DECISIONS.md` **avant** l'implémentation correspondante.

---

## État d'avancement

| Jalon | Contenu | État |
|---|---|---|
| 0 | Mise en place du projet, dépôt, licence, documents | À faire |
| 1 | Lecteur de base : moteur, ouverture par le sélecteur, défilement, zoom | À faire |
| 2 | Ouverture depuis d'autres apps, récents, reprise, « À propos » | À faire |
| 3 | Confort : commandes, mode nuit, indicateur et saut de page, écran allumé, partage | À faire |
| 4 | Marque-pages, recherche, table des matières (selon le moteur) | À faire |
| 5 | Optimisation : gros fichiers, mémoire, PDF protégés ou mal formés | À faire |
| — | Autres formats (EPUB, CBZ, CBR) | Hors feuille de route, `[À DÉCIDER]` (`SPEC.md` F-16) |

---

## Contrôles communs à toutes les tâches (`CHARTE.md` A.13)

1. `./gradlew assembleDebug` sans erreur.
2. `./gradlew testDebugUnitTest` au vert.
3. `./gradlew lint` sans nouvelle erreur.
4. Manifeste fusionné sans `INTERNET` : `apkanalyzer manifest permissions app/build/outputs/apk/debug/app-debug.apk` ne liste aucune permission non prévue (`SPEC.md` §8).
5. Liste de contrôle du jalon passée sur l'appareil.
6. `git status` et contenu indexé relus ; commit en anglais, à l'impératif.

---

## Jalon 0 — Mise en place

Session unique (0).

- [x] Créer le projet Android Studio : Kotlin, Compose, `minSdk` 31, `targetSdk` / `compileSdk` 36, `applicationId` `com.alain.epure.pdf`, nom technique `EpurePdf`.
- [x] `app_name` = « Épure PDF » dans `strings.xml`.
- [x] Reprendre le thème d'Épure : `Color.kt` (trois jetons), `Theme.kt` (schéma sombre), `Type.kt` (échelle en `sp`), `themes.xml` (fond noir, pas de flash blanc).
- [x] Créer les paquets `ui/`, `data/`, `system/`, `domain/` et la classe `AppContainer`.
- [x] Manifeste : une seule activité, `allowBackup="false"`, aucune permission.
- [x] `LICENSE` (GPL v3) et `.gitignore` Android vérifié (`*.jks`, `*.keystore`, `local.properties`, `*.pdf` hors dossier de test autorisé).
- [x] `docs/` : `SPEC.md`, `ROADMAP.md`, `DECISIONS.md` (modèle d'entrée en tête), `CHARTE.md` (Partie A reprise, Partie B propre à Épure PDF).
- [x] `CLAUDE.md` à la racine : choix techniques figés, règle « aucune permission `INTERNET` » avec la commande de contrôle, règles du dépôt public, commandes de build, consigne de lire `SPEC.md` et `ROADMAP.md` avant toute tâche, travail limité au jalon en cours.
- [x] README : présentation, section Confidentialité (pas d'accès à Internet, permission absente du manifeste, données sur l'appareil), licence.
- [x] Constituer le jeu de PDF de test **hors du dépôt** (`SPEC.md` annexe A).
- [x] Premier build, tests, lint ; app vide lancée sur l'appareil (écran noir, aucun flash blanc).
- [x] Premier commit (adresse « noreply »). Pas de `git push` sans demande explicite.

**Liste de contrôle sur l'appareil**
- [x] L'app s'installe et s'ouvre sur un écran noir.
- [x] « Infos sur l'application » (One UI) → « Autorisations » : aucune autorisation listée.

---

## Jalon 1 — Lecteur de base

**À trancher avant** : moteur de rendu (`SPEC.md` §6, à l'issue de 1a) ; zoom maximal (F-3.1) ; cycle du double toucher (F-3.2) ; verrouillage d'orientation (F-2.6).

### Session 1a — Prototype comparatif des moteurs
- [x] Prototype jetable (branche séparée) avec PdfRenderer (option A).
- [x] Même prototype avec androidx.pdf (option B) : statut, `minSdk`, dépendances, manifeste fusionné.
- [x] Mesures sur le jeu de test : poids de l'APK, temps d'ouverture (documents 1, 2, 9), mémoire, fidélité du rendu (1, 3, 4, 10).
- [x] Vérifier sur l'appareil : recherche de texte, mot de passe, sommaire, pour chaque option.
- [x] Option C étudiée seulement si A et B sont insuffisants (licence, poids, code natif) — non nécessaire : A suffit (DEC-0015).
- [x] Entrée `DECISIONS.md` : moteur retenu, mesures, fonctions conditionnelles F-11, F-13, F-14.1 confirmées ou écartées.
- [x] Mettre à jour `SPEC.md` (retirer le `[À DÉCIDER]` du §6, mettre à jour les `[À VÉRIFIER]` tranchés).

### Session 1b — Ouverture et affichage
- [x] Interface du moteur dans `system/` (le reste de l'app n'en dépend pas directement).
- [x] Accueil minimal : titre, action « Ouvrir un fichier » (F-1.1).
- [x] Sélecteur système filtré sur `application/pdf` ; autorisation persistante demandée.
- [x] Lecteur : défilement vertical continu, ajustement à la largeur, espace noir entre pages (F-2.1 à F-2.3).
- [x] Rendu à la demande, hors fil principal, page vide de bonne proportion pendant le rendu (F-2.5).
- [x] Cache d'images borné, valeur provisoire (T-6).
- [x] Plein écran immersif (F-2.4).
- [x] Retour : ramène à l'accueil (F-6.4, version simple).
- [x] Messages d'erreur de base, sans plantage (F-1.5).
- [x] Rotation : page conservée (F-2.6).

### Session 1c — Zoom
- [x] Pincement entre ×1 et le maximum décidé, déplacement libre (F-3.1).
- [x] Double toucher : cycle décidé, cadrage sur la colonne touchée (F-3.2).
- [x] Nouveau rendu net après le geste (F-3.3).
- [x] Tests unitaires `domain/` : cycle de zoom, calcul de la résolution de rendu, page courante.

**Liste de contrôle sur l'appareil (jalon 1)**
- [x] Ouvrir le document 1 depuis « Mes fichiers » via le sélecteur : affichage correct, défilement fluide.
- [x] Double toucher sur la colonne droite du document 1 : la colonne remplit l'écran ; second double toucher : retour à la largeur.
- [x] Pincement jusqu'au maximum : texte net après le geste.
- [x] Document 4 : pages de formats mixtes non déformées.
- [x] Document 2 (long) : défilement sans plantage jusqu'à la dernière page.
- [x] Document 8 (non PDF) : message, pas de plantage.
- [x] Rotation au milieu d'un document : même page.
- [x] Annuler le sélecteur : retour à l'accueil sans message.
- [ ] Aucune permission dans « Infos sur l'application » ; contrôle `apkanalyzer` passé.

---

## Jalon 2 — Ouverture depuis d'autres apps, récents, reprise

**À trancher avant** : `ACTION_SEND` et `launchMode` (F-1.2) ; récent illisible (F-1.3) ; copie temporaire (F-1.4) ; taille du nom, nombre de récents, confirmation du retrait, identification secondaire (F-4.1, F-4.2, F-4.4, F-4.5) ; mémorisation du zoom (F-5.3) ; DataStore ou Room (§7).

### Session 2a — Intent `VIEW`
- [x] Filtre `VIEW` `application/pdf`, schéma `content` (F-1.2) ; `SEND` si décidé.
- [ ] Vérifier sur l'appareil la persistance des droits et le type de descripteur selon la source (F-1.3, F-1.4) ; consigner les résultats.
- [ ] Copie temporaire ou message, selon la décision (F-1.4).
- [x] Retour depuis le lecteur ouvert par `VIEW` : retour à l'app d'origine.

### Session 2b — Récents et reprise
- [x] Persistance (`data/`, DataStore, DEC-0027) : dernier document, position de lecture.
- [x] Accueil : le dernier document ouvert, avec « p. X / N », sous « Ouvrir un fichier » ; aucune entrée si rien n'a été ouvert (F-4.1, DEC-0024).
- [x] Menu d'appui long : « Retirer de la liste » (libère l'autorisation, efface la position ; le fichier reste) (F-4.4, DEC-0025). « Partager » viendra au jalon 3.
- [x] Récent illisible : message « Fichier inaccessible… », entrée conservée (F-1.3, DEC-0021).
- [x] Reprise à la dernière position, écriture différée après une pause de lecture (F-5).
- [x] Tests unitaires : identification par nom, position hors limites (F-5.5).

### Session 2c — Paramètres et « À propos »
- [x] Écran Paramètres minimal avec « À propos » (F-15.2) : version, licence, dépôt, confidentialité, moteur et sa licence.
- [x] Réglage « Liste des documents » (désactivé par défaut) et liste complète, limitée à 10 documents (F-4.2, DEC-0024).

**Liste de contrôle sur l'appareil (jalon 2)**
- [ ] « Ouvrir avec » Épure PDF depuis « Mes fichiers », Samsung Email ou Gmail, Chrome (téléchargement) : le document s'ouvre.
- [ ] Retour après ouverture par `VIEW` : retour à l'app d'origine.
- [ ] Fermer à la page 37, rouvrir depuis les récents : page 37.
- [ ] Redémarrer le téléphone, rouvrir un récent ouvert par le sélecteur : il s'ouvre.
- [ ] Récent issu d'une pièce jointe : comportement conforme à la décision F-1.3.
- [ ] Déplacer ou supprimer un fichier récent dans « Mes fichiers », puis le toucher : message, pas de plantage.
- [ ] « Retirer de la liste » : l'entrée disparaît ; le fichier existe toujours dans « Mes fichiers ».
- [ ] Forcer l'arrêt de l'app pendant la lecture puis la relancer : position conservée.
- [x] « À propos » : lien du dépôt ouvert dans le navigateur ; aucune permission ajoutée.

---

## Jalon 3 — Confort de lecture

**À trancher avant** : délai de masquage et répartition des commandes (F-6.2, F-6.3) ; indicateur permanent (F-7.1) ; portée du mode nuit (F-8.3) ; écran allumé par défaut (F-9.2).

### Session 3a — Commandes et navigation
- [x] Commandes au toucher simple, masquage automatique et au défilement (F-6.1 à F-6.3).
- [x] Ordre du bouton Retour complet (F-6.4).
- [x] Indicateur « 12 / 48 » (F-7.1).
- [x] « Aller à la page » : boîte de saisie, validation, message hors limites (F-7.2).
- [x] Tests unitaires : validation du numéro de page.

### Session 3b — Mode nuit, écran allumé, partage
- [x] Mode nuit par filtre de couleur, mémorisé selon la décision (F-8).
- [x] Écran allumé : option dans Paramètres et menu « Plus », effet limité au lecteur (F-9).
- [x] « Partager » dans le menu « Plus » et le menu d'appui long des récents (F-12) ; vérifier selon les sources (F-12.2).

**Liste de contrôle sur l'appareil (jalon 3)**
- [x] Toucher simple : commandes affichées ; elles disparaissent après le délai ; un double toucher ne les affiche pas.
- [x] Taille de police système au maximum : commandes lisibles, cibles utilisables, rien de coupé.
- [x] Aller à la page 150 du document 2 : saut immédiat ; saisir 999 : message.
- [x] Mode nuit : pages inversées ; réglage conservé après fermeture.
- [x] Écran allumé activé : l'écran ne s'éteint pas pendant la lecture au-delà du délai de mise en veille ; il s'éteint normalement sur l'accueil.
- [x] Partager vers une messagerie : le fichier arrive intact.
- [ ] TalkBack : commandes et pages annoncées.

---

## Jalon 4 — Marque-pages, recherche, table des matières

Les sessions 4b et 4c ne sont réalisées que si le moteur retenu au jalon 1 le permet ; sinon, la fonction est consignée comme écartée dans `DECISIONS.md` et retirée de `SPEC.md`.

**À trancher avant** : libellé des marque-pages (F-10.2) ; recherche insensible aux accents, couleur du signalement, compteur (F-11.2, F-11.4, F-11.5).

### Session 4a — Marque-pages
- [ ] Ajouter / retirer un marque-page sur la page courante (F-10.1).
- [ ] Écran « Marque-pages », aller à la page, retrait par appui long, liste vide (F-10.3).
- [ ] Données effacées avec « Retirer de la liste » (F-4.4).

### Session 4b — Recherche de texte (conditionnelle)
- [ ] Champ, lancement par Entrée, saut à la première occurrence, compteur, « Précédent » / « Suivant » (F-11.1, F-11.3).
- [ ] Signalement sur la page selon la décision (F-11.4).
- [ ] Recherche interruptible, interface non bloquée (F-11.5).
- [ ] Document scanné : message (F-11.6).

### Session 4c — Table des matières (conditionnelle)
- [ ] Liste hiérarchique, aller à la page (F-13.1).
- [ ] Document sans sommaire : message, action en `TextSecondary` (F-13.2).

**Liste de contrôle sur l'appareil (jalon 4)**
- [ ] Ajouter trois marque-pages, fermer, rouvrir : ils sont conservés et triés.
- [ ] Rechercher un mot présent 10 fois dans le document 1 : compteur juste, navigation correcte.
- [ ] Rechercher dans le document 2 (long) : interface réactive, Retour interrompt.
- [ ] Rechercher dans le document 3 (scanné) : message.
- [ ] Sommaire du document 2 : niveaux respectés, saut correct.

---

## Jalon 5 — Optimisation et robustesse

**À trancher avant** : taille du cache, taille maximale d'image, rendu par tuiles (T-6) ; cibles de temps (T-7) ; mot de passe jamais enregistré (F-14.1) ; poignée de défilement rapide (F-7.3).

### Session 5a — Gros fichiers et mémoire
- [ ] Mesurer mémoire et temps sur les documents 2, 5, 9 ; ajuster le cache.
- [ ] Très grandes pages : résolution plafonnée ou tuiles selon la décision (F-14.3).
- [ ] Repli en résolution réduite en cas de manque de mémoire (T-6).
- [ ] Poignée de défilement rapide si décidée (F-7.3).

### Session 5b — Documents protégés ou mal formés
- [ ] Mot de passe : boîte de saisie si le moteur le permet, sinon message (F-14.1).
- [ ] Page illisible isolée : « Page illisible », reste du document lisible (F-14.2).
- [ ] Revue de tous les messages d'erreur (ton neutre, `strings.xml`).

### Session 5c — Vérifications finales
- [ ] Écran partagé et fenêtre flottante One UI (T-13).
- [ ] Revue complète des `[À VÉRIFIER]` restants dans `SPEC.md`.
- [ ] Revue du README et de « À propos ».
- [ ] Contrôle final du manifeste fusionné.

**Liste de contrôle sur l'appareil (jalon 5)**
- [ ] Document 9 (> 50 Mo) : ouverture dans la cible de temps, défilement complet sans plantage.
- [ ] Document 5 (A0) : zoom maximal sans plantage.
- [ ] Document 6 : bon mot de passe → ouverture ; mauvais → message ; réouverture → mot de passe redemandé.
- [ ] Document 7 (corrompu) : message, pas de plantage.
- [ ] Écran partagé avec une autre app : lecture correcte.
- [ ] Aucune permission dans « Infos sur l'application ».

---

## Historique

- 0.1 (24/10/2026) : première version, à partir de la note de cadrage et de `SPEC.md` 0.1.
