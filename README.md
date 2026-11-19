# NEON LOCK — v0.3.0 (expérimentale)

Application personnelle Android sans root avec **cadran automatique facultatif**.

## Changement principal
- Le fond d'écran animé continue à fonctionner.
- Le nouveau service d'accessibilité offre un **cadran visuel superposé lorsque l’écran est allumé et verrouillé**.
- Active-le dans **NEON LOCK > Personnaliser > Afficher mon cadran au réveil**, puis autorise le service **NEON LOCK — cadran au réveil** dans les réglages d'accessibilité Android.
- **Important :** il peut être nécessaire d'autoriser les « Paramètres restreints » dans **Paramètres > Applications > NEON LOCK > ⋮ > Autoriser paramètres restreints** après installation manuelle. Autorise cette option uniquement si tu fais confiance à l'application.
- Après le bon code visuel, l'interface disparaît et **Android demande encore son déverrouillage habituel**.
- Un bouton « Passer à Android » existe en permanence, notamment pour accéder à l’appel d'urgence du système.
- L'autorisation n'est **jamais** activée sans ton intervention et l'application ne lit pas le contenu des autres applications.
- Sur certains téléphones / versions d'HyperOS, les superpositions peuvent être limitées et nécessiter un test réel.

## Fonctions conservées
- Horloge et cadran rotatif.
- Titre et date activables séparément, masqués par défaut.
- Personnalisation des couleurs et position du cadran.
- Code visuel local (4–6 chiffres).

## Limites
Ne remplace **pas** l'écran de verrouillage sécurisé Android, ni son PIN, sa reconnaissance biométrique ou le mode Appel d'urgence.
Ce cadran **n'est pas un contrôle de sécurité** et n'empêche pas l'accès au système pour une personne connaissant le PIN Android.

## Installation de cette APK de test
Les versions de test GitHub Actions peuvent être signées par différentes clés debug selon l'exécution.
Android peut refuser l'installation au-dessus de v0.2.0. Dans ce cas, désinstalle NEON LOCK et réinstalle la v0.3.0 ; tu devras reconfigurer le code et le fond d'écran. Aucun droit root nécessaire.

Build via GitHub Actions > Releases > v0.3.0. Compilation = validation du code, **pas test réel sur le téléphone**.
