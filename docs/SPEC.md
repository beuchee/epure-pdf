# Cahier des charges — Épure PDF

> Version 0.1 — 24 octobre 2026
> Statut : cadrage, avant le jalon 0.
> Ce document décrit **ce que l'application doit faire**. Les règles visuelles, d'interaction et de méthode sont celles de `CHARTE.md` (Partie A, reprise d'Épure) ; elles ne sont pas répétées ici, sauf adaptation propre au lecteur PDF.
> Conventions : `F-x.y` = exigence fonctionnelle ; `T-n` = exigence transversale ; **[À DÉCIDER]** = point non tranché, à consigner dans `DECISIONS.md` avant toute implémentation ; **[À VÉRIFIER]** = point à tester sur l'appareil, résultat à consigner.
> Une valeur suivie de « (proposition) » est une proposition de départ, pas une décision.

---

## 1. Objet et périmètre

### 1.1 Objet

Épure PDF est un lecteur de fichiers PDF pour Android, minimaliste et **entièrement hors ligne** : lire sans distraction, sans réseau, sans collecte de données. Usage principal : articles scientifiques, recommandations, documents de travail, lus sur téléphone.

### 1.2 Identité

| Élément | Valeur |
|---|---|
| Nom affiché | « Épure PDF » (`app_name`) |
| Nom technique | `EpurePdf` |
| `applicationId` | `com.alain.epure.pdf` |
| Dépôt Git | `epure-pdf` (public) |
| Licence | GPL v3 |
| Relation avec le launcher Épure | Application distincte : dépôt, identifiant et cycle séparés ; aucune dépendance ; cohérence visuelle seulement |

- « PDF » est un format ouvert (ISO 32000) ; son usage descriptif dans le nom ne pose pas de problème. Aucun nom, logo ou visuel d'Adobe ; aucune suggestion d'affiliation.
- Avant une éventuelle publication sur le Play Store : recherche d'homonymes, vérification INPI / EUIPO (classe 9), nom affiché distinctif. Publication non prioritaire.

### 1.3 Hors périmètre (décision prise)

Annotations et surlignage, remplissage de formulaires, signature, édition, synchronisation cloud, lecture à voix haute.

### 1.4 Appareil de référence

Samsung sous Android 16 / One UI 8.0. Émulateur accepté pour le développement, validation finale toujours sur l'appareil.

---

## 2. Glossaire

| Terme | Sens dans ce document |
|---|---|
| Document | Un fichier PDF ouvert par l'utilisateur, identifié par son URI |
| URI | Adresse `content://` du fichier, fournie par le système ; l'app ne connaît pas le chemin réel du fichier |
| SAF | Storage Access Framework : le sélecteur de fichiers du système (`ACTION_OPEN_DOCUMENT`) |
| Autorisation persistante | Droit de relire un fichier après redémarrage, conservé par le système (`takePersistableUriPermission`) |
| Moteur de rendu | Composant qui transforme une page PDF en image (PdfRenderer, androidx.pdf ou bibliothèque tierce) |
| Lecteur | L'écran qui affiche le document |
| Commandes | Barre de texte affichée au toucher dans le lecteur |
| Position de lecture | Page courante (et éventuellement décalage et zoom) mémorisés pour un document |

---

## 3. Écrans

| Écran | Rôle | Accès | Jalon |
|---|---|---|---|
| Accueil | Titre, action « Ouvrir un fichier », liste des récents, accès aux paramètres | Lancement de l'app | 1 (sans récents), 2 (avec récents) |
| Lecteur | Pages en défilement vertical continu, commandes au toucher | Ouverture d'un document | 1 |
| Aller à la page | Boîte de saisie (composant `CHARTE.md` A.6) | Commandes → indicateur de page | 3 |
| Marque-pages | Liste des marque-pages du document | Commandes | 4 |
| Recherche | Champ de recherche et navigation entre résultats | Commandes | 4 |
| Table des matières | Liste hiérarchique du sommaire du PDF | Commandes | 4 (écartée, DEC-0036) |
| Paramètres | Options de lecture, « À propos » | Accueil | 2 (« À propos »), 3 (options) |
| Mot de passe | Boîte de saisie | Ouverture d'un PDF protégé | 5 |

Navigation : état simple géré à la main, sans `navigation-compose` (`CHARTE.md` A.3).

---

## 4. Exigences fonctionnelles

### F-1 Ouverture d'un document

**F-1.1 Sélecteur système.** L'action « Ouvrir un fichier » de l'accueil lance `ACTION_OPEN_DOCUMENT` filtré sur `application/pdf`. Le document choisi s'ouvre dans le lecteur.
- Critères : un PDF choisi dans « Mes fichiers » (Samsung), dans Google Drive hors ligne ou dans « Téléchargements » s'ouvre ; annuler le sélecteur ramène à l'accueil sans message.
- L'app demande une autorisation persistante sur l'URI (`takePersistableUriPermission`, lecture seule) pour pouvoir le rouvrir depuis les récents.

**F-1.2 Ouverture depuis une autre app (intent `VIEW`).** L'app déclare un filtre `ACTION_VIEW`, catégorie `DEFAULT`, type `application/pdf`, schéma `content`. Elle apparaît dans « Ouvrir avec » d'un gestionnaire de fichiers, d'une messagerie, d'un navigateur.
- Critères : un PDF ouvert depuis « Mes fichiers », une pièce jointe d'e-mail et un téléchargement du navigateur s'affichent dans le lecteur ; le bouton Retour ramène à l'app d'origine.
- Le schéma `file://` n'est pas accepté (interdit pour le partage entre apps depuis Android 7).
- **[Décidé : DEC-0019]** Accepter aussi `ACTION_SEND` (`application/pdf`) : certaines apps proposent « Partager » plutôt que « Ouvrir avec ».
- **[Décidé : DEC-0020]** Comportement de la tâche Android (`launchMode`) quand un PDF est ouvert depuis une autre app : lecteur dans la tâche de l'app d'origine (comportement standard) ou dans la tâche d'Épure PDF. Proposition : comportement standard.

**F-1.3 Limite des autorisations reçues par `VIEW`.** Une URI reçue par `VIEW` donne en général un droit **temporaire** : il n'est persistant que si l'app d'origine l'a permis. Conséquence : un tel fichier peut ne plus être lisible plus tard depuis les récents.
- **[Constaté : DEC-0034]** Pour « Mes fichiers » : le droit n'est pas persistant. **[À VÉRIFIER]** pour Google Files, Samsung Email, Gmail, Chrome.
- **[Décidé : DEC-0021, texte modifié]** Traitement d'un récent devenu illisible : (a) l'afficher en `TextSecondary` avec, au toucher, « Fichier inaccessible. Ouvre-le à nouveau depuis l'application où il a été ouvert initialement. » et l'action « Ouvrir un fichier » ; (b) ne pas l'ajouter aux récents. Proposition : (a), la position de lecture étant retrouvée si le même fichier est rouvert (voir F-4.5).

**F-1.4 Fichier non lisible directement.** Le moteur de rendu (PdfRenderer notamment) exige un descripteur de fichier sur lequel il peut se déplacer librement. Certaines apps fournissent un flux non repositionnable (pièces jointes, fichiers en cours de téléchargement).
- **[Constaté : DEC-0034]** Gmail, Firefox Focus, Chrome (téléchargement), WhatsApp et « Mes fichiers » fournissent un descripteur repositionnable : le document est lu directement, sans copie. Samsung Browser et Chrome (lecteur intégré) n'envoient pas le fichier à Épure PDF.
- **[Décidé : DEC-0022]** En ce cas : (a) copie **temporaire** dans le cache privé de l'app, supprimée à la fermeture du document et au démarrage suivant ; (b) message d'erreur. La note de cadrage dit « l'app ne copie pas les fichiers » : l'option (a) est une dérogation à consigner dans `DECISIONS.md` si elle est retenue.

**F-1.5 Erreurs d'ouverture.** Fichier absent, droit retiré, fichier vide, fichier qui n'est pas un PDF, PDF mal formé : un message en texte, sans plantage, et retour possible à l'accueil. Libellés (proposition) : « Fichier introuvable. », « Accès au fichier retiré. », « Ce fichier n'est pas un PDF lisible. »

### F-2 Affichage et défilement

**F-2.1 Défilement vertical continu.** Les pages se suivent verticalement, séparées par un espace noir (8 dp, proposition). Fond hors des pages : `Black`. Les pages gardent leurs couleurs d'origine (blanc en général).

**F-2.2 Ajustement initial.** À l'ouverture, chaque page occupe la largeur de l'écran (marges latérales : 0 dp, proposition — maximiser la surface de lecture).

**F-2.3 Formats de page mixtes.** Un document mêlant portrait, paysage et tailles différentes s'affiche sans déformation ; chaque page est ajustée à la largeur.

**F-2.4 Plein écran.** Pendant la lecture, barres d'état et de navigation masquées (mode immersif). Elles réapparaissent avec les commandes, ou par balayage depuis le bord.
- **[À VÉRIFIER]** Comportement du mode immersif sous One UI 8 avec navigation par gestes et par boutons.

**F-2.5 Rendu à la demande.** Seules les pages visibles et leurs voisines immédiates sont rendues. Pendant le rendu, la page est un rectangle de la bonne proportion, couleur de page unie, sans indicateur animé (`CHARTE.md` A.6, « Pendant une action »).

**F-2.6 Rotation.** La rotation de l'écran conserve la page en cours. La rotation suit le réglage système. **[Décidé : DEC-0016]** Pas de verrouillage d'orientation propre à l'app : le réglage rapide One UI suffit (principe « Respect du système »).

### F-3 Zoom

**F-3.1 Pincement.** Zoom continu entre la largeur de page (×1) et ×5 (**[Décidé : DEC-0017]**), centré sur les doigts. Déplacement horizontal et vertical libre une fois zoomé.

**F-3.2 Double toucher — cycle de zoom.** Cycle demandé : largeur de page → demi-page → retour à la largeur.
- Second état (« demi-page ») : zoom ×2, cadré sur la moitié gauche ou droite de la page selon l'endroit touché. Usage visé : articles en deux colonnes, chaque colonne remplit alors l'écran.
- **[Décidé : DEC-0018]** Cycle : largeur → demi-page → largeur (variante a).
- **[Décidé : DEC-0018]** Un double toucher après un pincement ramène à la largeur.
- Cadrage vertical : le point touché reste sous le doigt après le zoom.

**F-3.3 Rendu net après zoom.** Après la fin du geste, les pages visibles sont rendues à nouveau à la résolution correspondant au zoom, dans la limite de taille d'image fixée en T-6. Pendant le geste, l'image existante est agrandie (floue un court instant, sans animation ajoutée).

**F-3.4 Distinction toucher simple / double toucher.** Le toucher simple (affichage des commandes) attend la fin du délai de double toucher du système avant d'agir.

### F-4 Fichiers récents

**F-4.1 Accueil.** L'accueil affiche, sous « Ouvrir un fichier », le dernier document ouvert, en texte seul (DEC-0024).
- Ligne 1 : nom du fichier (`OpenableColumns.DISPLAY_NAME`), sans tronquer (passage à la ligne, `CHARTE.md` A.5), en `LabelStyle` (20 sp) et `TextPrimary` : dérogation à la charte, consignée en DEC-0023.
- Ligne 2 (proposition) : « p. 12 / 48 » en `TextSecondary`.
- Aucun document ouvert : pas d'entrée affichée.

**F-4.2 Historique facultatif.** L'accueil ne garde qu'un récent. Un réglage « Liste des documents » (désactivé par défaut, DEC-0024) active une liste complète, du plus récent au plus ancien. Ce réglage est ajouté en session 2c ; la liste est limitée à 10 documents (DEC-0024) et ne s'applique qu'à ce mode. Le système limite le nombre d'autorisations persistantes par app (512 à partir d'Android 11, **[À VÉRIFIER]**).

