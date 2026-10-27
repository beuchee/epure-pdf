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
- Contrôle à chaque ajout ou mise à jour de dépendance avec aapt2.
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
L'app conserve seulement l'URI, les métadonnées de la liste des récents (nom, nombre de pages), la position de lecture, les marque-pages et les réglages, stockés localement. Le contenu des documents n'est pas copié.

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
- Le contrôle `aapt2` listera cette permission, et c'est attendu. Toute autre permission reste interdite.
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

## DEC-0019 — Accepter ACTION_SEND (« Partager »)

- Date : 25/10/2026
- Statut : Adoptée
- Jalon : 2
- Exigences concernées : F-1.2

**Contexte**
Certaines apps proposent « Partager » plutôt que « Ouvrir avec » pour un PDF.

**Décision**
Accepter `ACTION_SEND` avec le type `application/pdf`, en plus de `ACTION_VIEW`. Le fichier est lu depuis `EXTRA_STREAM`.

**Justification**
Coût faible (un filtre et une lecture d'extra), et le même traitement que `VIEW`.

**Conséquences**
- Filtre `SEND` ajouté au manifeste.
- Sur l'appareil, « Partager vers Épure PDF » doit ouvrir le document (contrôle de la session 2a).

**Réversibilité**
Facile.

---

## DEC-0020 — Lecteur ouvert par une autre app : tâche de l'app d'origine

- Date : 25/10/2026
- Statut : Adoptée
- Jalon : 2
- Exigences concernées : F-1.2, F-6.4

**Contexte**
Quand un PDF est ouvert depuis une autre app, le lecteur peut s'ouvrir dans la tâche de cette app ou dans une tâche propre à Épure PDF.

**Décision**
Comportement standard : le lecteur s'ouvre dans la tâche de l'app d'origine. Retour (F-6.4) : le lecteur se ferme et l'utilisateur revient à l'app d'origine.

**Justification**
Le retour attendu est celui d'une visionneuse : on revient d'où l'on vient. Pas de `launchMode` particulier.

**Conséquences**
- Pas de `launchMode` dans le manifeste.
- Quand le lecteur a été ouvert par une autre app, « Retour » termine l'activité au lieu d'afficher l'accueil.

**Réversibilité**
Facile.

---

## DEC-0021 — Récent devenu illisible

- Date : 25/10/2026
- Statut : Adoptée
- Jalon : 2 (appliquée en session 2b)
- Exigences concernées : F-1.3, F-4.1

**Contexte**
Un document ouvert par `VIEW` peut ne plus être lisible plus tard, si le droit était temporaire.

**Décision**
(a) Le récent reste affiché, en `TextSecondary`. Au toucher : « Fichier inaccessible. Ouvre-le à nouveau depuis l'application où il a été ouvert initialement. » avec l'action « Ouvrir un fichier ». La position de lecture est conservée si le même fichier est rouvert (F-4.5).

**Modification du 25/10/2026 (porteur)**
Le texte initial renvoyait vers le sélecteur, qui ne retrouve pas un fichier reçu par e-mail ou par téléchargement. Le message renvoie désormais vers l'application d'origine. Le reste de la décision est inchangé.

**Justification**
La liste des récents reste utile : un fichier déplacé ou un droit retiré n'efface pas la position de lecture.

**Conséquences**
- Traité en session 2b, avec la liste des récents.

**Réversibilité**
Facile.

---

## DEC-0022 — Copie temporaire pour un descripteur non repositionnable

- Date : 25/10/2026
- Statut : Adoptée
- Jalon : 2
- Exigences concernées : F-1.4, DEC-0008

**Contexte**
PdfRenderer exige un fichier sur lequel il peut se déplacer. Certaines sources (pièces jointes, téléchargements en cours) fournissent un flux non repositionnable.

**Options**
- (a) Copie temporaire dans le cache privé de l'app.
- (b) Message d'erreur.

**Décision**
(a), seulement quand le descripteur n'est pas repositionnable. La copie est dans le cache privé (`cacheDir`), supprimée à la fermeture du document et au démarrage suivant.

**Justification**
Une pièce jointe ou un téléchargement doit pouvoir s'ouvrir. Le message d'erreur ne serait pas utile dans ce cas.

**Conséquences**
- Exception à DEC-0008 (« l'app ne copie pas les documents ») : seul le document ouvert, et temporairement, est copié.
- Les limites sont à consigner au contrôle sur l'appareil (session 2a).

**Réversibilité**
Facile : retirer la copie et afficher le message.

---

## DEC-0023 — Récents : nom affiché et identification secondaire

- Date : 25/10/2026
- Statut : Adoptée
- Jalon : 2 (session 2b)
- Exigences concernées : F-4.1, F-4.5

**Contexte**
Une liste de récents affiche le nom du fichier, souvent long (titres d'articles). Un même fichier peut être rouvert avec une autre URI (par exemple depuis une autre app) ; il faut alors le reconnaître sans lire son contenu.

**Options**
- Nom : (a) `AppNameStyle` (28 sp), comme les listes d'Épure ; (b) `LabelStyle` (20 sp) en `TextPrimary`.
- Identification : (a) nom et taille du fichier ; (b) empreinte du contenu.

**Proposition**
(b) pour le nom : `LabelStyle` (20 sp) en `TextPrimary`, dérogation à la charte à consigner (la charte prévoit `AppNameStyle` pour les listes). Identification : (a) nom et taille du fichier, puis **modifiée par le porteur** (voir ci-dessous).

**Justification**
Les noms d'articles dépassent vite la largeur de l'écran à 28 sp. La taille et le nom suffisent à reconnaître un fichier sans le lire ; une empreinte imposerait de lire tout le document.

**Conséquences**
- Dérogation à `CHARTE.md` sur la taille du nom : à noter dans la section de dérogations.
- Les récents conservent le nom au moment de l'ouverture.

**Modification du 25/10/2026 (porteur)**
La taille n'est ni stockée ni affichée. L'identification d'un même fichier sous une autre URI repose donc sur le **nom seul**. Conséquence acceptée : deux fichiers différents portant le même nom partagent la même position ; la page est alors ramenée à la page 1 si elle dépasse le nombre de pages (F-5.5).

**Réversibilité**
Facile.

---

## DEC-0024 — Accueil : dernier document seul, historique facultatif

- Date : 25/10/2026
- Statut : Adoptée (réglage désactivé par défaut, validé par le porteur)
- Jalon : 2 (sessions 2b et 2c)
- Exigences concernées : F-4.1, F-4.2, F-4.3

**Contexte**
Une liste de 30 récents impose une limite, une purge et la libération d'autorisations en série. Pour la plupart des lecteurs, le besoin réel est de reprendre le document en cours.

**Proposition**
- Session 2b : l'accueil affiche le dernier document ouvert, avec sa position. Un seul récent est gardé ; ouvrir un autre document remplace l'entrée. Pas de liste, donc pas de limite de 30 ni de purge.
- Historique facultatif : un réglage « Liste des documents » (désactivé par défaut) activera une liste complète. Ce réglage sera ajouté en session 2c, avec l'écran Paramètres ; la liste est limitée à 10 documents (décision du porteur) et ne s'applique qu'à ce mode.

**Justification**
Le dernier document suffit à reprendre la lecture. Une liste désactivée par défaut évite de garder des accès à des fichiers que le lecteur n'a pas demandés à retenir. Le réglage reste disponible pour qui veut un historique.

**Conséquences**
- F-4.2 (10 récents) devient conditionnel au réglage : à réviser dans `SPEC.md` une fois adopté.
- Le menu d'appui long (DEC-0025) porte sur l'entrée affichée ; en mode historique, il porte sur l'entrée touchée.
- Une autorisation persistante n'est gardée que pour le dernier document, sauf en mode historique.

**Réversibilité**
Facile : passer de l'entrée unique à la liste ne change pas le stockage (DEC-0027).

---

## DEC-0025 — Retrait d'un récent : pas de confirmation en session 2b

- Date : 25/10/2026
- Statut : Adoptée
- Jalon : 2 (session 2b), à réviser au jalon 4
- Exigences concernées : F-4.4

**Contexte**
« Retirer de la liste » libère l'autorisation et efface les données du document (position, réglages propres au document). Le fichier lui-même n'est pas touché. La confirmation prévue par F-4.4 porte sur les marque-pages, qui n'existent qu'à partir du jalon 4.

**Proposition**
Pas de confirmation en session 2b. Le retrait est immédiat et ne supprime aucun fichier. La confirmation est à trancher en session 4a, quand les marque-pages existeront.

**Justification**
Aucune donnée n'est perdue hors de l'app (le fichier reste dans « Mes fichiers »). Une confirmation sans objet ajouterait une étape inutile.

**Conséquences**
- En session 4a : décider si le retrait d'un document avec marque-pages demande confirmation (proposition de F-4.4).

**Réversibilité**
Facile.

---

## DEC-0026 — Mémorisation de la position : page et décalage, pas le zoom

- Date : 25/10/2026
- Statut : Adoptée
- Jalon : 2 (session 2b)
- Exigences concernées : F-5.3, F-5.1, F-5.2

**Contexte**
La reprise (F-5) doit ramener au bon endroit. Le zoom peut aussi être mémorisé, mais il change la taille des pages et le décalage horizontal.

**Proposition**
Mémoriser la page et la fraction de page déjà lue (fonction `pageFraction`, déjà utilisée pour la rotation). Le zoom n'est pas mémorisé : à la reprise, il revient à la largeur.

**Justification**
Une reprise à la largeur est prévisible. Mémoriser le zoom ajoute un état à relire et à tester pour un gain faible.

**Conséquences**
- Position écrite de façon différée (F-5.2), comme prévu.
- Le décalage horizontal n'est pas mémorisé.

**Réversibilité**
Facile : ajouter le zoom à la sauvegarde.

---

## DEC-0027 — Stockage des récents et des positions : DataStore Preferences

- Date : 25/10/2026
- Statut : Adoptée
- Jalon : 2 (session 2b)
- Exigences concernées : §7 de `SPEC.md`, F-4, F-5

**Contexte**
L'app doit garder une liste de récents, une position par document et quelques réglages. Ces données sont petites et ne demandent pas de requêtes.

**Options**
- (a) DataStore Preferences, données par document sérialisées en JSON avec `org.json` (inclus dans Android).
- (b) Room : base SQLite, dépendance et génération de code (KSP) en plus.
- (c) SharedPreferences : API ancienne, écritures synchrones sur le fil principal selon l'usage.

**Proposition**
(a). Passer à Room seulement si le volume ou les requêtes l'exigent.

**Justification**
La charte privilégie DataStore tant que les données ne sont pas réellement structurées. DataStore écrit en arrière-plan sans bloquer le fil principal.

**Dépendance proposée : `androidx.datastore:datastore-preferences`**
- Bibliothèque : AndroidX DataStore, Google.
- Licence : Apache 2.0, compatible GPL v3.
- Effet sur le manifeste fusionné : aucune permission attendue. À vérifier avec `aapt2 dump permissions` après ajout.
- Alternative : (b) ou (c) ci-dessus.

**Conséquences**
- Ajout de la dépendance à `app/build.gradle.kts` seulement après adoption de cette entrée.
- Aucune permission ajoutée.

**Réversibilité**
Moyenne : changer de stockage après la session 2b demanderait une migration des données.

---

## DEC-0028 — Accès « Paramètres » sur l'accueil : en bas, action secondaire

- Date : 25/10/2026
- Statut : Adoptée
- Jalon : 2 (session 2c)
- Exigences concernées : `CHARTE.md` B.3 (point ouvert), F-15.1

**Contexte**
L'accueil doit donner accès aux Paramètres sans concurrencer « Ouvrir un fichier ». La charte laissait l'emplacement ouvert.

**Options**
- (a) En bas de l'accueil, action secondaire « Paramètres » (`LabelStyle`, `TextSecondary`).
- (b) En haut, à droite du titre.
- (c) Seulement dans un appui long sur le titre.

**Décision**
(a). Le porteur a validé cette option.

**Justification**
Discret, toujours visible, sans icône (charte A : texte seul), et ne gêne pas l'action principale.

**Conséquences**
- Composant `TextAction` dans `ActionRow.kt`, réutilisé tel quel par la suite (A.6, « Action secondaire »).

**Réversibilité**
Facile.

---

## DEC-0029 — Mode nuit : inversion par filtre, réglage global dans Paramètres

- Date : 25/10/2026
- Statut : Adoptée (demande du porteur)
- Jalon : 3 (session 3b), avancé à la demande du porteur
- Exigences concernées : F-8.1, F-8.3, F-8.4, F-15.1

**Contexte**
Le mode nuit doit être accessible depuis les Paramètres. Le jalon 3 n'était pas encore commencé ; la session 2a conserve des points ouverts.

**Décision**
- Réglage « Mode nuit » dans Paramètres, désactivé par défaut, global : il vaut pour tous les documents (F-8.3 (a)).
- Inversion des couleurs à l'affichage par filtre de couleur ; aucun nouveau rendu des pages (F-8.4). Images incluses (F-8.2).

**Justification**
Réglage unique et simple ; le filtre évite un nouveau rendu et ne touche pas au cache de pages.

**Conséquences**
- Le fond de rendu d'une page passe en noir en mode nuit.
- `SPEC.md` F-8.3 et §9.1 à mettre à jour après validation sur l'appareil.

**Réversibilité**
Facile.

---

## DEC-0030 — Écran allumé et partage d'un récent

- Date : 25/10/2026
- Statut : Adoptée (valeur par défaut « activé », décidée par le porteur)
- Jalon : 3 (session 3b)
- Exigences concernées : F-9.1, F-9.2, F-12.1, F-12.2

**Contexte**
Deux fonctions de la session 3b ne dépendent pas de la barre de commandes : l'écran allumé (réglage) et le partage depuis le menu d'appui long d'un récent. Le menu « Plus » du lecteur dépend de la session 3a et n'est pas traité ici.

**Décision**
- Réglage « Écran allumé pendant la lecture » dans Paramètres, activé par défaut (F-9.2, décision du porteur après validation sur l'appareil). Effet limité au lecteur : `View.keepScreenOn`, levé à la sortie du lecteur.
- « Partager » dans le menu d'appui long d'un récent : `ACTION_SEND` avec un droit de lecture temporaire. Si le fichier ne peut pas être lu ici ou si aucune app ne peut le recevoir, message « Ce fichier ne peut pas être partagé depuis Épure PDF. » (F-12.2, à vérifier sur l'appareil selon les sources).

**Justification**
Ces deux fonctions ne demandent aucune permission. Le partage passe par le sélecteur système, sans réseau propre à l'app.

**Conséquences**
- Le partage depuis le menu « Plus » du lecteur reste à faire avec la session 3a.
- F-12.2 : à vérifier sur l'appareil (pièces jointes, téléchargements, Mes fichiers).

**Réversibilité**
Facile.

---

## DEC-0031 — Commandes du lecteur : délai, barre et menu « Plus »

- Date : 25/10/2026
- Statut : Adoptée (choix du porteur)
- Jalon : 3 (session 3a)
- Exigences concernées : F-6.1, F-6.2, F-6.3, F-6.4, F-7.1, F-7.2

**Décision**
- Délai de masquage automatique : 4 secondes sans interaction (F-6.2), ou dès que la page défile. Le délai repart à chaque toucher.
- Barre du bas : l'indicateur « 12 / 48 » (ouvre « Aller à la page ») et « Plus ». Barre du haut : le nom du fichier.
- Menu « Plus » : « Partager », « Mode nuit », « Écran allumé ». Les actions des sessions suivantes (Sommaire, Rechercher, Marque-page) s'ajouteront à ce menu ou à la barre selon leur place.
- Les barres système réapparaissent avec les commandes et disparaissent avec elles (`CHARTE.md` B.4).

**Conséquences**
- Retour (F-6.4) : ferme la fenêtre ouverte, puis masque les commandes, puis quitte le lecteur.
- F-6.2 et F-6.3 à mettre à jour dans `SPEC.md` et au §9.1.

**Réversibilité**
Facile.

---

## DEC-0032 — Contrôle des permissions avec aapt2

- Date : 25/10/2026
- Statut : Adoptée (choix du porteur)
- Jalon : transversal (contrôle commun, `CLAUDE.md` §4 et §9)
- Exigences concernées : aucune permission non prévue (`SPEC.md` §8), DEC-0005, DEC-0013

**Contexte**
Le contrôle des permissions du fichier APK était prévu avec `apkanalyzer`, un outil d'Android Studio. Son emplacement n'est pas le même selon l'installation, et il n'était pas vérifié sur le poste. `aapt2` est installé avec les outils de build (`build-tools`) que le projet utilise déjà pour compiler.

**Options**
- (a) `apkanalyzer manifest permissions` (`cmdline-tools`) : outil dédié, à installer et à localiser.
- (b) `aapt2 dump permissions` (`build-tools`) : déjà présent, indispensable au build.

**Décision**
Le contrôle se fait avec `aapt2 dump permissions app/build/outputs/apk/debug/app-debug.apk`. `aapt2` se trouve dans `$ANDROID_HOME/build-tools/<version>/`.

**Justification**
Même information : les deux outils lisent le fichier APK final. `aapt2` est toujours présent avec le build, donc le contrôle ne dépend pas d'un outil supplémentaire. Le contrôle a été passé le 25/10/2026 avec `aapt2` : seule la permission `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` apparaît, ce qui est prévu par DEC-0013.

**Conséquences**
- `CLAUDE.md` (§4 et §9) et `ROADMAP.md` (contrôles communs, jalon 1) mis à jour.
- `SPEC.md` §8 et `DECISIONS.md` (entrées antérieures) mentionnent encore `apkanalyzer` : à aligner par le porteur, `SPEC.md` ne doit pas être modifié par l'assistant (`CLAUDE.md` §5).

**Réversibilité**
Facile.

---

## DEC-0033 — Pas de copie durable des documents reçus

- Date : 25/10/2026
- Statut : Adoptée (choix du porteur)
- Jalon : 2 (suite de DEC-0022)
- Exigences concernées : F-1.3, F-1.4, T-2, DEC-0008, DEC-0021, DEC-0022

**Contexte**
Un document reçu par `VIEW` (pièce jointe, téléchargement) peut devenir inaccessible après redémarrage. Une option « garder une copie » a été proposée pour le rouvrir depuis les récents.

**Options**
- (a) Pas de copie durable : la copie temporaire de DEC-0022 est effacée à la fermeture et au démarrage ; un récent devenu inaccessible affiche le message de DEC-0021.
- (b) Option « garder les copies » dans Paramètres, désactivée par défaut, avec taille maximale et suppression au retrait du récent.

**Décision**
(a). Aucune copie durable du contenu d'un document n'est ajoutée. L'option (b) n'est pas retenue.

**Justification**
Le principe du projet est que le contenu des documents n'est pas stocké (T-2, DEC-0008). Une copie durable l'enfreindrait, même derrière une option. Le coût est limité : le récent indique où rouvrir le fichier.

**Conséquences**
- Aucun changement de code.
- Le réglage « garder les copies » ne figure ni dans `SPEC.md` ni dans les réglages.

**Réversibilité**
Moyenne : une option (b) demanderait une nouvelle décision, une mise à jour de T-2 et de `SPEC.md`, et la gestion de la taille du cache.

---

## DEC-0034 — Résultats des tests sur l'appareil : droits reçus par `VIEW`

- Date : 25/10/2026
- Statut : Adoptée (constat sur l'appareil, SM-A536B, Android 16)
- Jalon : 2 (session 2a)
- Exigences concernées : F-1.3, F-1.4, DEC-0021, DEC-0022

**Constats**
- F-1.3 : le droit reçu par « Ouvrir avec » sur un PDF ouvert depuis « Mes fichiers » n'est pas persistant. Le récent n'est alors plus lisible et affiche « Fichier inaccessible » (DEC-0021).
- F-1.4, Gmail : le document s'ouvre et reste lisible. Aucun dossier de copies temporaires pendant la lecture : le flux était repositionnable et le document a été lu directement (constat du 25/10/2026, vérifié avec `adb shell run-as com.alain.epure.pdf ls cache/opened`).
- F-1.4, Firefox Focus : le document, ouvert puis téléchargé, s'ouvre et reste lisible. Aucun dossier de copies temporaires pendant la lecture : lu directement, comme pour Gmail (constat du 25/10/2026).
- Chrome, téléchargement ouvert par la notification de Chrome (Épure PDF choisi comme lecteur par défaut) : le document s'ouvre dans Épure PDF et reste lisible. Aucun dossier de copies temporaires pendant la lecture : lu directement (constat du 25/10/2026).
- Samsung Browser : ouvre les PDF dans son propre lecteur et ne propose pas Épure PDF. Le test F-1.4 ne peut donc pas être fait depuis ce navigateur.
- WhatsApp : le document, ouvert avec Épure PDF, est affiché. Aucun dossier de copies temporaires pendant la lecture : lu directement (constat du 25/10/2026).
- « Mes fichiers » : le document, ouvert avec Épure PDF, est affiché. Aucun dossier de copies temporaires pendant la lecture : lu directement (constat du 25/10/2026).

Bilan : toutes les sources testées (Gmail, Firefox Focus, Chrome, WhatsApp, « Mes fichiers ») fournissent un fichier repositionnable ; aucune copie temporaire n'a été nécessaire. La copie de DEC-0022 reste en place pour les autres sources, non testées.

**Conséquences**
- Le récent issu de « Mes fichiers » suit DEC-0021 ; aucun changement de code.
- Les cases de la session 2a sur les droits et les copies sont cochées, sur la base de ce constat.

**Réversibilité**
Sans objet : constat à mettre à jour après chaque nouveau test.

---

## DEC-0035 — Choix préalables au jalon 4 (marque-pages et recherche)

- Date : 25/10/2026
- Statut : Adoptée (choix du porteur)
- Jalon : 4 (à consigner avant 4a et 4b)
- Exigences concernées : F-10.2, F-10.3, F-11.2, F-11.4, F-11.5

**Décision**
- Marque-pages (F-10.2) : le marque-page est le numéro de page seul, sans libellé saisi.
- Recherche (F-11.2) : insensible aux accents si le moteur le permet ; sinon, recherche exacte. À vérifier en session 4b.
- Signalement (F-11.4) : rectangle gris translucide sur l'occurrence. Aucune couleur d'accent.
  *Modifié le 25/10/2026 par le porteur, après test sur l'appareil : le gris translucide était peu visible. Le signalement est désormais le négatif de l'occurrence (couleurs inversées : noir sur blanc, ou blanc sur noir en mode nuit), réalisé par un mélange « différence » avec le jeton `TextPrimary`.*
- Compteur (F-11.5) : affiché à la fin de la recherche, sans mise à jour pendant la recherche.
- Retrait d'un marque-page (F-10.3) : immédiat, sans confirmation, comme pour un récent (DEC-0025).

**Conséquences**
- `SPEC.md` §9.1 et les exigences F-10.2, F-10.3, F-11.2, F-11.4, F-11.5 à aligner par le porteur (`CLAUDE.md` §5).

**Réversibilité**
Facile.

---

## DEC-0036 — Sommaire écarté (session 4c)

- Date : 25/10/2026
- Statut : Adoptée (confirme DEC-0015 après vérification)
- Jalon : 4 (session 4c)
- Exigences concernées : F-13.1, F-13.2

**Constat**
L'API de `PdfRenderer` du SDK Android 36 (Android 16) ne donne ni sommaire ni signets : `PdfRenderer` et `PdfRenderer.Page` n'exposent que le rendu, les liens, le texte, la recherche et les champs de formulaire.

**Décision**
La session 4c (table des matières, F-13) est écartée. Les points F-13 sont à retirer de `SPEC.md` et de la feuille de route, par le porteur (`CLAUDE.md` §5).

**Réversibilité**
Moyenne : un sommaire demanderait une autre bibliothèque ou un autre moteur (voir DEC-0015).

---

## DEC-0037 — Autres formats : trois applications séparées

- Date : 25/10/2026
- Statut : Adoptée
- Jalon : hors feuille de route
- Exigences concernées : F-16 (EPUB, CBZ, CBR)

**Contexte**
Le `SPEC.md` (F-16) envisageait l'ajout d'EPUB, de CBZ et de CBR à Épure PDF. Le nom « Épure PDF » ne couvrirait plus l'app, et chaque format demande un moteur d'affichage distinct : pages PDF, images d'une archive, texte recomposable.

**Décision**
Chaque format fait l'objet d'une application séparée, sur le modèle d'Épure PDF, sans dépendance de code entre elles :
- Épure CBZ (archive ZIP d'images) ;
- Épure EPUB (texte recomposable) ;
- CBR (archive RAR) : traité dans une application à part, après levée de la question de licence UnRAR, ou converti en CBZ par l'utilisateur.

Aucune de ces applications n'est créée à ce jour.

**Conséquences**
- F-16 reste marqué `[À DÉCIDER]` dans `SPEC.md` ; le porteur l'alignera sur cette décision (`CLAUDE.md` §5).
- Épure PDF ne reçoit pas de nouveau format.

**Réversibilité**
Facile tant qu'aucune application n'est créée.

---

## DEC-0038 — Mot de passe à l'ouverture et repli mémoire

- Date : 25/10/2026
- Statut : Adoptée. Mot de passe validé sur l'appareil le 25/10/2026 (fichier de test chiffré, mot de passe 1234). Repli mémoire : pas encore déclenché sur l'appareil.
- Jalon : 5 (sessions 5a et 5b)
- Exigences concernées : F-14.1, T-6, T-8

**Constat**
PdfRenderer accepte un mot de passe par `LoadParams` à partir d'Android 16 (API 36). Un fichier chiffré ouvert sans mot de passe lève une `SecurityException`, comme prévu par DEC-0015.

**Décision**
- Sur Android 16 et plus : si un document est protégé, une boîte de saisie masquée « Mot de passe » s'affiche ; « Ouvrir » relance l'ouverture avec le mot de passe saisi. Mot de passe erroné : « Mot de passe incorrect. » dans la même boîte.
- Le mot de passe n'est jamais enregistré : il n'existe ni dans les réglages, ni dans les récents, ni dans l'état de l'écran. Il est redemandé à chaque ouverture.
- Sur les versions antérieures : le message « Ce document est protégé par un mot de passe et ne peut pas être ouvert. » est conservé.
- Manque de mémoire pendant le rendu d'une page : la page est redessinée à la moitié de la largeur demandée ; si cela échoue encore, elle affiche « Page illisible ». Aucun plantage.

**Conséquences**
- Nouvel état d'écran `UiState.Password` et nouvelle erreur `OpenError.WrongPassword`.
- Aucune dépendance ajoutée ; aucune permission ajoutée (contrôle `aapt2` passé le 25/10/2026).
- Ce qui reste à décider (taille de cache, tuiles, poignée de défilement) n'est pas tranché ici : voir `SPEC.md` §9.1.

**Réversibilité**
Facile.

---

## DEC-0039 — Poignée de défilement rapide et très grandes pages

- Date : 25/10/2026
- Statut : Adoptée (décisions du porteur, prises le 25/10/2026)
- Jalon : 5 (session 5a)
- Exigences concernées : F-7.3, F-14.3, T-6

**Décision**
- Poignée de défilement rapide (F-7.3) : non en version 1. Le défilement continu et « Aller à la page » suffisent.
- Très grandes pages (F-14.3, T-6) : résolution réduite, sans rendu par tuiles. Au zoom maximal, le texte d'une très grande page peut paraître moins net ; à revoir seulement si la mesure sur l'appareil le justifie.

**Conséquences**
- Aucun code à ajouter pour F-7.3.
- `SPEC.md` §9.1 (F-7.3, T-6) à aligner par le porteur (`CLAUDE.md` §5). Les points « taille du cache » et « cibles de temps » restent `[À DÉCIDER]` jusqu'à mesure sur l'appareil.

**Réversibilité**
Moyenne pour les tuiles (nouveau moteur de rendu par morceaux) ; facile pour la poignée.

---

## DEC-0040 — Rendu par tuiles au zoom maximal

- Date : 25/10/2026
- Statut : Adoptée (décision du porteur, prise le 25/10/2026, après mesure sur l'appareil)
- Jalon : 5 (session 5a)
- Exigences concernées : F-3.3, F-14.3, T-6

**Constat**
Sur le document synthétique d'affiche A0 (`doc5-a0.pdf`, 2384 × 3370 pt), au zoom ×5, le texte de 9 points était à peine lisible. La limite de `MAX_BITMAP_PIXELS` (4 millions de pixels) ne permet de rendre qu'environ 1 700 px de large pour toute la page, soit moins d'un pixel par point. DEC-0039 prévoyait de revoir ce choix si la mesure le justifiait : c'est le cas.

**Décision**
Le rendu par tuiles est retenu. Au-delà de la limite, seule la partie visible de la page est rendue à la résolution du zoom, par morceaux de 512 px de côté. Le rendu réduit reste affiché pendant le rendu des tuiles (F-3.3).

Conséquences de conception :
- La limite de pixels continue de borner chaque image rendue entièrement ; les tuiles sont bornées par leur taille fixe.
- La mémoire reste bornée par le cache d'octets (T-6) ; les tuiles hors écran ne sont pas rendues.
- La logique de découpage est pure et testée dans `domain/`.

**Conséquences**
- DEC-0039 reste valable pour la poignée de défilement, mais son point sur les très grandes pages est remplacé par la présente décision.
- `SPEC.md` T-6 et F-14.3 à aligner par le porteur (`CLAUDE.md` §5).

**Réversibilité**
Moyenne : le rendu par tuiles ajoute une seconde voie de rendu dans le lecteur.

---

## DEC-0041 — Cache de pages et cible de mémoire (mesures de la session 5a)

- Date : 25/10/2026
- Statut : Adoptée (décision du porteur, prise le 25/10/2026, après mesure sur l'appareil SM-A536B)
- Jalon : 5 (session 5a)
- Exigences concernées : T-6, T-7

**Mesures** (`TOTAL PSS` de `dumpsys meminfo`, en Mo, appareil SM-A536B, Android 16)
- App à vide (accueil) : environ 138.
- Document 2 après défilement : environ 205.
- Document 5 après défilement : environ 153.
- Document 9 après défilement : environ 320.
- Ouverture sur les documents de test : moins de 0,5 s (porteur, sur ses propres documents).

La limite de tas de l'appareil est de 256 Mo, donc le cache de pages vaut environ 32 Mo. La mémoire mesurée provient surtout du décodage des pages par PdfRenderer, pas du cache.

**Décisions**
- Cache de pages : 1/8 de la mémoire du tas, soit environ 32 Mo. Pas de changement.
- Cible de mémoire : rester sous 350 Mo de `TOTAL PSS` sur les documents testés, défilement compris. Le document 9 (320 Mo) respecte cette cible.
- Les cibles de temps de T-7 (1 s pour 20 pages, 2 s pour 500 pages) sont confirmées : les mesures sont nettement en dessous.

**Conséquences**
- `SPEC.md` T-6 et T-7 à aligner par le porteur (`CLAUDE.md` §5).

**Réversibilité**
Facile.

---

## DEC-0042 — Page illisible isolée écartée (F-14.2)

- Date : 27/10/2026
- Statut : Adoptée (décision du porteur, prise le 27/10/2026)
- Jalon : 5 (session 5b)
- Exigences concernées : F-14.2

**Constat**
Un document de test (`partial-page.pdf`, 3 pages, page 2 à `MediaBox [0 0 0 0]` et flux de contenu cassé) s'ouvre. Sur le SM-A536B, Android 16 :
- `PdfRenderer` annonce 612 × 792 pour la page cassée (format Lettre par défaut) ;
- le rendu de cette page réussit sans erreur et donne une page blanche.
« Page illisible » n'apparaît que lorsque le rendu échoue, donc jamais pour ce cas. Le moteur ne signale pas l'erreur, et l'application n'a aucun moyen fiable de la détecter sans analyser le contenu des pages.

**Décision**
La fonction « Page illisible isolée » (F-14.2) est écartée. Une page cassée s'affiche blanche, sans message. Le message « Ce fichier n'est pas un PDF lisible » (F-1.5) reste en place pour un fichier qui ne s'ouvre pas du tout.

**Réversibilité**
Facile : une détection des pages blanches ou une autre bibliothèque de rendu permettraient de revenir sur ce point (voir DEC-0015).

---

## Décisions en attente

La liste à jour des points `[À DÉCIDER]` est tenue dans `SPEC.md` §9.1, avec le jalon auquel chacun doit être tranché. Le premier à trancher est le **moteur de rendu** (`SPEC.md` §6), à l'issue du prototype comparatif du jalon 1 (session 1a).
