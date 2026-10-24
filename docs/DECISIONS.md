# Journal des décisions — Épure PDF

> Ce journal consigne chaque décision technique, fonctionnelle ou de méthode, avec son contexte et sa justification.
> Règles :
> - Les entrées ne sont **jamais effacées**. Une décision révisée reçoit une nouvelle entrée qui la remplace ; l'ancienne passe au statut « Remplacée par DEC-xxxx ».
> - Un point `[À DÉCIDER]` de `SPEC.md` est d'abord tranché ici, puis retiré du `SPEC.md` (avec renvoi à l'entrée).
> - Toute nouvelle dépendance est justifiée ici **avant** son ajout (licence, compatibilité GPL v3, permissions du manifeste fusionné).
> - Toute dérogation à `CHARTE.md` est consignée ici.
> - Numérotation continue : DEC-0001, DEC-0002…

---

## Modèle d'entrée

```
## DEC-xxxx — Titre court

- Date : JJ/MM/AAAA
- Statut : Adoptée | Remplacée par DEC-xxxx | Abandonnée
- Jalon : n
- Exigences concernées : F-x.y, T-n

**Contexte**
Pourquoi la question se pose.

**Options**
- (a) …
- (b) …

**Décision**
L'option retenue, formulée sans ambiguïté.

**Justification**
Pourquoi cette option : critères, mesures, contraintes.

**Conséquences**
Ce que cela implique (code, SPEC, charte, permissions, licence).

**Réversibilité**
Facile | Moyenne | Difficile — et ce qu'il faudrait refaire.
```

---

## DEC-0001 — Identité de l'application

- Date : 24/10/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : `SPEC.md` §1.2

**Contexte**
Nommer l'app et fixer les identifiants avant le premier build : l'`applicationId` ne peut plus changer après une éventuelle publication.

**Décision**
- Nom affiché : « Épure PDF ».
- Nom technique : `EpurePdf`.
- `applicationId` : `com.alain.epure.pdf`.
- Dépôt GitHub : `epure-pdf`, public.

**Justification**
Cohérence avec le launcher Épure (`com.alain.epure`) et avec `CHARTE.md` A.2. « PDF » désigne un format ouvert (ISO 32000) : usage descriptif sans risque. Aucun nom, logo ou visuel d'Adobe ; aucune suggestion d'affiliation.

**Conséquences**
Avant toute publication sur le Play Store : recherche d'homonymes, vérification INPI / EUIPO (classe 9), nom affiché distinctif.

**Réversibilité**
Nom affiché : facile. `applicationId` : difficile après publication.

---

## DEC-0002 — Application distincte du launcher Épure

- Date : 24/10/2026
- Statut : Adoptée
- Jalon : 0

**Contexte**
Épure PDF reprend l'esprit et la charte du launcher Épure.

**Options**
- (a) Module ou fonction du launcher.
- (b) Application séparée.

**Décision**
(b) : dépôt, identifiant et cycle de développement séparés. Aucune dépendance de code entre les deux apps. Cohérence visuelle seulement, par reprise de `CHARTE.md` Partie A.

**Justification**
Un lecteur PDF sert indépendamment du launcher. Séparer garde chaque app simple et permet de les faire évoluer à des rythmes différents.

**Conséquences**
Le thème (`Color.kt`, `Theme.kt`, `Type.kt`, `themes.xml`) et les composants types sont **copiés**, pas partagés par une bibliothèque commune.

**Réversibilité**
Facile.

---

## DEC-0003 — Licence GPL v3

- Date : 24/10/2026
- Statut : Adoptée
- Jalon : 0

**Décision**
Code publié sous GPL v3. Fichier `LICENSE` présent dès le premier commit.

**Justification**
Règle commune des projets publics (`CHARTE.md` A.2).

**Conséquences**
Toute bibliothèque ou code repris doit avoir une licence compatible avec la GPL v3 (Apache 2.0, MIT, BSD, LGPL, GPL v3…). Une bibliothèque sous AGPL (ex. MuPDF) ferait passer l'ensemble sous AGPL : décision spécifique requise. Origine et avis de copyright conservés pour tout code repris.

**Réversibilité**
Difficile une fois du code de contributeurs extérieurs intégré.

---

## DEC-0004 — Pile technique

- Date : 24/10/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : T-10, T-12

