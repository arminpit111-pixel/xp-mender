# XP Mender (Fabric, Minecraft 1.21.11 Java)

Clientseitige Mod: zählt deine XP-Bottles im Inventar und zeigt per HUD, wie viele du auf welches
Rüstungsteil werfen sollst, damit alles möglichst gleichmäßig gemendet wird.

## Bedienung

- **K** öffnet das Menü (Taste in den Minecraft-Steuerungen unter "XP Mender" änderbar).
- Im Menü das HUD mit der Maus **ziehen**, um es zu verschieben. Position wird automatisch gespeichert.
- Einstellungen: Mod an/aus, Angleichen nach Haltbarkeit oder Prozent, Haupthand/Offhand einbeziehen,
  Haltbarkeit anzeigen, Hintergrund, erwartete XP pro Bottle (3-11, Standard 7).
- Config-Datei: `config/xpmender.json`.

## So wird gerechnet

- Eine XP-Bottle gibt 3-11 XP (Schnitt 7). Mending repariert 2 Haltbarkeit pro XP -> Standard 14 pro Bottle.
- Das am stärksten beschädigte Teil bekommt zuerst Bottles, bis es auf dem Niveau des nächsten Teils ist, usw.
  Reichen die Bottles für alles, wird alles voll repariert und der Rest wird als "Übrig" angezeigt.
- Berücksichtigt werden nur getragene Teile mit Mending, die beschädigt sind.
- Hinweis: Vanilla verteilt XP-Orbs zufällig auf getragene beschädigte Mending-Teile. Die Mod zeigt dir den
  Soll-Zustand; um ihn exakt zu erreichen, musst du jeweils nur das Teil tragen, das gerade dran ist.

## Die fertige .jar bekommen (ohne etwas zu installieren): GitHub baut sie für dich

1. Auf https://github.com ein kostenloses Konto anlegen und ein neues Repository erstellen (z. B. `xp-mender`).
2. Den kompletten Inhalt dieses Ordners hochladen ("Add file" > "Upload files"). Wichtig: auch der versteckte
   Ordner `.github` muss mit hoch (am einfachsten den ganzen Ordnerinhalt per Drag & Drop ziehen).
3. Im Repository auf **Actions** gehen und warten, bis "Mod bauen" grün ist (ca. 2-4 Minuten).
4. Rechts auf **Releases** > "XP Mender (neuester Build)" > die Datei `xp-mender-1.0.0.jar` anklicken.
   Das ist die fertige Mod.
5. Die `.jar` in deinen `mods`-Ordner legen. Zusammen mit dem **Fabric Loader** und dem **Fabric API** für 1.21.11.

## Selbst bauen (Alternative)

Java 21 und Gradle 9.5.1 installieren, im Ordner `gradle build` ausführen.
Die Mod liegt dann in `build/libs/xp-mender-1.0.0.jar`.
