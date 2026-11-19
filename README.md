# NEON LOCK v1.0.0 — Cadran coffre-fort animé

Fond d'écran animé Android, sans root et sans fonction de verrouillage propriétaire.

## Design — retour à la v0.5.0
- Même base graphique que le coffre-fort original v0.5.0 : fond bleu/noir dégradé, métal radial, anneaux de précision, doubles arcs orange, triangle orange fixe en haut et heure dans le moyeu central.
- Chiffres **0 1 2 3 4 5 6** sur la roue mobile, à la place de 0–9.
- La **roue métallique tourne continûment** : un tour par minute, synchronisée avec l'horloge du téléphone.
- **Décompte 60 à 01 secondes** en orange sous l'heure HH:mm au centre.
- Animation à 30 images/s lorsque le fond est visible, arrêt des rappels lorsque le fond n'est plus visible.

## Paramètres du fond animé conservés
- Afficher ou masquer « NEON LOCK » et la date.
- Personnaliser indépendamment les couleurs du titre, de la date, des arcs, des chiffres et de l'heure.
- Ajuster la position verticale du cadran (40 à 75 %, 69 % par défaut).
- Bouton de réinitialisation du thème.
- Prévisualisation dans l'application, et bouton d'application du fond animé Android.

## Sécurité
- Aucun code NEON, aucune superposition sur l'écran sécurisé, aucun service d'accessibilité.
- Le balayage et la désactivation éventuelle du verrouillage Android ne sont **pas** contrôlés par cette application.
- Pour afficher le fond sur l'écran de verrouillage, choisir l'option Android « écran d'accueil et écran verrouillé » lorsqu'elle est proposée par le constructeur.
- La compilation réussie ne garantit pas l'acceptation par Play Protect ni le rendu identique sur tous les firmwares HyperOS.

## Build
GitHub Actions assemble et publie `NEON-LOCK-v1.0.0.apk` dans Releases. APK de test signée avec une clé de débogage.
