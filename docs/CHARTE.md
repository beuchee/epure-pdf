# Charte graphique et fonctionnelle — Épure PDF

> Partie A : version 1.0 du 24 octobre 2026, reprise **sans modification** de la charte du launcher Épure (DEC-0010). Les références de la Partie A (`ActionRow.kt`, « Épure »…) désignent les fichiers et l'app du launcher, qui servent de modèle.
> Partie B : version 0.1 du 24 octobre 2026, propre à Épure PDF.
> Ce document ne remplace pas `SPEC.md`, qui reste le cahier des charges détaillé. En cas d'écart, `SPEC.md` et `DECISIONS.md` font foi ; l'écart est signalé et tranché.

---

## Partie A — Ce qui se reprend dans chaque projet

Ces règles sont **génériques** : elles valent pour tout nouveau projet Android de Alain, sauf décision explicite contraire consignée dans `DECISIONS.md`.

### A.1 Principes

1. **Sobriété visuelle** : fond uni, texte seul, aucune icône décorative, aucune animation superflue.
2. **Sobriété technique** : pas de service permanent sans nécessité ; données lues à la demande ; dépendances réduites au strict minimum.
3. **Confidentialité** : les données restent sur l'appareil. Pas de bibliothèque d'analytics, de suivi de plantage ni de publicité. Pas de permission `INTERNET` si le projet n'en a pas besoin strictement.
4. **Permissions justifiées** : chaque permission sensible correspond à une fonction précise, expliquée à l'utilisateur au moment où elle sert. Si elle est refusée, seule cette fonction est désactivée.
5. **Simplicité d'abord** : la solution simple avant la solution sophistiquée.
6. **Respect du système** : ne pas dupliquer ni contrarier les outils natifs de l'appareil (ici, les outils Samsung / One UI).

### A.2 Identité d'un projet

| Élément | Règle | Exemple (Épure) |
|---|---|---|
| Nom affiché | Avec accents si la langue le permet, dans `strings.xml` (`app_name`) | « Épure » |
| Nom technique | Sans accent ni espace (projet, modules, classes) | `Epure` |
| `applicationId` | Espace de nom inversé, stable dès le premier build | `com.alain.epure` |
| Dépôt Git | Nom court, en minuscules, avec tirets | `epure-launcher` |
| Licence | GPL v3 pour les projets publics | GPL v3 |
| Marques | Ne jamais utiliser le nom d'une marque tierce (nom, dépôt, paquet, fiche de store) ni suggérer une affiliation | — |

### A.3 Pile technique figée

| Domaine | Choix |
|---|---|
| Langage | Kotlin uniquement (pas de code natif, pas de Rust, pas de NDK sauf décision contraire) |
| Interface | Jetpack Compose, Material 3, thème personnalisé |
| `minSdk` | 31 (Android 12) |
| `targetSdk` / `compileSdk` | 36 (Android 16) |
| Architecture | Une seule activité ; écrans en Compose ; `ViewModel` + `StateFlow` |
| Injection | Pas de framework : une classe `AppContainer` instancie les dépendances à la main |
| Navigation | État simple géré à la main (3 à 4 écrans) ; pas de `navigation-compose` tant que ce n'est pas nécessaire |
| Persistance | DataStore (Preferences) ; Room seulement si les données deviennent réellement structurées |
| Logique métier | Dans un paquet `domain/`, **sans dépendance Android**, couverte par des tests unitaires |
| Paquets | `ui/` (écrans), `data/` (dépôts, persistance), `system/` (accès aux API Android), `domain/` (logique pure) |
| Sauvegarde Android | Désactivée (`allowBackup` à `false`) pour un outil qui ne stocke que des préférences locales |

**Règle de dépendance** : toute nouvelle bibliothèque est justifiée dans `DECISIONS.md` (contexte, alternative, raison) **avant** d'être ajoutée au projet.

### A.4 Contraintes Android à anticiper dès le départ

