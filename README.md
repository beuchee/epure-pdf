# Épure PDF

Lecteur PDF Android minimaliste : lire sans distraction, en texte seul, sur le téléphone.

> Projet personnel en cours de développement. Application distincte du launcher Épure, dont elle reprend la charte visuelle.

## Confidentialité

- Toutes les données restent sur le téléphone. Aucune collecte, aucun envoi, aucune statistique.
- L'application ne demande aucune permission : elle n'a pas accès à Internet (permission `INTERNET` absente du manifeste fusionné).
- Les fichiers ne sont ouverts que par le sélecteur du système, par « Ouvrir avec » ou par « Partager ». L'application ne parcourt pas le stockage.
- Seuls sont conservés l'adresse du fichier, la position de lecture, les marque-pages et les réglages. Le contenu des documents n'est pas conservé. Pour certaines sources qui ne permettent pas de relire le fichier, une copie temporaire est faite dans le cache privé de l'app, pendant la lecture, puis supprimée à la fermeture du document.
- La sauvegarde Android est désactivée (`allowBackup="false"`).

## Langues

Français par défaut ; anglais prévu en seconde langue.

## Licence

Épure PDF est distribuée sous licence GPL v3. Voir le fichier [LICENSE](LICENSE).
