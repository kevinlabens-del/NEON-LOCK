# NEON LOCK v1.2.4 — fond animé 3D avec mises à jour intégrées

Application Android native `com.creatix.neonlock`, basée sur le visuel de coffre-fort 3D fourni par son créateur.

## Fonctionnement du fond animé

- Une seule molette ronde en métal brossé, **sans porte de coffre-fort autour**.
- Numéros **0 à 6 toujours droits**, même pendant la rotation de la couronne métallique.
- Heure **HH:mm uniquement** dans le disque central ; les secondes peuvent se décompter sous la molette.
- Repère orange immobile, éclairage néon et intensité réglables.
- Couleurs, titre, date, position de la molette et sens de rotation personnalisables.
- Fond animé disponible sur accueil / écran verrouillé selon le constructeur. Aucune fonction de verrouillage propriétaire ni accessibilité.

## Changements v1.2.4

- Le bouton « Réglages du verrouillage Android » a été entièrement retiré.
- Nouvelle vérification des versions officielles publiées dans `kevinlabens-del/NEON-LOCK`.
- Contrôle automatiquement à l'ouverture (au plus toutes les 6 heures) et vérification périodique via JobScheduler (environ toutes les 12 heures, cadence Android non garantie).
- Notifications de nouvelles versions possibles si Android autorise les notifications.
- Bouton « Vérifier les mises à jour » pour demander une vérification immédiate.
- Téléchargement de l'APK via DownloadManager Android avec notification de téléchargement.
- Avant installation, **vérification de l'empreinte SHA-256 officielle**, du nom de package, du numéro de version et de la **signature Android**.
- Installation par l'installateur officiel Android à partir du fichier vérifié. **Confirmation requise** : une application classique ne peut pas garantir de mise à jour silencieuse sans intervention.
- Si le téléchargement s'est terminé quand l'app était fermée, la prochaine ouverture propose l'installation.

## Signature persistante obligatoire

⚠️ **Les versions historiques de test étaient signées par une clé debug potentiellement régénérée par GitHub Actions.** Le nom du package identique ne suffit pas pour mettre à jour une APK existante : Android exige la même clé.

À partir de cette version, le pipeline **ne publie aucune APK officielle si les deux secrets de signature ne sont pas configurés** :

- `NEON_KEYSTORE_BASE64` : contenu base64 du fichier de signature privé .jks (alias `neonlock`).
- `NEON_SIGNING_PASSWORD` : mot de passe commun du magasin et de la clé.

Procédure détaillée : [SIGNATURE-ET-MAJ.md](SIGNATURE-ET-MAJ.md).

La build debug sert **uniquement à tester la compilation** et ne doit pas être distribuée comme une mise à jour.

### Transition unique

Une fois la clé configurée, installer la première APK de release signée avec cette clé. Si une ancienne édition debug du même package est déjà installée et possède un certificat différent, **une désinstallation unique est nécessaire** ; cette opération peut supprimer les réglages de l'ancienne application. Ensuite, toute future APK signée avec cette même clé est installable **par-dessus** la précédente, en conservant les données de l'application.

## Sources officielles

- [Android — app signing](https://developer.android.com/studio/publish/app-signing)
- [Android — DownloadManager](https://developer.android.com/reference/android/app/DownloadManager)
- [Android — PackageManager.canRequestPackageInstalls](https://developer.android.com/reference/android/content/pm/PackageManager#canRequestPackageInstalls())

Aucun système ne contourne Play Protect, les protections Android ou les confirmations d'installation.