**F-4.3 Ouverture.** Appui court sur un récent : ouverture à la position de lecture mémorisée (F-5).

**F-4.4 Menu d'appui long.** Fenêtre de menu (`CHARTE.md` A.6), titre = nom du fichier, actions : « Retirer de la liste », « Partager » (F-12).
- « Retirer de la liste » supprime l'entrée, sa position de lecture et ses marque-pages, et libère l'autorisation persistante. Le fichier lui-même n'est **jamais** supprimé.
- Confirmation : pas en session 2b (DEC-0025) ; à trancher en session 4a, quand les marque-pages existeront.

**F-4.5 Identification d'un document.** Un document est identifié par son URI.
- Limite connue : le même fichier ouvert par deux chemins (sélecteur, puis pièce jointe) donne deux URI, donc deux entrées.
- Identification secondaire par nom du fichier, pour retrouver la position de lecture (DEC-0023, modifiée par le porteur : la taille n'est pas utilisée). Pas de lecture du contenu.

### F-5 Reprise à la dernière position

**F-5.1** La page courante est mémorisée à la sortie du lecteur, à la mise en arrière-plan et à intervalle régulier pendant la lecture (pas d'écriture à chaque défilement ; proposition : à chaque changement de page, avec un délai d'1 s).

**F-5.2** À la réouverture, le lecteur s'ouvre sur cette page, sans écran intermédiaire.

**F-5.3** Mémorisation : page et décalage vertical seulement ; le zoom revient à la largeur (DEC-0026).

**F-5.4** Un document ouvert pour la première fois s'ouvre en page 1.

**F-5.5** Si le document a moins de pages que la page mémorisée (fichier remplacé), ouverture en page 1, sans message.

### F-6 Commandes du lecteur

**F-6.1 Affichage.** Un toucher simple sur une page affiche les commandes ; un nouveau toucher les masque. Apparition et disparition immédiates, sans animation (`CHARTE.md` A.5).

**F-6.2 Masquage automatique.** Les commandes disparaissent après 4 s sans interaction (proposition **[À DÉCIDER]**), ou dès que l'utilisateur fait défiler le document.

**F-6.3 Contenu.** Fond `Black`, texte seul, cibles ≥ 48 dp.
- En haut : nom du fichier (`LabelStyle`, `TextPrimary`, une ligne avec passage à la ligne autorisé).
- En bas : indicateur « 12 / 48 » (F-7) et actions texte selon le jalon : « Sommaire », « Rechercher », « Marque-page », « Nuit », « Plus ».
- « Plus » ouvre une fenêtre de menu avec les actions moins fréquentes : « Partager », « Marque-pages », « Écran allumé », etc.
- **[À DÉCIDER]** Répartition exacte entre barre et menu « Plus » : à fixer au jalon 3 sur l'appareil, selon la place disponible avec une police agrandie.

**F-6.4 Bouton Retour.** Dans l'ordre : ferme la fenêtre ouverte, puis masque les commandes, puis quitte le lecteur (retour à l'accueil, ou à l'app d'origine si ouverture par `VIEW`). Implémentation par `BackHandler` (`CHARTE.md` A.4).

