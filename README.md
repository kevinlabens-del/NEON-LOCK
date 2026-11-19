# NEON LOCK v1.4.0 — ULTRA 3D EDITION

Android native live wallpaper (`com.creatix.neonlock`) with 7 themes, original designs, offline settings, premium 3D imagery and signed in-app updates. **Upgrades in place from v1.3.0** using the established, permanent private signing key.

## The seven themes

1. Vault Original — preserves the real photo-based metal safe dial and original settings.
2. Cyber Hologram — cobalt/cyan 3D holographic circular panels with scanning lights.
3. Royal Gold — golden mechanical clock ring, jeweled metal highlights and moving glints.
4. Red Reactor — red industrial power ring, pulsing coils.
5. Steampunk — brass/copper gears and animated steam.
6. Galaxy Pulse — violet nebula and orbiting energy/stars.
7. Arc Reactor — independent sci-fi blue energy core design (not an official Iron Man/Marvel product).

For the six newer themes, a single optimized offline JPEG atlas (`neon_v140_hd_atlas.jpg`) provides photographic mechanical materials. Dynamic animations, hour text and upright digits are **rendered separately** in Android Canvas. The artwork is not an exact capture of the earlier promotional mockup.

## All 10 enhancements included

| # | Enhancement | Implementation |
|--:|---|---|
| 1 | Photo-realistic HD theme materials | Original HD offline atlas + Canvas highlights with individual ring textures; original Vault photo unchanged |
| 2 | Parallax/depth | Wallpaper `onOffsetsChanged`, subtle parallax for imagery and backgrounds when the launcher provides offsets |
| 3 | Dedicated motions per theme | Cyber scanner, Gold glints, Red coil pulses, Steampunk steam wisps, Galaxy twinkles, Arc coils, Vault metal wheel |
| 4 | Theme designer | Material (steel/titanium/brass/carbon), numeral style, central HUD treatment, colors, brightness, speed, size |
| 5 | Entry animation | Short non-blocking 1.65-second easing and ring-light startup when the live wallpaper becomes visible |
| 6 | Intelligent day/night style | Automatic night palette (22:00–07:00) and existing nocturnal brightness option, independent per theme |
| 7 | Battery performance | Atlas bitmap cached once, drawing stops when hidden, 30 FPS standard, 15 FPS saver, 12 FPS when Android battery saver is active |
| 8 | Home/lock profiles | Separate vertical positions for home vs lock **on Android 14+ when the system exposes distinct wallpaper flags**; fallback to shared position |
| 9 | Personal creation library | Save, rename, recall, delete up to 30 named variants, JSON import/export via Android document picker |
| 10 | Diagnostics and automated tests | In-app offline render tests for 7 themes + report; JUnit tests for geometry/time/rotation; CI validates package, assets, signer and version |

## User controls

- **Gallery**: choose a theme with a live preview, press *Activate live wallpaper* to apply it.
- **Personalize theme**: per-theme color, brightness, size, position, rotation direction and speed.
- **3D workshop**: advanced materials, digits, dial core, night palette, parallax, startup animation, separate lock position, saved variants and JSON backups.
- **Diagnostics**: render smoke tests, memory/fps inspection and check whether the installed certificate matches the permanent release signer.
- **Auto theme cycling**: optional; same settings and schedule as v1.3.0.
- **Automatic updates**: checks official GitHub Releases, verifies SHA-256 and Android signature, downloads and asks Android to install. No silent installation or security bypass.

All hour displays remain **HH:mm** (never seconds inside the central circle). 0–6 digits remain upright as the positions rotate. No fake lock screen, no PIN overlay, no AccessibilityService.

## Build and signing

- Android Java, AGP 8.7.3 / compile SDK 35, min SDK 28, target SDK 35.
- Version name **1.4.0**, versionCode **19**, same `applicationId`.
- GitHub Actions tests every PR without secret access; signed Releases only on `main`.
- Persistent private signing configured with GitHub Actions Secrets:
  - `NEON_KEYSTORE_BASE64`
  - `NEON_SIGNING_PASSWORD`
- The CI release gate requires the expected signing certificate fingerprint matching the v1.2.4/v1.3.0 official APKs, and a valid package/version.
- APK file: `NEON-LOCK-v1.4.0.apk`.

## Compatibility limitations

Android wallpaper flags for distinguishing system/lock wallpapers are available starting **API 34**. Some Xiaomi/HyperOS versions may share one engine for both screens or suppress launcher offsets; the feature gracefully uses the standard position or static artwork when not supported. Parallax does not bypass launcher constraints.

Automatic checking and notifications are best effort under Android battery policies. The app cannot silently install unsigned or differently signed APKs. Runtime render diagnostics are lightweight smoke tests, **not a guarantee of appearance or battery runtime on every device**.

## Reference

- [Signing and updates](SIGNATURE-ET-MAJ.md)
- [Android WallpaperService.Engine](https://developer.android.com/reference/android/service/wallpaper/WallpaperService.Engine)
- [Android app signing](https://developer.android.com/studio/publish/app-signing)
