# NEON LOCK v1.4.1 — Immersive Environments

Application Android de fonds d'écran animés avec **12 thèmes 3D**
personnalisables dans **une seule APK signée**, sans connexion permanente.

## Les douze thèmes

| # | Thème | Décor |
|---:|---|---|
| 1 | **Vault Original** | Molette ronde photographique acier/orange **inchangée** |
| 2 | Cyber Hologram | Anneau HD, grands panneaux holographiques rectangulaires et balayage |
| 3 | Royal Gold | Cadran doré, colonnes Art déco et ornements |
| 4 | Red Reactor | Réacteur rouge, structures industrielles, tubes et grilles |
| 5 | Steampunk | Horloge en cuivre, conduites, vapeur et turbines extérieures |
| 6 | Galaxy Pulse | Portail violet, nébuleuses et champs d'étoiles pleine surface |
| 7 | Arc Reactor | Réacteur bleu original, supports mécaniques et conducteurs latéraux |
| 8 | **Neon Grid** | Terminal HUD angulaire avec grille synthwave en perspective |
| 9 | **Frost Core** | Cristaux de glace, facettes et noyau hexagonal |
| 10 | **Lava Core** | Coque polygonale, rivières de magma et braises |
| 11 | **Shadow Tech** | Console furtive, ailettes rectangulaires, capteurs radar |
| 12 | **Ocean Pulse** | Hublot elliptique, lumière aquatique, bulles et courants |

Les six anciens thèmes HD bénéficient désormais d'un **décor extérieur**
qui n'est pas simplement un cercle isolé. Les thèmes 8–12 sont des scènes
originales **Android Canvas**, avec formes et mouvements adaptés au décor.
Ils ne sont pas des images photo-réalistes identiques aux maquettes.

## Fonctions conservées

- **Horloge HH:mm** lisible au centre, sans secondes supplémentaires.
- Chiffres 0–6 maintenus **toujours à l'endroit**, même en rotation.
- Galerie à 12 aperçus animés et application immédiate du thème choisi.
- Personnalisation indépendante des couleurs, matériaux, chiffres, noyau,
  intensité, taille, position et vitesse.
- Profils locaux enregistrés, import et export JSON des créations.
- Cycle automatique des 12 thèmes (facultatif), ambiances jour/nuit,
  animations d'activation et parallaxe lorsque le lanceur fournit les offsets.
- Gestion de l'autonomie : pause quand le fond est invisible, mode 15 FPS et
  adaptation à l'économiseur de batterie Android.
- Position accueil / verrouillage distincte lorsque le constructeur expose
  deux moteurs Android compatibles (sinon position commune).
- Diagnostics locaux et tests Gradle, sans collecte d'identifiants.
- Vérification des futures versions via GitHub, téléchargement avec
  validation SHA-256/certificat et confirmation par l'installateur Android.
- Aucun code PIN personnalisé, aucune surveillance, aucun AccessibilityService.

## Mise à jour depuis v1.4.0

`versionCode=20` · `versionName=1.4.1` ·
`applicationId=com.creatix.neonlock`.

La release est signée avec la **même clé privée permanente** que v1.4.0,
afin de pouvoir l'installer par-dessus sans perte des profils de l'appli.
Cette continuité est vérifiée dans le pipeline GitHub Actions en comparant
le certificat attendu.

La publication officielle est disponible sur :
https://github.com/kevinlabens-del/NEON-LOCK/releases/tag/v1.4.1

## Développement et tests

Android Java 17 / Canvas · minSdk 28 · targetSdk 35 · JUnit/Gradle en CI.
Les fonds HD préexistants réutilisent `neon_v140_hd_atlas.jpg`, et Vault
Original garde ses images d'origine. Les scènes 8–12 sont intégrées dans
`SceneArt.java` et ne nécessitent aucun téléchargement externe.

La compilation GitHub garantit une APK Android produite et signée.
Elle ne prouve pas à elle seule les performances ni le rendu visuel sur un
appareil Redmi/HyperOS : valider ensuite sur téléphone réel.

Cahier de la version : [CAHIER-DES-CHARGES-v1.4.1.md](CAHIER-DES-CHARGES-v1.4.1.md).
Documentation des clés : [SIGNATURE-ET-MAJ.md](SIGNATURE-ET-MAJ.md).