### F-7 Indicateur de page et saut de page

**F-7.1 Indicateur.** « 12 / 48 » (page courante / total), `LabelStyle`, `TextSecondary`, visible avec les commandes. La page courante est celle qui occupe le plus de hauteur à l'écran.
- **[À DÉCIDER]** Indicateur permanent et discret (coin inférieur, pendant la lecture) en option. Proposition : non au jalon 3, à réévaluer.

**F-7.2 Aller à la page.** Toucher l'indicateur ouvre une boîte de saisie (`CHARTE.md` A.6) : titre « Aller à la page », champ numérique, actions « Annuler » et « Aller ». Entrée du clavier = « Aller ».
- Numéro hors limites : message sous le champ, « Entre 1 et 48. », sans fermer la boîte.
- Le saut est immédiat, sans défilement animé.

**F-7.3** **[À DÉCIDER]** Poignée de défilement rapide (glisser le long du bord droit) pour les documents longs. Proposition : à évaluer au jalon 5 avec les gros fichiers.

**F-7.4** Les numéros sont ceux de la position physique (1 à N). Les libellés de page propres au PDF (i, ii, iii…) ne sont pas utilisés.

### F-8 Mode nuit

**F-8.1** Action « Nuit » : inverse les couleurs des pages (fond noir, texte blanc). L'interface reste inchangée (déjà sombre).