- **Edge-to-edge imposé** (`targetSdk` 36) : gérer les marges avec `WindowInsets`.
- **Retour prédictif** : utiliser `BackHandler` (Compose) ou `OnBackPressedCallback`, jamais `onBackPressed()`.
- **Visibilité des paquets** (Android 11+) : lister les apps impose une déclaration `<queries>` ; `QUERY_ALL_PACKAGES` est interdite.
- **Permissions interdites par défaut** dans le manifeste : `INTERNET`, `SYSTEM_ALERT_WINDOW`, `QUERY_ALL_PACKAGES`. Toute autre permission doit correspondre à une ligne d'un tableau des permissions dans `SPEC.md`.
- **Paramètres restreints** (Android 13+) : une app installée hors Play Store peut voir l'accessibilité ou l'écoute des notifications bloquées ; le noter dans le README dès qu'un service de ce type est prévu.
- **Services d'accessibilité** : `canRetrieveWindowContent="false"` sauf nécessité, événements limités au minimum, aucun enregistrement.

### A.5 Charte visuelle de base

#### Couleurs

Thème **sombre uniquement**, sans couleur dynamique, sans thème clair.

| Jeton | Valeur | Usage |
|---|---|---|
| `Black` | `#000000` | Fond de tous les écrans, fond des fenêtres |
| `TextPrimary` | `#FFFFFF` | Texte principal, éléments actifs, titres, curseur |
| `TextSecondary` | `#B0B0B0` | Texte secondaire, libellés, éléments inactifs, filets, liserés |

- Aucune couleur d'accent. L'information se transmet par la taille, la graisse et le niveau de gris.
- Éléments inactifs : gris clair (`TextSecondary`), **lisibles** (jamais invisibles).
- Les couleurs sont déclarées une fois, comme jetons dans `ui/theme/Color.kt`, puis reprises dans `darkColorScheme` (`ui/theme/Theme.kt`). Aucune couleur écrite en dur dans un écran.
- Fond : toujours `Black`, y compris dans `res/values/themes.xml` (`android:windowBackground`) pour éviter un flash blanc au démarrage.

#### Typographie

- Police système (`FontFamily.Default`). Aucune police tierce par défaut.
- Graisses utilisées : `Light` (très grand affichage uniquement), `Normal`. Pas de gras, sauf nécessité.
- **Toutes les tailles sont en `sp`** : elles suivent le réglage de taille de police du téléphone. Jamais en `dp` pour du texte.
- Échelle utilisée dans Épure (`ui/theme/Type.kt`) :

| Rôle | Taille | Graisse | Style Compose |
|---|---|---|---|
| Heure (accueil) | 120 sp | Light | `ClockStyle` |
| Date (accueil) | 20 sp | Normal | `DateStyle` |
| Nom d'app, titre d'écran, titre de fenêtre, champ de recherche | 28 sp | Normal | `AppNameStyle` |
| Libellé, option secondaire, texte d'aide, bouton texte | 20 sp | Normal | `LabelStyle` |
| Corps de texte (rarement utilisé) | 16 sp | Normal | `Typography.bodyLarge` |

- Interligne généreux pour les textes courants : 1,3 à 1,4 fois la taille (28 sp / 36 sp ; 20 sp / 28 sp). L'heure seule a un interligne égal à sa taille (120 sp).
- Un nom trop long **passe à la ligne**, il n'est pas tronqué.

#### Espacements et géométrie

| Élément | Valeur |
|---|---|
| Marge horizontale des écrans | 24 dp (accueil : 16 dp pour les raccourcis) |
| Marge verticale du haut d'écran | 24 dp |
| Espace entre un titre et son premier élément | 24 dp |
| Espace entre un bloc et son libellé | 16 dp à 32 dp |
| Hauteur minimale d'une cible tactile | 48 dp |
| Rayon des fenêtres (dialogue, menu) | 16 dp |
| Épaisseur des liserés et filets | 1 dp, couleur `TextSecondary` |

