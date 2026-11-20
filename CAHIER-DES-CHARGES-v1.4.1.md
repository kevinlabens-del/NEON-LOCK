# NEON LOCK — Cahier des charges de la mise à jour v1.4.1

## Objectif

Étendre NEON LOCK de 7 à **12 fonds d'écran animés**, sans limiter les
nouveaux thèmes à la seule forme ronde du coffre-fort. Maintenir une unique
APK Android native compatible avec l'installation et les mises à jour directes.

## Compatibilité et garanties

- **Vault Original (thème 1) reste inchangé** : photo fournie, molette de métal
  circulaire, chiffres 0–6 toujours à l'endroit, heure HH:mm, rotation fluide.
- L'identifiant Android demeure `com.creatix.neonlock`.
- `versionCode` passe de 19 à **20**, `versionName` à **1.4.1**.
- Même certificat de signature de production que la v1.4.0.
- Les identifiants des sept premiers thèmes restent 0–6, ce qui préserve
  leurs choix de couleur, position, taille, luminosité, vitesse et variantes.
- Les profils locaux, imports/exports JSON, la vérification GitHub de
  nouvelles versions, les réglages jour/nuit et les options d'autonomie restent.
- Le fond demeure décoratif : aucun PIN, aucune interception de l'écran de
  verrouillage, aucune permission d'accessibilité.

## Les 12 scènes

| N° | Nom | Mise en scène |
|---:|---|---|
| 1 | Vault Original | Molette de coffre-fort circulaire d'origine, non modifiée |
| 2 | Cyber Hologram | Anneau HD + panneaux HUD flottants rectangulaires, balayage, circuits |
| 3 | Royal Gold | Cadran de luxe + colonnes Art déco, ornements dorés |
| 4 | Red Reactor | Réacteur HD + structures industrielles, tubes et grilles rouges |
| 5 | Steampunk | Horloge cuivre + conduites et petites turbines à vapeur asymétriques |
| 6 | Galaxy Pulse | Portail HD + nébuleuses, étoiles et fragments cosmiques sur tout l'écran |
| 7 | Arc Reactor | Bobines énergétiques + épaules métalliques, plaques et rails conducteurs |
| 8 | Neon Grid | Terminal angulaire, grillage perspectif synthwave, scanlines |
| 9 | Frost Core | Noyau hexagonal, prismes de glace, facettes et particules froides |
| 10 | Lava Core | Coque rocheuse polygonale, fissures et rivières de lave animées |
| 11 | Shadow Tech | Console rectangulaire furtive, ailettes asymétriques et radar discret |
| 12 | Ocean Pulse | Hublot elliptique, rayons sous-marins, bulles et courbes de courant |

Les six cadrans HD du thème 2 au thème 7 réutilisent l'atlas de v1.4.0 et
bénéficient d'un **décor supplémentaire hors du cercle**. Les thèmes 8–12
sont des dessins vectoriels originaux procéduraux Android Canvas. Ils ne sont
pas présentés comme des photographies ou des captures de l'application.

## Animations, lisibilité et performances

- L'heure centrale reste **HH:mm** ; aucune seconde ajoutée au milieu.
- Les chiffres orbitaux restent verticalement orientés malgré leur
  déplacement. La photographie Vault garde son comportement historique.
- La nouvelle galerie présente douze aperçus, en 2 colonnes et 6 rangées,
  avec rafraîchissement uniquement lorsque la miniature est visible.
- Les animations sont composées hors ligne (aucune connexion pour dessiner).
- Le rendu s'arrête lorsque le fond n'est plus visible.
- L'économie d'énergie et l'adaptation FPS du projet restent disponibles.
- Les décors respectent la position, la taille et la couleur personnalisables.

## Tests de livraison

1. Compiler la variante debug avec Gradle et exécuter les tests JVM.
2. Vérifier les ressources du fond original et l'atlas HD historique.
3. Vérifier l'existence de SceneArt et le nombre de thèmes = 12.
4. Compiler la variante release avec les deux secrets GitHub de signature.
5. Confirmer avec `apksigner` que le SHA‑256 du certificat de signature est
   identique à celui de v1.4.0.
6. Vérifier `packageName`, `versionCode=20` et l'intégrité de l'archive.
7. Publier `NEON-LOCK-v1.4.1.apk` dans GitHub Releases.
8. Après publication, tester visuellement sur appareil Android, car les
   tests GitHub n'équivalent pas à un test matériel sur Redmi/HyperOS.

## Limites honnêtes

Les performances réelles, les effets 3D et l'affichage distinct sur accueil
ou verrouillage dépendent du constructeur Android. L'installation d'une APK
hors Play Store nécessite la confirmation système. Cette version améliore le
décor et la diversité des thèmes, sans prétendre intégrer un moteur 3D natif.