**F-8.2** Limite assumée : les images et figures sont aussi inversées (aspect négatif). C'est la solution simple ; pas de traitement sélectif des images.

**F-8.3** **[À DÉCIDER]** Portée du réglage : (a) global (vaut pour tous les documents, mémorisé) ; (b) par document. Proposition : (a).

**F-8.4** L'inversion s'applique à l'affichage (filtre de couleur), sans nouveau rendu des pages.

### F-9 Maintien de l'écran allumé

**F-9.1** Option « Écran allumé », dans les paramètres et dans le menu « Plus ». Effet limité à l'écran du lecteur (`FLAG_KEEP_SCREEN_ON` sur la fenêtre). Aucune permission requise.

**F-9.2** Valeur par défaut : activée (décision du porteur, DEC-0030).

### F-10 Marque-pages

**F-10.1** Action « Marque-page » dans les commandes : ajoute la page courante ; si elle est déjà marquée, l'action devient « Retirer le marque-page ». Un marque-page = un numéro de page.

**F-10.2** **[Décidé : DEC-0035]** Le marque-page est le numéro de page seul, sans libellé saisi.

**F-10.3** Écran « Marque-pages » : liste « Page 12 », « Page 31 »… triée par page ; appui court = aller à la page ; appui long = « Retirer ». Liste vide : « Aucun marque-page ». Le retrait est immédiat, sans confirmation **[Décidé : DEC-0035]**.

**F-10.4** Les marque-pages sont stockés localement, par document (voir section 7). Ils ne sont pas écrits dans le fichier PDF.

### F-11 Recherche de texte

Fonction **conditionnelle** : livrée sur Android 16 et plus (DEC-0015) ; vérifiée sur l'appareil de référence en session 4b. Sur les versions antérieures, la fenêtre « Rechercher » affiche un message.

**F-11.1** Action « Rechercher » : champ de recherche (`CHARTE.md` A.6), clavier ouvert à l'arrivée. Entrée = lancer la recherche.