**Décision**
- Kotlin uniquement ; aucun code natif ajouté par nous (pas de NDK, pas de Rust).
- Jetpack Compose, Material 3, thème personnalisé sombre (`CHARTE.md` A.5).
- `minSdk` 31 (Android 12) ; `targetSdk` / `compileSdk` 36 (Android 16).
- Une seule activité ; `ViewModel` + `StateFlow` ; navigation par état simple ; pas de framework d'injection (`AppContainer`).
- Paquets `ui/`, `data/`, `system/`, `domain/` ; `domain/` sans dépendance Android, couvert par des tests unitaires.
- Appareil de référence : Samsung, Android 16 / One UI 8.0.

**Justification**
Pile éprouvée sur le launcher Épure (`CHARTE.md` A.3) ; méthode et conventions réutilisables.

**Conséquences**
Le choix d'un moteur de rendu tiers à code natif (option C, `SPEC.md` §6) exigerait une décision dérogeant à « pas de code natif ».

**Réversibilité**
Moyenne.

---

## DEC-0005 — Aucune permission `INTERNET`

- Date : 24/10/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : T-1, T-2

**Contexte**
L'app est hors ligne par principe. Une bibliothèque peut ajouter `INTERNET` au manifeste fusionné sans que le manifeste source la mentionne.

