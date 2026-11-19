# NEON LOCK v1.3.0 — Seven Themes Edition

**Application Android native** de fond d'écran animé, conçue pour fonctionner sans PC, sans compte et hors ligne après installation.

## Les sept thèmes intégrés

| N° | Thème | Esthétique |
|---:|---|---|
| 1 | Vault Original | Photo d'origine, acier brossé et lumière orange |
| 2 | Cyber Hologram | HUD holographique bleu cyan, arcs et repères lumineux |
| 3 | Royal Gold | Horlogerie de luxe en or, repères romains et mini-engrenages |
| 4 | Red Reactor | Mécanisme noir avec segments d'énergie rouge |
| 5 | Steampunk | Engrenages cuivre/laiton, rivets et mouvement mécanique |
| 6 | Galaxy Pulse | Cercle cosmique violet, étoiles et vagues d'énergie |
| 7 | Arc Reactor | Réacteur énergétique original à bobines bleues et cœur triangulaire, sans logo ni reproduction officielle d'Iron Man |

## Fonctionnalités v1.3.0

- Galerie sur l'écran d'accueil avec **7 aperçus animés** ; toucher une carte pour sélectionner son thème.
- Le fond animé actif adopte immédiatement le thème sélectionné, sans nécessité de réappliquer le fond Android.
- Réglages indépendants : couleur personnalisable (y compris hexadécimal), intensité néon, taille, position verticale, vitesse de rotation, sens de rotation.
- Changement automatique des thèmes configurable (toutes les 1, 3, 6, 12 ou 24 heures), désactivé par défaut.
- Luminosité nocturne adaptative (22 h–7 h), par thème.
- Mode économie de batterie global (15 FPS au lieu de 30 FPS).
- **Heure HH:mm au centre uniquement**, sans secondes ; chiffres droits.
- Fond Vault Original basé sur les images originales et préserve ses réglages historiques.
- Vérification et téléchargement des nouvelles APK GitHub depuis l'application, contrôle SHA-256/signature, confirmation d'installation Android.
- Aucune superposition de verrouillage, aucun code NEON et aucune autorisation d'accessibilité.
- ApplicationId conservé `com.creatix.neonlock`, **versionCode 18** (montée de version depuis v1.2.4, versionCode 17).

## Installation et mises à jour

La release `NEON-LOCK-v1.3.0.apk` est construite via GitHub Actions avec la **même clé de signature permanente** que la version 1.2.4 :

```text
NEON_KEYSTORE_BASE64
NEON_SIGNING_PASSWORD
```

Ces secrets sont utilisés dans GitHub Actions, sans exposer la clé privée dans le dépôt. La CI vérifie la signature, la structure de l'APK et l'identifiant Android, puis publie l'APK dans Releases.

La mise à jour de 1.2.4 à 1.3.0 doit pouvoir être effectuée **par-dessus** l'installation actuelle, sans suppression des réglages, sous réserve de la confirmation Android.

## Notes techniques

- 100 % Java / Android Canvas (sans moteurs de jeux ni téléchargement de ressources externes).
- Les six thèmes ajoutés sont **des rendus procéduraux originaux** : cadrans et animations vectoriels, pas des images haute définition identiques à la maquette promotionnelle.
- Vault Original réutilise les textures photographiques fournies, pour conserver le rendu existant.
- L'animation ne tourne que quand le fond est visible.
- La cadence des tâches automatiques dépend des restrictions d'arrière-plan Android/HyperOS.

Voir [SIGNATURE-ET-MAJ.md](SIGNATURE-ET-MAJ.md) pour la documentation de signature.