**F-11.2** Recherche insensible à la casse ; **[Décidé : DEC-0035]** insensible aux accents si le moteur le permet, sinon recherche exacte (vérifié sur l'appareil en session 4b).

**F-11.3** Résultat : saut à la première occurrence, occurrence signalée sur la page ; compteur « 3 / 17 » ; actions « Précédent » et « Suivant ». Aucun résultat : « Aucun résultat ».

**F-11.4** **[Décidé : DEC-0035, modifié le 25/10/2026]** Négatif de l'occurrence : les couleurs sous le rectangle sont inversées (noir sur blanc devient blanc sur noir, et l'inverse en mode nuit), par un mélange « différence » avec le jeton `TextPrimary`. Aucune couleur d'accent, donc aucune dérogation à la charte.

**F-11.5** Sur un document long, la recherche ne bloque pas l'interface ; elle peut être interrompue par Retour. Aucun indicateur animé ; le compteur est affiché à la fin de la recherche (**[Décidé : DEC-0035]**).

**F-11.6** PDF sans couche texte (scan) : « Ce document ne contient pas de texte. » Pas de reconnaissance de caractères (OCR).

### F-12 Partage

**F-12.1** Action « Partager » (menu « Plus » et menu d'appui long d'un récent) : `ACTION_SEND` avec l'URI du document et un droit de lecture temporaire, via le partage système. Aucun réseau propre à l'app.

**F-12.2** Si le droit sur l'URI ne permet pas de la transmettre, message « Ce fichier ne peut pas être partagé depuis Épure PDF. » **[À VÉRIFIER]** selon les sources.

### F-13 Table des matières — écartée (DEC-0036)

Fonction **écartée** : PdfRenderer ne fournit pas le sommaire (« outline ») du PDF, vérifié dans le SDK Android 16 (DEC-0036). Les exigences ci-dessous ne sont pas livrées.

**F-13.1** Action « Sommaire » : liste hiérarchique des titres, retrait de 16 dp par niveau, texte seul ; appui court = aller à la page du titre.

**F-13.2** PDF sans sommaire : « Aucune table des matières ». L'action reste visible en `TextSecondary` (`CHARTE.md` A.6, état inactif).

### F-14 PDF protégés et documents difficiles

**F-14.1 Mot de passe.** Si le moteur le permet (**[À VÉRIFIER]**) : boîte de saisie « Mot de passe », champ masqué, actions « Annuler » et « Ouvrir ». Mot de passe erroné : « Mot de passe incorrect. »
- Le mot de passe n'est **jamais** enregistré (proposition **[À DÉCIDER]**) : il est redemandé à chaque ouverture.
- Si le moteur ne le permet pas : « Ce document est protégé par un mot de passe et ne peut pas être ouvert. »

**F-14.2 PDF mal formé** : message clair (F-1.5) ; si seules certaines pages échouent, la page fautive affiche « Page illisible » en `TextSecondary`, le reste du document reste lisible.

**F-14.3 Très grandes pages** (affiches, plans) : rendu à la résolution maximale autorisée (T-6), sans plantage.

### F-15 Paramètres et « À propos »

**F-15.1 Paramètres** (liste de réglages, `CHARTE.md` A.6) : « Écran allumé », « Mode nuit », « À propos ». Rien d'autre sans nouvelle exigence.