**Décision**
- `INTERNET` (et `ACCESS_NETWORK_STATE`) absentes du manifeste source **et** du manifeste fusionné.
- Contrôle à chaque ajout ou mise à jour de dépendance (onglet « Merged Manifest », `apkanalyzer manifest permissions` sur l'APK).
- Permission injectée : retrait par `tools:node="remove"`, ou bibliothèque écartée, avec entrée dans ce journal.

**Justification**
Garantie de confidentialité vérifiable par quiconque lit le manifeste. Cohérent avec Épure (`CHARTE.md` A.1, A.4).

**Conséquences**
Aucune fonction ne peut reposer sur le réseau. Le lien « Dépôt GitHub » de « À propos » est ouvert par le navigateur. Section Confidentialité du README.

**Réversibilité**
Facile techniquement, mais contraire au principe fondateur : toute révision exige une nouvelle entrée argumentée.

---

## DEC-0006 — Accès aux fichiers par le Storage Access Framework seulement

- Date : 24/10/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : F-1, T-3

**Options**
- (a) SAF (`ACTION_OPEN_DOCUMENT`) et ouverture depuis d'autres apps (intent `VIEW`).
- (b) Permissions de stockage (`READ_EXTERNAL_STORAGE`, `READ_MEDIA_*`) et parcours des dossiers.
- (c) `MANAGE_EXTERNAL_STORAGE` (« accès à tous les fichiers »).

**Décision**
(a). Autorisations persistantes sur les URI pour rouvrir les récents. Jamais (b) ni (c).

**Justification**
(a) ne demande aucune permission : l'utilisateur choisit chaque fichier. (c) est inutile ici et fortement restreint par le Play Store. (b) donnerait accès à plus que nécessaire.

**Conséquences**
L'app ne connaît que les fichiers choisis ; pas de bibliothèque de documents ni d'exploration de dossiers. Limites connues : droits temporaires reçus par `VIEW`, descripteurs non repositionnables (`SPEC.md` F-1.3, F-1.4, à trancher au jalon 2).

**Réversibilité**
Facile.

---

## DEC-0007 — Aucun service en arrière-plan, aucune notification

- Date : 24/10/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : T-4

**Décision**
Pas de `Service`, pas de `WorkManager`, pas de `POST_NOTIFICATIONS`. Tout traitement a lieu pendant que l'utilisateur utilise l'app.

**Justification**
Sobriété technique (`CHARTE.md` A.1) ; un lecteur n'en a pas besoin.

**Réversibilité**
Facile.

---

## DEC-0008 — L'app ne copie pas les documents

- Date : 24/10/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : T-2, §7

**Décision**
L'app conserve seulement l'URI, les métadonnées de la liste des récents (nom, taille, nombre de pages, date), la position de lecture, les marque-pages et les réglages, stockés localement. Le contenu des documents n'est pas copié.

**Conséquences**
Une exception éventuelle (copie temporaire pour les descripteurs non repositionnables, `SPEC.md` F-1.4) fera l'objet d'une entrée dédiée qui précisera ses limites.

**Réversibilité**
Facile.

---

## DEC-0009 — Fonctions hors périmètre

- Date : 24/10/2026
- Statut : Adoptée
- Exigences concernées : `SPEC.md` §1.3

**Décision**
Écartées à ce stade : annotations et surlignage, remplissage de formulaires, signature, édition, synchronisation cloud, lecture à voix haute.

**Justification**
Lecteur seul, sans écriture dans les fichiers et sans réseau.

**Réversibilité**
Facile (nouvelle entrée).

---

## DEC-0010 — Reprise de la charte d'Épure

- Date : 24/10/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : T-9

**Décision**
`CHARTE.md` Partie A (version 1.0 du 24/10/2026) s'applique sans modification. Une Partie B propre à Épure PDF est rédigée au jalon 0. Toute dérogation passe par ce journal.

**Adaptation déjà actée** (sans dérogation) : les pages PDF conservent leurs couleurs d'origine ; le thème sombre s'applique à l'interface autour des pages.

**Réversibilité**
Facile.

---

## DEC-0011 — Jeu de PDF de test hors du dépôt

- Date : 24/10/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : `SPEC.md` annexe A

**Décision**
Le jeu de test est conservé hors du dépôt. Aucun document personnel ou sous droits d'auteur n'est versionné. Les PDF éventuellement versionnés sont générés par nous ou sous licence libre, avec mention de la source.

**Justification**
Dépôt public ; droits d'auteur des articles scientifiques ; données personnelles.

**Réversibilité**
Facile.

---

## DEC-0012 — compileSdk 36 avec bibliothèques antérieures aux dernières versions

- Date : 24/10/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : DEC-0004 (pile technique)

**Contexte**
Le modèle Android Studio a généré les versions les plus récentes de `core-ktx` (1.19.1) et de `lifecycle-runtime-ktx` (2.11.0). Elles exigent un `compileSdk` 37 au minimum : la compilation échoue (`checkDebugAarMetadata`). DEC-0004 fixe `compileSdk` à 36.

**Options**
- (a) `compileSdk` 37, `targetSdk` 36 : modifie DEC-0004.
- (b) `compileSdk` 36, versions antérieures compatibles : `core-ktx` 1.17.0, `lifecycle-runtime-ktx` 2.10.0.

**Décision**
(b). `compileSdk` et `targetSdk` restent à 36 ; `core-ktx` passe à 1.17.0 et `lifecycle-runtime-ktx` à 2.10.0.

**Justification**
`compileSdk` ne change que les API disponibles à la compilation, pas le comportement à l'exécution : c'est `targetSdk` qui le fixe, et il reste à 36. Le choix de DEC-0004 est donc maintenu. Épure, sur le même appareil, compile déjà avec `compileSdk` 36. Les versions retenues ont été vérifiées par le build (assembleDebug, lint sans erreur).

**Conséquences**
- `gradle/libs.versions.toml` : `coreKtx` = 1.17.0, `lifecycleRuntimeKtx` = 2.10.0.
- Le lint signale encore des versions plus récentes ; elles exigent `compileSdk` 37. À réévaluer si le projet passe à 37.

**Réversibilité**
Facile : passer `compileSdk` à 37 et remonter les deux versions de bibliothèques.

---

## DEC-0013 — Permission automatique `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` tolérée

- Date : 24/10/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : T-1, DEC-0005

**Contexte**
Le contrôle `aapt2 dump permissions` sur le APK liste une permission, `com.alain.epure.pdf.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. Elle est ajoutée automatiquement par le plugin Android, sans être demandée dans le manifeste source. Épure en a une aussi.

**Options**
- (a) La tolérer, comme le fait le plugin Android pour toutes les apps.
- (b) La retirer avec `tools:node="remove"` : non testé, et son retrait pourrait affecter le fonctionnement interne d'Android.

**Décision**
(a). Exception documentée à la règle « aucune permission » : c'est une permission propre à l'app, définie par elle-même.

**Justification**
Elle protège les messages internes de l'app : seule Épure PDF peut les recevoir. Elle n'accorde aucun accès aux données, ni à d'autres apps, et n'apparaît pas dans « Autorisations » de « Infos sur l'application ». Le contrôle sur l'appareil reste donc valable.

**Conséquences**
- Le contrôle `aapt2` (ou `apkanalyzer`) listera cette permission, et c'est attendu. Toute autre permission reste interdite.
- À réévaluer si Android change le comportement de cette permission.

**Réversibilité**
Facile : retirer la permission par `tools:node="remove"` et refaire le contrôle sur l'appareil.

---

## DEC-0014 — Prototype comparatif : ajout d'androidx.pdf sur une branche jetable

- Date : 24/10/2026
- Statut : Adoptée (prototype seulement ; le choix final du moteur est en DEC-0015)
- Jalon : 1
- Exigences concernées : `SPEC.md` §6, F-11, F-13, F-14.1, T-1

**Contexte**
La session 1a compare deux moteurs de rendu : PdfRenderer (intégré à Android, option A) et androidx.pdf (bibliothèque Jetpack, option B). Pour mesurer B, il faut l'ajouter au projet.

**Caractéristiques vérifiées avant ajout** (fichiers Maven de Google)
- Artefact : `androidx.pdf:pdf-viewer:1.0.0-beta01`, version bêta.
- `minSdk` 28 pour tous ses modules : compatible avec le nôtre (31).
- Dépendances déclarées : AndroidX (core, material, recyclerview, exifinterface), coroutines Kotlin.
- Le module de reconnaissance de texte (`pdf-ocr-play-services`) dépend de ML Kit de Google. Il n'est pas nécessaire au prototype et n'est pas ajouté.
- Aucun module ne déclare de permission dans son manifeste.

**Options**
- (a) Ajouter `pdf-viewer` sur une branche de prototype, pour mesurer.
- (b) Écarter B sans la mesurer.

**Décision**
(a), sur la branche `spike/render-engine` seulement. Cette bibliothèque n'entre pas dans `main` avant DEC-0015.

**Justification**
Les mesures demandées par la feuille de route (poids, mémoire, temps, manifeste fusionné) ne peuvent pas être prises sans l'ajouter. Le prototype est jetable.

**Conséquences**
- `gradle/libs.versions.toml` et `app/build.gradle.kts` sont modifiés sur la branche de prototype seulement.
- Le contrôle du manifeste fusionné se fera sur l'APK du prototype. Une permission `INTERNET` injectée rendrait B inacceptable.

**Réversibilité**
Facile : supprimer la branche.

---

## DEC-0015 — Moteur de rendu retenu

- Date : 24/10/2026
- Statut : Adoptée
- Jalon : 1
- Exigences concernées : `SPEC.md` §6, F-2.5, F-3.3, F-11, F-13, F-14.1, T-5, T-6, T-12

**Contexte**
La session 1a a comparé deux moteurs sur l'appareil de référence (Samsung SM-A536B, Android 16), à partir du jeu de test de l'annexe A.

**Options**
- (a) PdfRenderer, intégré à Android.
- (b) androidx.pdf 1.0.0-beta01, visionneuse Jetpack.
- (c) Moteur tiers (pdfium, MuPDF…) : écarté, il faudrait du code natif (DEC-0004).

**Mesures**
- PdfRenderer : ouverture entre 90 et 200 ms ; première page rendue entre 30 et 300 ms ; 300 pages et 70 Mo ouverts sans erreur ; mémoire d'environ 114 à 147 Mo (PSS) ; APK de prototype de 30,2 Mo ; recherche de texte sur 300 pages en 0,6 s.
- androidx.pdf : fonctionne sur les mêmes documents ; APK de 38,4 Mo (+8,1 Mo) ; permissions inchangées ; mémoire non mesurée sur les mêmes documents.

**Décision**
(a) PdfRenderer, derrière l'interface de `system/`. La recherche de texte et l'ouverture avec mot de passe ne sont disponibles que sur Android 16 et plus (niveau API 36) ; sur les versions antérieures, ces fonctions sont désactivées avec un message. Le sommaire est écarté : aucune des deux API ne le fournit.

**Justification**
- PdfRenderer est intégré à Android : aucune dépendance (T-5), aucune permission, aucun impact sur la licence.
- Il répond aux mesures demandées : ouverture et rendu rapides, mémoire acceptable, recherche et mot de passe sur Android 16.
- androidx.pdf est une version bêta. Elle exige un `compileSdk` 37 (contraire à DEC-0012), tire d'autres bibliothèques (Material, fragment), et son interface a son propre style, en conflit avec la charte (A.5, A.6).
- Les dépendances ajoutées pour le prototype B (`pdf-viewer`, `pdf-viewer-fragment`, `fragment-ktx`, `material`) et le passage à `compileSdk` 37 ne sont pas intégrés dans `main`. Ils restent sur la branche locale `spike/render-engine`, dont le commit `3ce53f5` garde les mesures.

**Conséquences**
- `SPEC.md` §6 : le point « [À DÉCIDER] » est tranché ; le moteur retenu est indiqué.
- F-11 (recherche) : limitée à Android 16 et plus.
- F-14.1 (mot de passe) : limitée à Android 16 et plus ; sur les versions antérieures, message « Ce document est protégé par un mot de passe et ne peut pas être ouvert. ».
- F-13 (sommaire) : écartée.
- Limites à assumer : PdfRenderer n'ouvre qu'une page à la fois (T-6) ; pas d'OCR ; pas de sommaire.
- Le rendu des pages (défilement, zoom, cache) est à écrire dans l'application (jalons 1b et 1c).

**Réversibilité**
Moyenne. Un passage à androidx.pdf reste possible : il suffit d'écrire une seconde implémentation de l'interface de `system/`. Il faudra alors une nouvelle entrée pour `compileSdk` 37, les dépendances et le style de l'interface. Déclencheurs possibles : besoin de recherche ou de mot de passe sur Android 12 à 15, ou version stable d'androidx.pdf sans exigence de `compileSdk` 37.

---

## DEC-0016 — Rotation : suivre le réglage système

- Date : 25/10/2026
- Statut : Adoptée
- Jalon : 1
- Exigences concernées : F-2.6

**Contexte**
F-2.6 laissait ouverte la question d'un verrouillage d'orientation propre à l'app.

**Options**
- (a) Suivre le réglage de rotation du système ; la page en cours est conservée.
- (b) Verrouiller l'orientation dans le lecteur.

**Décision**
(a). Pas de verrouillage propre à l'app.

**Justification**
Principe « Respect du système » (CHARTE A.1) : le réglage rapide de One UI suffit. Le verrouillage ajouterait une option sans besoin constaté.

**Conséquences**
- Aucune orientation fixée dans le manifeste.
- La page en cours doit rester la même après une rotation (contrôle sur l'appareil, jalon 1b).

**Réversibilité**
Facile.

---

## DEC-0017 — Zoom maximal ×5

- Date : 25/10/2026
- Statut : Adoptée
- Jalon : 1
- Exigences concernées : F-3.1, F-3.3, T-6

**Contexte**
F-3.1 laissait ouvert le zoom maximal (proposition : ×5).

**Décision**
Zoom continu par pincement entre la largeur de page (×1) et ×5.

**Justification**
Valeur de départ de la proposition, adaptée aux colonnes d'articles et aux petits caractères. Au-delà, le rendu de la page dépasserait la taille d'image bornée par T-6.

**Conséquences**
- Au zoom maximal, la page est rendue à une largeur bornée à environ 16 Mo d'image (T-6, valeur provisoire) : le texte peut paraître moins net qu'à l'écran réel. À évaluer sur l'appareil, puis à revoir au jalon 5 (rendu par tuiles, si nécessaire).
- Le rendu net après le geste (F-3.3) se fait à la largeur bornée.

**Réversibilité**
Facile : changer la constante de zoom maximal.

---

## DEC-0018 — Cycle du double toucher

- Date : 25/10/2026
- Statut : Adoptée
- Jalon : 1
- Exigences concernées : F-3.2

**Contexte**
F-3.2 proposait un cycle largeur, demi-page, largeur, avec le cadrage sur la colonne touchée.

**Décision**
Largeur de page (×1) → demi-page (×2, cadrée sur la moitié gauche ou droite selon l'endroit touché) → largeur. Si l'utilisateur a zoomé par pincement, un double toucher ramène à la largeur. Le point touché reste sous le doigt après le zoom.

**Justification**
Usage visé : articles en deux colonnes, chaque colonne remplit alors l'écran. La proposition initiale est retenue sans changement.

**Conséquences**
- Aucune animation : le changement de cadrage est immédiat (CHARTE A.5).

**Réversibilité**
Facile.

---

## Décisions en attente

La liste à jour des points `[À DÉCIDER]` est tenue dans `SPEC.md` §9.1, avec le jalon auquel chacun doit être tranché. Le premier à trancher est le **moteur de rendu** (`SPEC.md` §6), à l'issue du prototype comparatif du jalon 1 (session 1a).