#### Iconographie

- **Par défaut, pas d'icône, pas de pictogramme** : loupe, roue dentée, flèches décoratives sont exclus. Les libellés sont du texte.
- Exception documentée : les icônes de raccourcis de l'accueil (Material Symbols, licence Apache 2.0, licence conservée dans chaque fichier `.xml`). Toute autre exception passe par `DECISIONS.md`.
- Les icônes conservées sont en `TextPrimary`, sans fond ni cadre, avec une zone tactile d'au moins 48 dp.

#### Mouvement

- Aucune animation décorative. Pas de transition d'apparition, pas de rebond.
- Un défilement natif, un changement d'écran immédiat, c'est tout.

### A.6 Composants types

Ces composants se réutilisent d'un projet à l'autre. Leur style doit rester identique.

| Composant | Description | Référence dans Épure |
|---|---|---|
| **Action en liste** | Texte seul, `AppNameStyle`, `TextPrimary`, hauteur minimale 48 dp, cible pleine largeur | `ActionRow.kt` |
| **Action secondaire** | Texte `LabelStyle` en `TextSecondary`, cible ≥ 48 dp | `TextAction` dans `ActionRow.kt` |
| **Champ de recherche** | Texte seul, sans cadre ni loupe ; une ligne soulignée implicite ; curseur `TextPrimary` ; clavier ouvert à l'arrivée sur l'écran | `AppListScreen.kt` |
| **Fenêtre de menu** | Centrée, fond `Black`, liseré 1 dp `TextSecondary`, rayon 16 dp, titre en `TextPrimary`, filet, actions en liste ; fond de l'écran assombri ; fermeture par Retour ou toucher à l'extérieur | `AppActionsMenu.kt` |
| **Boîte de saisie** | Même fenêtre que le menu, champ de saisie en `AppNameStyle`, deux actions texte | `RenameDialog.kt` |
| **Écran d'autorisation** | Plein écran, texte seul : titre, pourquoi, ce que l'app ne fait pas, consignes numérotées, bouton « Ouvrir le réglage », mention de révocation, bouton « Plus tard » | `PermissionScreen.kt` |
| **Index alphabétique** | Colonne verticale de lettres à droite de la liste, `TextSecondary`, largeur 40 dp, seules les lettres présentes, masquée pendant une recherche | `LetterIndex.kt` |
| **Liste de réglages** | Titre (`AppNameStyle`) puis actions en liste, défilement vertical | `SettingsScreen.kt` |
| **Séparateur** | `HorizontalDivider` de 1 dp en `TextSecondary` | menu et boîte de saisie |

#### États d'un élément

| État | Rendu |
|---|---|
| Actif | `TextPrimary` |
| Inactif ou indisponible | `TextSecondary`, reste visible, explication disponible au toucher |
| Pendant une action | Pas d'indicateur animé ; le résultat apparaît à la fin |
| Vide | Une phrase courte en `TextSecondary`, sans encadré ni illustration (ex. « Aucune app ») |

### A.7 Textes et langue

- **Tous les textes d'interface** sont dans `res/values/strings.xml`. Aucune chaîne en dur dans le code Kotlin.
- Langue par défaut : **français**. L'anglais peut être ajouté en seconde langue.
- **Tutoiement** dans l'interface.
- Ton **factuel et neutre** : pas de promesse chiffrée, pas de formule promotionnelle, pas de culpabilisation.
- Phrases courtes. Libellés à l'infinitif pour les actions (« Renommer », « Ouvrir le réglage »). Majuscule en début de phrase seulement.
- Les libellés des réglages système reprennent **exactement** le texte affiché par Android ou One UI, entre guillemets (ex. « Données d'utilisation » → « Octroi d'autorisation »).
- Les commentaires de code et les identifiants sont en **anglais**.

### A.8 Accessibilité (minimum)