**F-15.2 « À propos »** : nom, version, licence GPL v3, lien « Dépôt GitHub » (ouvert par le navigateur, sans permission réseau propre à l'app), section confidentialité (texte de T-2), licences des ressources tierces éventuelles, mention du moteur de rendu et de sa licence.

### F-16 Autres formats : EPUB, CBZ, CBR — [À DÉCIDER]

Listés dans les fonctions proposées, absents de la feuille de route. Points à trancher avant toute implémentation :

| Format | Nature | Conséquences |
|---|---|---|
| CBZ | Archive ZIP d'images | Le plus simple : lecture ZIP intégrée à Java, aucune dépendance ; réutilise le lecteur page par page |
| CBR | Archive RAR d'images | Aucune lecture RAR dans Android ; bibliothèques disponibles liées à la licence UnRAR, dont la compatibilité avec la GPL v3 est **douteuse** (**[À VÉRIFIER]**) ; RAR5 mal pris en charge |
| EPUB | Texte recomposable (HTML/CSS) | Autre moteur d'affichage (WebView ou bibliothèque), pagination, réglages de police : un second lecteur à part entière |

- Le nom « Épure PDF » ne couvre plus l'app si ces formats sont ajoutés.
- Proposition : hors périmètre de la version 1 ; CBZ seul envisageable ensuite ; EPUB et CBR à réévaluer, éventuellement dans une app séparée.

---

## 5. Exigences transversales

**T-1 Aucune permission `INTERNET`.**
- Absente du manifeste source et du **manifeste fusionné**.
- Contrôle à chaque ajout ou mise à jour de dépendance par aapt2.
- Une permission injectée par une bibliothèque est retirée par `tools:node="remove"`, ou la bibliothèque est écartée (décision consignée).
- `ACCESS_NETWORK_STATE` est traitée de la même manière.

**T-2 Confidentialité.** Aucune donnée ne quitte l'appareil. Aucune bibliothèque d'analytics, de suivi de plantage ou de publicité. Aucune statistique de lecture. L'app conserve seulement : URI, nom affiché, nombre de pages, position de lecture, marque-pages, réglages. La date d'ouverture et la taille du fichier ne sont ni conservées ni affichées. Le contenu des documents n'est jamais stocké (sauf copie temporaire si F-1.4 option (a) est retenue).

**T-3 Accès aux fichiers.** Uniquement par le SAF et les intents `VIEW` (et `SEND` si retenu). Jamais `MANAGE_EXTERNAL_STORAGE`, `READ_EXTERNAL_STORAGE`, `READ_MEDIA_*`. L'app ne parcourt pas le stockage et n'écrit dans aucun fichier de l'utilisateur.

**T-4 Aucun service en arrière-plan, aucune notification.** Pas de `Service`, pas de `WorkManager`, pas de permission `POST_NOTIFICATIONS`.

**T-5 Sobriété.** Dépendances au strict minimum ; chacune justifiée dans `DECISIONS.md` avant ajout, avec sa licence et sa compatibilité GPL v3 (`CHARTE.md` A.3, A.12).

**T-6 Mémoire.**
- Cache d'images de pages borné en octets (proposition : 1/8 de la mémoire disponible pour l'app, après mesure).
- Taille maximale d'une image de page bornée (proposition : 4 096 px de côté ou ~16 Mo) ; au-delà, rendu par morceaux (tuiles) au jalon 5.
- Un seul rendu à la fois si le moteur l'impose (PdfRenderer n'accepte qu'une page ouverte à la fois et n'est pas utilisable depuis plusieurs fils en parallèle).
- Aucun plantage pour manque de mémoire : la page concernée passe en résolution réduite.

**T-7 Performances (cibles, à mesurer sur l'appareil de référence).**
- Première page visible en moins d'1 s pour un article de 20 pages ; moins de 2 s pour un document de 500 pages.
- Défilement sans à-coups perceptibles sur un article courant.
- Ouverture du document en dehors du fil principal (aucun blocage de l'interface).

**T-8 Robustesse.** Aucun plantage pour : fichier absent, droit retiré, PDF mal formé, PDF protégé, page illisible, mémoire insuffisante, rotation, mise en arrière-plan, arrêt du processus par le système (au retour, réouverture sur la position mémorisée).

**T-9 Conformité à la charte.** `CHARTE.md` Partie A s'applique : thème sombre seul, trois jetons de couleur, typographie en `sp`, aucune icône par défaut, aucune animation, composants types, textes dans `strings.xml`, français et tutoiement, ton neutre, accessibilité minimale, gestes, méthode de travail, règles Git.
- Adaptations propres au lecteur : les pages PDF gardent leurs couleurs d'origine (hors mode nuit) ; le signalement de recherche (F-11.4) est un négatif et ne demande pas de dérogation.

**T-10 Contraintes Android** (`CHARTE.md` A.4) : edge-to-edge géré par `WindowInsets` ; retour prédictif ; `allowBackup="false"` ; une seule activité ; `ViewModel` + `StateFlow` ; pas de framework d'injection (`AppContainer`).

**T-11 Accessibilité.** Commandes, récents et fenêtres lisibles par TalkBack. Chaque page a une description (« Page 12 sur 48 »). Le contenu textuel des pages n'est pas lu par TalkBack (hors périmètre, faute d'extraction garantie).

**T-12 Architecture et tests.**
- Paquets : `ui/`, `data/` (récents, positions, marque-pages, réglages), `system/` (moteur de rendu, SAF, intents), `domain/` (logique pure).
- Logique couverte par des tests unitaires dans `domain/` : cycle de zoom du double toucher, calcul de la page courante, calcul de résolution de rendu, politique du cache, limite des récents, validation du numéro de page, identification secondaire d'un document.
- Le moteur de rendu est isolé derrière une interface dans `system/`, pour pouvoir en changer sans toucher à l'interface.

**T-13 Multi-fenêtre.** **[À VÉRIFIER]** Lecture en écran partagé et en fenêtre flottante One UI : pas de plantage, rendu adapté à la largeur disponible.

---

## 6. Moteur de rendu — [Décidé : DEC-0015]

Décision prise après un prototype comparatif (jalon 1, session 1a), consignée dans `DECISIONS.md`.

**Retenu : option A, PdfRenderer (DEC-0015).** Recherche de texte et mot de passe seulement sur Android 16 et plus ; sommaire non disponible (F-13 écartée).

| Option | Description | Points forts | Points à vérifier |
|---|---|---|---|
| A — PdfRenderer | Classe intégrée à Android | Zéro dépendance, hors ligne par nature, aucun impact sur la licence | Recherche de texte, mot de passe : API récentes, disponibles selon la version d'Android et les extensions SDK (**[À VÉRIFIER]** sur l'appareil) ; sommaire probablement absent ; tout le lecteur (défilement, zoom, cache) est à écrire |
| B — androidx.pdf | Bibliothèque Jetpack (visionneuse prête à l'emploi) | Moins de code ; recherche et sélection intégrées | Stabilité (alpha / bêta ?), compatibilité `minSdk` 31, intégration avec Compose, style personnalisable selon la charte, dépendances tirées, permission `INTERNET` injectée ou non |
| C — Moteur tiers (pdfium, MuPDF…) | Seulement si A et B sont insuffisants | Fidélité, sommaire, recherche | Poids de l'APK, code natif embarqué (contraire à « pas de code natif » sauf décision), licence (MuPDF : AGPL — l'ensemble passerait sous AGPL) |

