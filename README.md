# NEON LOCK — v0.4.0 (expérimentale)

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


## v0.4 — Saisie automatique du code
Tourner et relâcher la roue. Le chiffre sous la flèche orange est validé automatiquement 450 ms après la fin du geste. Un nouveau toucher pendant ce délai annule la validation. Toucher un chiffre de la couronne le sélectionne aussi. La confirmation manuelle au centre reste facultative. Compatible avec le cadran de test et celui affiché sur l’écran verrouillé.

## v0.5.0 — Fond animé + balayage + cadran
- Nouvelle séquence par défaut : **réveil → fond (sans masque opaque) → geste vers le haut → cadran NEON → verrouillage sécurisé Android**.
- Le code visuel et le service d’accessibilité doivent rester activés ; l'activation du fond animé ne doit plus masquer le cadran par-dessus au réveil.
- Geste détecté par le service de fond animé lorsque le fabricant transmet les événements tactiles au fond.
- Fallback robuste : une petite zone flottante « ↑ BALAYER POUR LE CODE NEON » (glissement ou simple tapotement), placée au-dessus des raccourcis du système. Elle ne couvre pas tout l’écran.
- Nouvelle préférence dans Paramètres : « Afficher d'abord le fond, puis balayer vers le haut », activée par défaut. Désactiver pour retrouver le cadran immédiat.
- La touche « PASSER À ANDROID » reste accessible lorsque le cadran est affiché, indépendamment du code NEON.
- Après le code NEON, **Android exige toujours son propre déverrouillage**. Aucune méthode ne désactive ni ne contourne le Keyguard.
- Limitations : les politiques Android 16 / HyperOS peuvent empêcher le fond de recevoir le glissement ou le service de s'afficher ; tester sur l'appareil.

## v0.6.0 — Transition Android sans deuxième balayage inutile

Après validation du code NEON, une activité visible au-dessus du Keyguard appelle
`KeyguardManager.requestDismissKeyguard()` au lieu de se contenter de retirer la superposition.

- Si Android utilise un simple verrouillage par glissement (non sécurisé), Android **peut** retirer le dernier écran de verrouillage directement.
- Si Android exige un PIN, un schéma, une empreinte ou une autre authentification, cette demande reste obligatoire et se présente via l'interface système.
- Si HyperOS/Android 16 bloque le lancement de l'activité ou la fermeture du Keyguard, le fonctionnement retombe sur l'écran verrouillé d'origine (aucune suppression du verrouillage).
- Le service évite aussi de réafficher son indicateur NEON lors des transitions internes de l'écran verrouillé pendant le même réveil.

Conserve le fond animé, le balayage, la roulette avec confirmation automatique après 450 ms et toutes les options de personnalisation.
**Aucun contournement du verrouillage sécurisé natif.**

## v0.7.0 — Correction de la disparition du code

L'option introduite en v0.6.0 pour fermer automatiquement le Keyguard a été rendue **facultative et désactivée par défaut**, pour éviter qu'Android retourne à l'accueil sans afficher son propre code, ou qu'une transition OEM masque le cadran NEON.

- Conservé : fond d'écran animé → balayage → cadran NEON avec chiffres validés automatiquement après 450 ms.
- Le service réarme le cadran à chaque notification réelle `ACTION_SCREEN_OFF`, y compris lorsque la gestion de l'affichage permanent (AOD) ne fait pas immédiatement passer `PowerManager.isInteractive()` à false.
- Nouvel interrupteur **Après le code NEON → Demander le déverrouillage Android immédiatement (expérimental)** : désactivé par défaut. Peut être essayé après vérification du comportement de l'appareil.
- L'écran paramètres signale si un PIN/schéma/mot de passe sécurisé est réellement configuré sous Android.
- Le code NEON est un contrôle **visuel** facultatif et ne remplace pas la protection d'Android. Si l'on souhaite une sécurité authentique, configurer aussi le PIN du téléphone dans les paramètres système.
- Le bouton « Passer à Android » est conservé pour les cas où le service ou HyperOS n'affiche pas correctement le code.

## v0.7.1 — Diagnostic du service d'accessibilité

- Corrige la comparaison des identifiants du service d'accessibilité : Android peut fournir un nom abrégé et l'application affichait à tort « non autorisé ».
- Distingue « activé dans les paramètres » de « réellement connecté » dans la page de personnalisation.
- Protège le traitement des événements et du réveil contre une exception de l'interface, et affiche une erreur de surcouche si Android refuse le cadran.
- Conserve le fond animé, le geste de balayage, la validation automatique du chiffre (450 ms), et le PIN Android natif.
- Le message Android « Ce service ne fonctionne pas » peut encore nécessiter la désactivation/réactivation du service sur l'appareil. En cas de panne persistante, récupérer la trace Android du crash ; une compilation réussie ne prouve pas le fonctionnement sur HyperOS.