- Tailles en `sp` ; l'interface doit rester utilisable avec la taille de police système agrandie.
- Cibles tactiles ≥ 48 dp.
- Contraste élevé : texte clair sur fond noir.
- Chaque élément interactif a un libellé lisible par TalkBack (`contentDescription` ou texte). Les lettres de l'index sont annoncées comme actions (« Aller à la lettre M »).
- Un écran qui échoue (permission retirée, donnée absente) affiche un message, sans plantage.

### A.9 Gestes et navigation (règles communes)

- **Bouton Retour** : ferme d'abord le premier niveau ouvert (fenêtre, clavier, liste) ; sur l'écran d'accueil d'un launcher, il ne fait rien.
- **Bouton Accueil** (launcher) : ramène toujours à l'état initial de l'écran d'accueil.
- **Geste de navigation** : un balayage clair, avec un seuil de distance (ici 100 dp), pour éviter les déclenchements involontaires lors d'un simple toucher. Un libellé texte est toujours disponible en plus du geste, pour ne pas dépendre du seul geste.
- **Appui court** : action principale (ouvrir, valider). **Appui long** : menu d'actions sur l'élément.
- Pas de fenêtre de confirmation pour une action réversible ; confirmation pour une action irréversible.

### A.10 Permissions et autorisations spéciales

Modèle commun, à réutiliser tel quel :

1. La demande **apparaît au moment où la fonction est utilisée**, jamais dans un assistant au premier lancement.
2. L'écran d'autorisation (A.6) explique : la fonction, pourquoi, ce qu'Épure ne fait pas, les consignes exactes, le bouton d'ouverture du réglage, la mention « Révocable à tout moment », le bouton « Plus tard ».
3. Au retour du réglage système, l'autorisation est **vérifiée automatiquement** ; l'écran se ferme si elle est accordée.
4. Un refus ne désactive que la fonction concernée ; le reste de l'app fonctionne.
5. Aucune illustration ni capture du réglage système (sobriété, et pas de visuels tiers dans le dépôt).

### A.11 Méthode de travail et documents

Chaque projet contient un dossier `docs/` avec :

| Fichier | Contenu |
|---|---|
| `SPEC.md` | Cahier des charges : exigences `F-x.y` (fonctions) et `T-n` (transversales), critères d'acceptation, tableau des permissions, points ouverts `[À DÉCIDER]` |
| `ROADMAP.md` | Jalons et tâches cochables ; une tâche n'est cochée qu'une fois **validée sur l'appareil** |
| `DECISIONS.md` | Journal des décisions `DEC-xxxx` (contexte, options, décision, justification, réversibilité) ; les entrées ne sont jamais effacées |
| `CHARTE.md` | Le présent document |
| `CLAUDE.md` (racine) | Consignes pour l'assistant de code : lire les documents avant toute tâche, ne travailler que sur le jalon en cours |

Règles de méthode :

- **Un point `[À DÉCIDER]` n'est jamais implémenté comme s'il était tranché.** Il est d'abord consigné dans `DECISIONS.md`, puis retiré du `SPEC.md`.
- **Un point `[À VÉRIFIER]` se teste sur l'appareil** ; le résultat est consigné.
- **Ne pas modifier `SPEC.md` pour l'aligner sur le code** : l'écart est signalé et tranché.
- Une session de travail par jalon ou par tâche.
- Ne travailler que sur le jalon en cours : pas d'anticipation des jalons suivants.

### A.12 Dépôt public et Git

