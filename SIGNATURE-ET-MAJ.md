# Activer les mises à jour sans réinstaller NEON LOCK

Ce guide se suit directement depuis le téléphone, sans PC.

## Étape 1 — Conserver la clé et le mot de passe

Les deux fichiers secrets sont fournis directement au propriétaire du projet hors GitHub :
- `NEON-LOCK-secret-NEON_KEYSTORE_BASE64.txt` (clé privée encodée en base64)
- `NEON-LOCK-secret-NEON_SIGNING_PASSWORD.txt` (mot de passe de cette clé)

Les conserver dans un lieu privé et sûr. **Ne jamais les publier dans un dépôt GitHub public, un message, un commit ou un ticket.** Le fichier `.jks` privé est une copie de sauvegarde.

## Étape 2 — Créer les deux GitHub Actions Secrets

Sur le téléphone, ouvrir :

https://github.com/kevinlabens-del/NEON-LOCK/settings/secrets/actions

Cliquer « New repository secret » :

1. Nom : `NEON_KEYSTORE_BASE64`. Valeur : copier **tout le contenu d'une seule ligne** du fichier `NEON-LOCK-secret-NEON_KEYSTORE_BASE64.txt`. Enregistrer.
2. Nom : `NEON_SIGNING_PASSWORD`. Valeur : copier **tout le contenu** du fichier `NEON-LOCK-secret-NEON_SIGNING_PASSWORD.txt`. Enregistrer.

Ne pas remplacer les noms par ceux des fichiers. Ne pas joindre le fichier `.jks` dans le dépôt.

## Étape 3 — Lancer la compilation et récupérer la release

https://github.com/kevinlabens-del/NEON-LOCK/actions/workflows/android.yml

Choisir « Run workflow » sur la branche `main`. Le workflow reconstruit l'APK `NEON-LOCK-v1.2.4.apk` et la publie dans **Releases** uniquement lorsque les deux secrets sont disponibles.

https://github.com/kevinlabens-del/NEON-LOCK/releases/tag/v1.2.4

Le journal « Verify APK signature and release artifact » confirme que la signature existe ; la clé privée n'apparaît jamais dans les logs.

## Étape 4 — Passage unique depuis les anciennes APK

Les anciennes versions de test utilisaient une signature debug non conservée. Android n'accepte pas leur remplacement par une APK signée avec une nouvelle clé.

**Une seule fois**, pour passer à la nouvelle chaîne officielle :

1. Sauvegarder ou noter les réglages visuels importants.
2. Désinstaller l'ancienne NEON LOCK.
3. Installer la v1.2.4 officielle signée depuis GitHub Releases.
4. Appliquer à nouveau le fond animé si nécessaire.

À partir de là, **ne plus désinstaller** NEON LOCK : les versions suivantes doivent être signées avec exactement les **mêmes deux secrets**. Le système de mises à jour intégré vérifie GitHub, télécharge, contrôle SHA-256 et propose l'installation.

## Limitations légitimes d'Android

- Une app normale distribuée hors Play Store doit toujours obtenir la confirmation système pour une installation, et parfois l'autorisation de source inconnue.
- Les contrôles périodiques en arrière-plan dépendent d'Android, de la connexion et des réglages de batterie/notifications.
- Google Play Protect peut analyser l'APK. Ne pas désactiver Play Protect pour contourner un blocage.
- Ne pas perdre les deux fichiers privés : la perte de la clé empêche les futures mises à jour par-dessus la même application.

## Vérification pratique

La clé n'est jamais stockée dans GitHub. Seule sa version base64 est enregistrée dans les **GitHub Actions Secrets chiffrés** du dépôt, puis décodée temporairement pendant une compilation signée.