Critères : poids de l'APK, fidélité du rendu sur le jeu de test (annexe A), recherche de texte, sommaire, mot de passe, mémoire, temps d'ouverture, licence, permissions du manifeste fusionné, respect de la charte visuelle.

---

## 7. Données stockées

| Donnée | Portée | Contenu |
|---|---|---|
| Récents | Par document | URI, nom affiché, nombre de pages |
| Position de lecture | Par document | Page, décalage vertical (et zoom si F-5.3 le décide) |
| Marque-pages | Par document | Liste de numéros de page |
| Réglages | Globale | Mode nuit, écran allumé |

- Stockage local, dans l'espace privé de l'app ; sauvegarde Android désactivée.
- Technique : DataStore Preferences, données par document sérialisées avec `org.json` (DEC-0027). Passage à Room seulement si le volume ou les requêtes l'exigent.
- Retirer un document des récents supprime toutes ses données (F-4.4).

---

## 8. Permissions et accès

| Permission / accès | Fonction | Obtention |
|---|---|---|
| Sélecteur système (`ACTION_OPEN_DOCUMENT`) | Ouvrir un fichier | Choix de l'utilisateur, à chaque ouverture |
| Autorisation persistante sur une URI | Rouvrir un récent | Accordée par le système lors du choix ; libérée au retrait de la liste |
| Filtre d'intent `VIEW` (`application/pdf`) | Ouverture depuis une autre app | Aucune permission ; droit temporaire fourni par l'app d'origine |
| `FLAG_KEEP_SCREEN_ON` | Écran allumé | Aucune permission |
| `INTERNET`, `ACCESS_NETWORK_STATE` | — | **Non déclarées**, absentes du manifeste fusionné |
| `MANAGE_EXTERNAL_STORAGE`, `READ_EXTERNAL_STORAGE`, `READ_MEDIA_*` | — | **Non déclarées** |
| `POST_NOTIFICATIONS`, `WAKE_LOCK`, `QUERY_ALL_PACKAGES`, `SYSTEM_ALERT_WINDOW` | — | **Non déclarées** |

Conséquence : **aucune permission déclarée** dans le manifeste (objectif). Pas d'écran d'autorisation (`CHARTE.md` A.10) nécessaire à ce stade.

---

## 9. Points ouverts

### 9.1 [À DÉCIDER]