- Jamais versionnés : clé de signature (`*.jks`, `*.keystore`) et ses mots de passe, `local.properties`, informations personnelles, captures ou visuels de produits tiers.
- `LICENSE` présent dès le premier commit ; `.gitignore` Android vérifié avant le premier envoi.
- Avant chaque commit : `git status` et contenu indexé relus.
- **Commit seulement après validation sur l'appareil.** Commit avec l'adresse e-mail « noreply » de GitHub.
- Messages de commit en **anglais**, courts, à l'impératif (ex. `Add text-only app list`).
- Pas de `git push`, pas de réécriture d'historique, pas de force sans demande explicite.
- Ressources tierces seulement sous licence compatible avec la redistribution (Google Fonts, Material Symbols, Apache 2.0 ou équivalent). Code repris d'un projet open source : licence du fichier vérifiée, avis de copyright conservés, origine mentionnée en commentaire et dans « À propos ».
- README : mention factuelle si le projet s'inspire d'une autre app (« inspiré de … »), sans logo ni suggestion d'affiliation ; section Confidentialité ; procédure d'activation des permissions spéciales.

### A.13 Contrôles avant de déclarer une tâche terminée

1. `./gradlew assembleDebug` sans erreur.
2. `./gradlew testDebugUnitTest` au vert.
3. `./gradlew lint` sans **nouvelle** erreur.
4. Vérification sur l'appareil de test (Samsung, Android 16 / One UI 8.0) selon une liste de contrôle écrite dans `ROADMAP.md`.
5. Cocher la tâche dans `ROADMAP.md` seulement après validation.

---

## Partie B — Épure PDF

Cette partie décrit ce qu'Épure PDF fait et comment la Partie A s'applique à un lecteur de documents. Les exigences détaillées sont dans `SPEC.md` (références entre parenthèses). Tout ce qui est marqué **[À DÉCIDER]** n'est pas tranché.

### B.1 Objet

Lecteur PDF Android minimaliste, entièrement hors ligne : lire sans distraction, sans réseau, sans collecte de données. Usage principal : articles scientifiques, recommandations, documents de travail. Usage personnel en priorité ; code public ; publication sur le Play Store éventuelle, non prioritaire.

### B.2 Identité (DEC-0001 à DEC-0003)

| Élément | Valeur |
|---|---|
| Nom affiché | « Épure PDF » |
| Nom technique | `EpurePdf` |
| `applicationId` | `com.alain.epure.pdf` |
| Dépôt Git | `epure-pdf` |
| Licence | GPL v3 |
| Relation avec Épure | App distincte ; cohérence visuelle seulement |
| Marques | Aucun nom, logo ou visuel d'Adobe ; « PDF » employé comme nom de format (ISO 32000) |

### B.3 Écrans

| Écran | Rôle | Accès | Jalon |
|---|---|---|---|
| Accueil | Titre, « Ouvrir un fichier », récents, accès aux paramètres | Lancement de l'app | 1, 2 |
| Lecteur | Pages en défilement vertical continu, commandes au toucher | Ouverture d'un document | 1 |
| Aller à la page | Boîte de saisie | Toucher l'indicateur de page | 3 |
| Marque-pages | Liste des pages marquées | Commandes | 4 |
| Recherche | Champ et navigation entre résultats | Commandes | 4 (conditionnel) |
| Table des matières | Sommaire hiérarchique du PDF | Commandes | 4 (conditionnel) |
| Paramètres | Options de lecture, « À propos » | Accueil | 2, 3 |
| Mot de passe | Boîte de saisie | PDF protégé | 5 (conditionnel) |

- Emplacement de l'accès aux paramètres sur l'accueil : **[À DÉCIDER]**. Proposition : action secondaire « Paramètres » (`LabelStyle`, `TextSecondary`) en bas de l'écran, comme dans Épure.
- Pas d'écran d'autorisation (A.6, A.10) : l'app ne demande aucune permission (B.6).

### B.4 Application de la charte visuelle au lecteur

#### Pages et fond

| Élément | Règle |
|---|---|
| Fond hors des pages | `Black` |
| Pages | Couleurs d'origine du PDF (DEC-0010) ; ce n'est pas une dérogation, les pages sont du contenu, pas de l'interface |
| Espace entre deux pages | Noir, 8 dp (proposition **[À DÉCIDER]**) |
| Marges latérales autour des pages | 0 dp (proposition **[À DÉCIDER]**), pour maximiser la surface de lecture |
| Page en cours de rendu | Rectangle de la bonne proportion, couleur de page unie, sans indicateur animé (A.6, « Pendant une action ») |
| Page illisible | « Page illisible » en `TextSecondary`, centré dans le rectangle de la page |
| Mode nuit | Inversion des couleurs des pages par filtre d'affichage ; images comprises (limite assumée) ; l'interface ne change pas |

