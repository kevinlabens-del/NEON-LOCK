# NEON LOCK v1.5.0 — IMMERSIVE STUDIO

Fond d'écran animé Android natif, **12 thèmes conservés**, avec éditeur tactile
et nouvelles options d'ambiance. Même identifiant Android
`com.creatix.neonlock`, **versionCode 21**, **versionName 1.5.0**.

## Dix évolutions de la version

| # | Amélioration | Fonctionnement réel |
|---:|---|---|
| 1 | Décors immersifs | Les 12 scènes de v1.4.1 et l'atlas HD de six thèmes restent ; les décors personnalisés s'ajoutent aux environnements sans masquer le centre |
| 2 | Éditeur tactile | Glisser pour déplacer le décor et l'horloge ; pincer à deux doigts pour modifier la taille |
| 3 | Animation personnalisée | Quatre intensités influencent la vitesse de rotation et la cadence du rendu, avec effets existants adaptés |
| 4 | Grand aperçu | Bouton d'aperçu élargi, sans outils d'édition ; avant activation du fond |
| 5 | Modes de rendu | Calme (20 FPS), normal (30), intense (40), économie (12), sous réserve des capacités de l'appareil |
| 6 | Horloges interchangeables | Numérique, typographie mécanique, holographique, minimaliste ; **HH:mm uniquement** |
| 7 | Éléments personnalisables | Position de l'horloge indépendante du décor, couleur, taille, décalage de la scène, fond importé |
| 8 | Transitions | Fondu croisé entre les thèmes pendant environ 750 ms, désactivable |
| 9 | Bibliothèque de créations | Variantes nommées et fichiers JSON existants enrichis par les nouveaux réglages Studio ; import/export depuis le téléphone |
| 10 | Diagnostics | Auto-test des 12 thèmes, mémoire, informations de signature, cadence cible, modes et personnalisation locale ; tests JVM en CI |

## Éditeur et import de décor

Ouvre **Immersive Studio** depuis la galerie des thèmes.

- **Décor** : glisse à gauche/droite ou en haut/bas pour changer la position.
- **Horloge** : sélectionne le mode Horloge et glisse pour changer sa place.
- **Zoom** : rapproche ou écarte deux doigts pour ajuster la dimension du décor.
- **Horloge** : personnalise le style et la couleur de l'inscription HH:mm.
- **Image importée** : sélectionne une photo PNG/JPEG via Android Documents.
  Elle est copiée en JPEG compressé dans le stockage privé de l'application et
  reste sur le téléphone, sans upload.
- L'image de fond importée se mélange au décor à l'extérieur du mécanisme,
  afin de conserver l'heure et les chiffres lisibles.
- La photo et la molette de **Vault Original restent inchangées**.
- Un bouton permet de retirer l'image ajoutée, sans modifier tes autres thèmes.

Les préférences Studio sont dans le même stockage local que les préférences
de v1.4.1, sans remise à zéro. Les variantes sauvegardées incluent la
position, la taille, la couleur et le style de l'horloge. Les photos restent
locales, ne sont **pas embarquées** dans les exports JSON.

## Fluidité et sécurité

- Le fond animé suspend son rendu lorsqu'il n'est pas visible.
- Les quatre modes adaptent les FPS ; la batterie Android limite à 12 FPS.
- Les transitions utilisent temporairement deux scènes superposées.
- L'application ne superpose aucun élément à l'écran de verrouillage Android.
- Pas de code PIN propriétaire, pas d'AccessibilityService.
- Les mises à jour continuent à être vérifiées via GitHub Releases,
  avec SHA-256 et certificat avant l'installateur Android.

## Distribution

`versionCode=21` ; APK de release créée par GitHub Actions avec **la même
clé privée stable** que les APK v1.4.0/v1.4.1.

https://github.com/kevinlabens-del/NEON-LOCK/releases/tag/v1.5.0

Compilation avec Java 17 / Android Gradle Plugin / SDK 35, tests JVM
`ThemeMathTest` et `StudioMathTest`. GitHub vérifie aussi l'empreinte du
certificat attendue.

**Limite importante :** GitHub Actions teste la compilation, les fichiers et
la signature ; il ne remplace pas un test réel de fluidité ni du rendu tactile
sur un Redmi/HyperOS. Le rendu avancé reste du **Canvas Android 2D avec effets
de profondeur visuelle**, pas un moteur de 3D temps réel photoréaliste.

Le fichier `CAHIER-DES-CHARGES-v1.4.1.md` documente la phase précédente.
