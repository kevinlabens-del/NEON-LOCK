# NEON LOCK v1.2.2 — heure intégrée au disque central 3D

Cette édition intègre **l'image fournie par l'utilisateur** dans le dossier Android `drawable-nodpi/neon_vault_reference.jpg` et respecte son identité : **une seule molette ronde**, sans porte de coffre-fort, charnières ou décor rectangulaire.

## Fond animé photographique
- Image 3D d'origine comme texture (métal brossé chromé, reflets bleus, néon orange, véritable relief visuel).
- La molette métallique est découpée virtuellement dans la photo d'origine, **sans les anciens chiffres pré-imprimés**. Une nouvelle texture intérieure `neon_vault_wheel_clean.jpg` préserve le relief du métal. Les **sept chiffres 0 1 2 3 4 5 6** sont placés à leurs positions tournantes puis rendus séparément **sans rotation des glyphes**, donc toujours droits et lisibles, y compris après un demi-tour.
- La bague mobile tourne continûment en une minute, synchronisée avec les secondes réelles de l'appareil.
- L'inscription statique « 15:56:24 » de l'image est remplacée à l'affichage par une **heure HH:mm:ss dynamique**. Les secondes s'illuminent en orange ou dans la couleur choisie.
- Décompte des secondes restantes facultatif sous l'horloge.
- Rafraîchissement toutes les 33 ms quand le fond animé est visible; les tâches se suspendent autrement.

## Personnalisation
- **Couleur des LEDs et du repère** : choix de palette ou hexadécimal personnalisé. Les pixels orange du visuel sont recolorés pour préserver les reflets de métal.
- **Puissance de l'éclairage néon** : réglage de 40 à 245.
- **Sens de rotation** : normal ou inverse.
- **Décompte** : afficher/masquer.
- **Titre/date** : affichage et couleur optionnels.
- **Couleur de l'heure, des chiffres et de la date**, ainsi que **position verticale** et remise à zéro des réglages.

## Sécurité et installation
L'application demeure un **simple fond d'écran animé** : aucun PIN NEON, aucune autorisation d'accessibilité, aucune interception des gestes ni du verrouillage Android. Le verrouillage et le balayage sont gérés séparément par le téléphone.

GitHub Actions assemble l'APK `NEON-LOCK-v1.2.2.apk` (signature debug). Le build ne prouve pas un fonctionnement parfait sur tous les modèles HyperOS. Si Android autorise le fond animé sur l'écran de verrouillage, activer l'option « écran d'accueil et écran verrouillé ».

## v1.2.2 — Suppression du rectangle noir central
- Nouveau visuel `neon_vault_center_clean.jpg` : heure et indicateurs imprimés effacés de la photographie d'origine pour retrouver la surface sombre et métallisée du centre, sans rectangle ajouté par-dessus.
- Les heures, minutes et secondes réelles sont inscrites **directement sur le disque rond central**, sous la poignée chromée.
- Le décompte apparaît plus bas dans le disque, sous la ligne orange d'origine ; ajustement de la taille pour ne pas dépasser du cercle.
- Les chiffres 0–6 restent toujours droits pendant la rotation et tous les réglages existants sont conservés.