#### Plein écran

- Lecture en mode immersif : barres d'état et de navigation masquées.
- Les barres système réapparaissent avec les commandes, ou par balayage depuis le bord.

#### Typographie propre à Épure PDF

L'échelle de A.5 s'applique. Les styles `ClockStyle` et `DateStyle` (heure, date) ne servent pas. Usages :

| Élément | Style |
|---|---|
| Titre de l'accueil « Épure PDF » | `AppNameStyle`, `TextPrimary` |
| « Ouvrir un fichier » | Action en liste (`AppNameStyle`) |
| Nom de fichier dans les récents | **[À DÉCIDER]** : `AppNameStyle` (conforme) ou `LabelStyle` en `TextPrimary` (dérogation, mieux adapté aux noms longs d'articles) |
| Seconde ligne d'un récent (« p. 12 / 48 ») | `LabelStyle`, `TextSecondary` (proposition) |
| Nom du fichier dans les commandes | `LabelStyle`, `TextPrimary` |
| Indicateur « 12 / 48 » | `LabelStyle`, `TextSecondary` |
| Actions des commandes | Action secondaire, en `TextPrimary` quand disponible, `TextSecondary` quand indisponible |
| Titres de la table des matières | `LabelStyle`, `TextPrimary` ; retrait de 16 dp par niveau |

#### Iconographie

Aucune icône. Toutes les actions du lecteur sont des libellés texte (« Sommaire », « Rechercher », « Marque-page », « Nuit », « Plus »). L'exception des raccourcis d'Épure (A.5) ne s'applique pas ici.

#### Dérogations possibles, non tranchées

| Sujet | Problème | Référence |
|---|---|---|
| Signalement des résultats de recherche | Il faut marquer un mot sur une page claire ; aucune couleur d'accent disponible | `SPEC.md` F-11.4, **[À DÉCIDER]** |
| Taille du nom dans les récents | Noms de fichiers longs | `SPEC.md` F-4.1, **[À DÉCIDER]** |

Toute dérogation retenue est consignée dans `DECISIONS.md` puis reportée ici.

### B.5 Composants propres à Épure PDF

À créer dans ce projet, en respectant les composants types de A.6 :

| Composant | Description | Exigences |
|---|---|---|
| **Liste de pages** | Défilement vertical continu, rendu à la demande, zoom par pincement et double toucher | F-2, F-3 |
| **Barre de commandes** | Fond `Black`, texte seul ; en haut le nom du fichier, en bas l'indicateur et les actions ; apparition et disparition immédiates | F-6 |
| **Indicateur de page** | « 12 / 48 », touchable : ouvre « Aller à la page » | F-7 |
| **Ligne de récent** | Nom du fichier (passage à la ligne, pas de troncature) et seconde ligne en `TextSecondary` ; appui court = ouvrir, appui long = menu | F-4 |
| **Liste hiérarchique** | Table des matières, retrait par niveau, texte seul | F-13 |

Composants de A.6 réutilisés tels quels : action en liste, action secondaire, champ de recherche, fenêtre de menu, boîte de saisie, liste de réglages, séparateur. L'index alphabétique et l'écran d'autorisation ne servent pas.

### B.6 Gestes et navigation

| Geste | Où | Effet |
|---|---|---|
| Appui court | Récent | Ouvrir à la dernière position |
| Appui long | Récent | Menu : « Retirer de la liste », « Partager » |
| Toucher simple | Page | Afficher ou masquer les commandes (après le délai de double toucher du système) |
| Double toucher | Page | Cycle de zoom ; proposition : largeur → demi-page (colonne touchée) → largeur, **[À DÉCIDER]** |
| Pincement | Page | Zoom continu, de la largeur au maximum (×5, proposition **[À DÉCIDER]**) |
| Défilement | Page | Défilement natif ; masque les commandes |
| Toucher | Indicateur de page | « Aller à la page » |
| Retour | Lecteur | Ferme la fenêtre ouverte, puis masque les commandes, puis quitte le lecteur (accueil, ou app d'origine si ouverture depuis une autre app) |
| Retour | Accueil | Quitte l'app (comportement Android standard ; Épure PDF n'est pas un launcher) |

- Masquage automatique des commandes : 4 s (proposition **[À DÉCIDER]**).
- Pas de geste sans libellé équivalent, sauf le zoom (A.9) : le pincement et le double toucher sont les gestes standards d'un lecteur.

### B.7 Textes propres

Libellés de référence (à reprendre dans `strings.xml`) :

| Contexte | Texte |
|---|---|
| Accueil | « Ouvrir un fichier », « Aucun fichier récent » |
| Menu d'un récent | « Retirer de la liste », « Partager » |
| Commandes | « Sommaire », « Rechercher », « Marque-page », « Retirer le marque-page », « Nuit », « Plus » |
| Aller à la page | « Aller à la page », « Annuler », « Aller », « Entre 1 et 48. » |
| Listes vides | « Aucun marque-page », « Aucune table des matières », « Aucun résultat » |
| Erreurs | « Fichier introuvable. », « Accès au fichier retiré. », « Ce fichier n'est pas un PDF lisible. », « Page illisible », « Mot de passe incorrect. », « Ce document ne contient pas de texte. » |
| Réglages | « Écran allumé pendant la lecture », « Mode nuit », « À propos » |

Les messages d'erreur exacts restent ajustables au jalon concerné ; le ton reste neutre et factuel (A.7).

### B.8 Fonctions

| Fonction | Détail | Jalon | État |
|---|---|---|---|
| Ouverture par le sélecteur | `ACTION_OPEN_DOCUMENT`, autorisation persistante | 1 | Prévu |
| Défilement vertical continu | Pages ajustées à la largeur, formats mixtes | 1 | Prévu |
| Zoom | Pincement, double toucher, nouveau rendu net | 1 | Prévu |
| Ouverture depuis une autre app | Intent `VIEW`, `application/pdf` | 2 | Prévu |
| Récents | Liste texte, retrait sans supprimer le fichier | 2 | Prévu |
| Reprise de lecture | Dernière page mémorisée | 2 | Prévu |
| « À propos » | Version, licence, dépôt, confidentialité | 2 | Prévu |
| Commandes au toucher | Masquage automatique | 3 | Prévu |
| Indicateur et saut de page | « 12 / 48 », « Aller à la page » | 3 | Prévu |
| Mode nuit | Inversion des couleurs des pages | 3 | Prévu |
| Écran allumé | Option, limitée au lecteur | 3 | Prévu |
| Partage | Partage système, sans réseau propre | 3 | Prévu |
| Marque-pages | Numéros de page, par document | 4 | Prévu |
| Recherche de texte | Selon le moteur retenu | 4 | Conditionnel |
| Table des matières | Selon le moteur retenu | 4 | Conditionnel |
| PDF protégés | Saisie du mot de passe selon le moteur, sinon message | 5 | Conditionnel |
| Gros fichiers | Mémoire bornée, résolution plafonnée | 5 | Prévu |
| EPUB, CBZ, CBR | — | — | **[À DÉCIDER]** (`SPEC.md` F-16) |

Moteur de rendu : **[À DÉCIDER]** au jalon 1 (`SPEC.md` §6).

### B.9 Fonctions écartées (DEC-0009)

Annotations et surlignage, remplissage de formulaires, signature, édition, synchronisation cloud, lecture à voix haute.

### B.10 Permissions

| Permission / accès | Fonction | Obtention |
|---|---|---|
| Sélecteur système | Ouvrir un fichier | Choix de l'utilisateur |
| Autorisation persistante sur une URI | Rouvrir un récent | Accordée lors du choix ; libérée au retrait de la liste |
| Filtre d'intent `VIEW` | Ouverture depuis une autre app | Droit temporaire fourni par l'app d'origine |
| `FLAG_KEEP_SCREEN_ON` | Écran allumé | Aucune permission |
| `INTERNET`, `ACCESS_NETWORK_STATE`, `MANAGE_EXTERNAL_STORAGE`, `READ_EXTERNAL_STORAGE`, `READ_MEDIA_*`, `POST_NOTIFICATIONS`, `WAKE_LOCK`, `QUERY_ALL_PACKAGES`, `SYSTEM_ALERT_WINDOW` | — | **Non déclarées** |

Objectif : **aucune permission déclarée** dans le manifeste fusionné.

### B.11 Garanties de confidentialité

- Aucune donnée ne quitte le téléphone.
- Pas de permission `INTERNET` : garantie vérifiable dans le manifeste (DEC-0005).
- L'app n'accède qu'aux fichiers choisis par l'utilisateur (DEC-0006).
- Le contenu des documents n'est pas copié ; seuls sont conservés l'URI, les métadonnées des récents, la position de lecture, les marque-pages et les réglages (DEC-0008).
- Aucune statistique de lecture, aucun historique au-delà de la liste des récents.
- Retirer un document de la liste efface toutes ses données ; le fichier n'est jamais modifié ni supprimé.
- Le lien « Dépôt GitHub » de « À propos » est ouvert par le navigateur.

### B.12 Jalons et état d'avancement (24 octobre 2026)

| Jalon | Contenu | État |
|---|---|---|
| 0 | Mise en place du projet, dépôt, licence, documents | En cours (documents rédigés) |
| 1 | Lecteur de base : moteur, sélecteur, défilement, zoom | À faire |
| 2 | Ouverture depuis d'autres apps, récents, reprise | À faire |
| 3 | Commandes, mode nuit, saut de page, écran allumé, partage | À faire |
| 4 | Marque-pages, recherche, table des matières | À faire |
| 5 | Gros fichiers, mémoire, PDF protégés ou mal formés | À faire |

---

## Partie C — Démarrer un nouveau projet avec cette charte

Liste à suivre dans l'ordre :

1. Créer le projet Android Studio : Kotlin, Compose, `minSdk` 31, `targetSdk`/`compileSdk` 36, `applicationId` au format `com.alain.<nom>`.
2. Renseigner l'identité (A.2) : `app_name`, nom technique, dépôt, licence.
3. Copier le thème : `Color.kt` (trois jetons), `Theme.kt` (schéma sombre), `Type.kt` (échelle en `sp`), `themes.xml` (fond noir).
4. Créer les composants types (A.6) avant les écrans : liste en texte, fenêtre de menu, écran d'autorisation.
5. Créer `docs/` avec `SPEC.md`, `ROADMAP.md`, `DECISIONS.md` (modèle d'entrée en tête du fichier) et `CLAUDE.md` à la racine.
6. Rédiger la **Partie B** du présent document pour le nouveau projet (écrans, fonctions, permissions, garanties).
7. Vérifier le manifeste : aucune permission hors tableau de `SPEC.md`.
8. Mettre en place `.gitignore`, `LICENSE`, puis le premier build, les tests et le lint.
9. Consigner toute dérogation à cette charte dans `DECISIONS.md` ; ne pas la modifier silencieusement.

---

## Historique

- Partie A 1.0 (24/10/2026) : reprise sans modification de la charte du launcher Épure (DEC-0010).
- Partie B 0.1 (24/10/2026) : première version pour Épure PDF, à partir de `SPEC.md` 0.1 et `DECISIONS.md` (DEC-0001 à DEC-0011).
