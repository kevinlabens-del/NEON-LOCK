# NEON LOCK v0.2.0

Personnalisation du fond animé sur Android (Java) :
- Masquer ou afficher séparément le titre NEON LOCK et la date. **Tous deux masqués par défaut** pour éviter de recouvrir les éléments système.
- Choisir librement les couleurs du titre, de la date, du cercle, des chiffres et de l'horloge (palette ou code #RRGGBB).
- Positionner le cadran plus bas (**69 %** de hauteur par défaut), avec curseur de 58 à 75 %.
- Aperçu instantané et réglages conservés entre les lancements ; le fond animé relit les préférences à chaque rafraîchissement.
- Conserve le même identifiant d’application et le même stockage de code visuel pour la mise à jour depuis la V0.1.

**Limitations Android :** le fond animé n'agit pas sur les textes de l'écran système et ne remplace ni le déverrouillage système ni le PIN Android.

APK de test signée par le certificat **debug GitHub Actions** publiée dans la Release `v0.2.0` après compilation. Installer par-dessus la version précédente uniquement si Android accepte la même signature de débogage ; sinon, sauvegarder sa configuration et désinstaller la V0.1 avant d’installer (la désinstallation efface le code visuel).
