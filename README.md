# NEON CLOCK — v0.9.0

Application Android native de **fond d'écran animé** issue de NEON LOCK.

## Fonctionnement
- Le cadran **0 à 6** représente six périodes de **10 secondes**, soit une minute complète.
- Un repère orange se déplace régulièrement sur l'arc durant 60 secondes.
- L'heure **HH:mm** reste au centre du cadran.
- Le texte inférieur indique les secondes restantes avant la minute suivante.
- Options : couleurs du titre, arc, graduations, heure ; affichage du titre/date ; position verticale.
- Le fond est animé tant qu'il est visible, et stoppe les mises à jour en arrière-plan.

## Sécurité et écran de verrouillage
**NEON CLOCK n'est plus un écran de déverrouillage.** Les anciens champs PIN visuel, contrôles d'accès, fenêtres superposées et le service d'accessibilité ont été supprimés de la compilation et du manifeste. Aucune autorisation d'accessibilité n'est requise.

Le verrouillage d'écran Android (PIN, schéma, biométrie ou balayage) reste configuré **uniquement par l'utilisateur dans les paramètres de son téléphone** ; l'application ne le désactive pas. Sur certains appareils, choisir le fond animé pour « accueil et verrouillage » dépend de l'interface constructeur. Le balayage est celui d'Android, pas un geste intercepté par NEON CLOCK.

## APK
GitHub Actions compile **NEON-LOCK-v0.9.0.apk**, signée avec la clé Android de débogage fournie par l'environnement de compilation. Ne pas considérer un build réussi comme une validation matérielle sur HyperOS.

Le nouveau projet ne demande plus `BIND_ACCESSIBILITY_SERVICE` : cela réduit le risque de blocage Play Protect lié aux services d'accessibilité, **sans garantir** qu'une installation hors Play Store ne déclenche aucun avertissement.