| Réf. | Sujet | Jalon | Proposition |
|---|---|---|---|
| §6 | Moteur de rendu | 1 | Décidé : A, PdfRenderer (DEC-0015) |
| F-1.2 | Accepter `ACTION_SEND` | 2 | Oui si le coût est faible |
| F-1.2 | `launchMode` pour `VIEW` | 2 | Comportement standard |
| F-1.3 | Récent devenu illisible | 2 | Affiché en gris, message au toucher |
| F-1.4 | Copie temporaire si descripteur non repositionnable | 2 | Copie en cache, supprimée à la fermeture |
| F-2.6 | Verrouillage d'orientation | 1 | Non |
| F-3.1 | Zoom maximal | 1 | ×5 |
| F-3.2 | Cycle du double toucher | 1 | Largeur → demi-page → largeur |
| F-4.1 | Taille du nom dans les récents | 2 | Décidé : 20 sp, dérogation (DEC-0023) |
| F-4.2 | Historique facultatif | 2 | Décidé : dernier document seul ; liste facultative, désactivée par défaut (DEC-0024) ; limite de la liste à fixer en 2c |
| F-4.4, F-10.3 | Confirmation du retrait | 4 | Décidé : pas de confirmation, pour les récents (DEC-0025) et pour les marque-pages (DEC-0035) |
| F-4.5 | Identification secondaire (nom) | 2 | Décidé : oui, sur le nom seul (DEC-0023, modifiée par le porteur) |
| F-5.3 | Mémoriser le zoom | 2 | Décidé : non (DEC-0026) |
| F-6.2 | Délai de masquage des commandes | 3 | 4 s |
| F-6.3 | Répartition barre / menu « Plus » | 3 | À fixer sur l'appareil |
| F-7.1 | Indicateur permanent | 3 | Non |
| F-7.3 | Poignée de défilement rapide | 5 | À évaluer |
| F-8.3 | Mode nuit global ou par document | 3 | Global |
| F-9.2 | Écran allumé par défaut | 3 | Décidé : activé (DEC-0030) |
| F-10.2 | Libellé des marque-pages | 4 | Décidé : numéro seul (DEC-0035) |
| F-11.2 | Recherche insensible aux accents | 4 | Décidé : si le moteur le permet (DEC-0035) |
| F-11.4 | Couleur du signalement de recherche | 4 | Décidé : négatif de l'occurrence (DEC-0035, modifié le 25/10/2026) |
| F-11.5 | Compteur de résultats | 4 | Décidé : à la fin de la recherche (DEC-0035) |
| F-14.1 | Mot de passe jamais enregistré | 5 | Oui |
| F-16 | EPUB, CBZ, CBR | après 5 | Hors version 1 |
| T-6 | Taille du cache, taille max d'image, tuiles | 1 puis 5 | Après mesure |
| T-7 | Cibles de temps d'ouverture | 1 | Après mesure |
| §7 | DataStore ou Room | 2 | Décidé : DataStore + `org.json` (DEC-0027) |

### 9.2 [À VÉRIFIER] sur l'appareil

| Réf. | Point |
|---|---|
| §6 | PdfRenderer sous Android 16 / One UI 8 : mot de passe à vérifier (recherche vérifiée en session 4b ; sommaire écarté, DEC-0036) |
| §6 | androidx.pdf : statut, `minSdk`, dépendances, permissions injectées, compatibilité Compose |
| F-1.3 | Persistance des droits reçus par `VIEW` : non persistante depuis « Mes fichiers » (DEC-0034) ; autres sources à vérifier |
| F-1.4 | Sources fournissant un descripteur non repositionnable : constaté, aucune des cinq sources testées n'en fournit (DEC-0034) |
| F-2.4 | Mode immersif sous One UI 8 |
| F-4.2 | Limite système des autorisations persistantes |
| F-12.2 | Possibilité de repartager une URI selon la source |
| F-16 | Licence des bibliothèques RAR et compatibilité GPL v3 |
| T-13 | Écran partagé et fenêtre flottante |

---

## Annexe A — Jeu de PDF de test

Conservé **hors du dépôt** (aucun document personnel ou sous droits d'auteur versionné). Les PDF éventuellement inclus dans le dépôt sont générés par nous ou sous licence libre, avec mention de la source.

| N° | Type | Ce qu'il teste |
|---|---|---|
| 1 | Article scientifique en deux colonnes, ~10 pages | Usage principal, zoom demi-page |
| 2 | Recommandations longues, 200 à 500 pages, avec sommaire | Mémoire, saut de page, sommaire |
| 3 | Document scanné (images seules) | Rendu, recherche sans texte |
| 4 | Pages de formats mixtes (portrait, paysage, A3) | F-2.3 |
| 5 | Très grande page (affiche, A0) | F-14.3, T-6 |
| 6 | PDF protégé par mot de passe | F-14.1 |
| 7 | PDF tronqué ou corrompu (généré) | F-1.5, F-14.2 |
| 8 | Fichier non PDF renommé en `.pdf` | F-1.5 |
| 9 | PDF de plus de 50 Mo, riche en images | T-6, T-7 |
| 10 | PDF avec formulaire et annotations | Rendu sans plantage (pas d'édition) |
| 11 | PDF d'une seule page | Cas limite |
| 12 | PDF en pièce jointe d'e-mail et en téléchargement du navigateur | F-1.2 à F-1.4 |

---

## Historique

- 0.1 (24/10/2026) : première version, à partir de la note de cadrage du projet et de `CHARTE.md` 1.0.
