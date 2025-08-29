# HideAndSeek Minecraft Plugin

Ein vollständiges Hide and Seek Plugin für Minecraft Bukkit/Spigot Server.

## Features

### 🎮 Spielmechanik
- **Zwei Rollen**: Seeker (Suchende) und Hider (Versteckende)
- **10-minütige Versteckphase**: Seeker können sich nicht bewegen und keinen Schaden verursachen
- **Automatische Spielverwaltung**: Das Spiel endet automatisch, wenn alle Hider gefunden wurden
- **Zuschauermodus**: Gefundene Hider und Spieler, die während eines laufenden Spiels beitreten, werden zu Zuschauern

### 📍 Spawnpunkt-System
- Setze einen zentralen Spawnpunkt mit `/setspawn`
- Automatische Konfigurationsspeicherung des Spawnpunkts
- Alle Spieler werden zum Spielstart zum Spawnpunkt teleportiert

### 👥 Rollenverwaltung
- `/setseeker <spieler>` - Bestimmt Spieler als Seeker
- `/removeseeker <spieler>` - Entfernt Seeker-Rolle von Spielern
- Automatische Nametag-Versteckung für Seeker (benötigt TAB Plugin)
- Dynamische Rollenzuweisung: Nicht-Seeker werden automatisch zu Hidern

### 🎯 Kampfsystem
- **Versteckphase**: Kein Schaden möglich
- **Suchphase**: Nur Seeker können Schaden verursachen
- **Elimination**: Hider werden bei Tod zu Zuschauern
- Schadensverhinderung zwischen Hidern

### ✨ Zusatzfunktionen
- `/glowing` - Schaltet Leuchteffekt für alle Hider um/aus
- `/start` - Startet das Spiel mit allen Validierungen
- Automatische Spielerzuordnung bei Spielstart
- Broadcast-Nachrichten für wichtige Spielereignisse

### 📊 PlaceholderAPI Integration
- `%hideandseek_hider_count%` - Anzahl lebender Hider
- `%hideandseek_seeker_names%` - Namen aller Seeker
- `%hideandseek_hiding_time%` - Verbleibende Versteckzeit (MM:SS Format)

## Befehle

| Befehl | Beschreibung | Verwendung |
|--------|--------------|------------|
| `/setspawn` | Setzt den Spawnpunkt | `/setspawn` |
| `/setseeker` | Bestimmt Seeker | `/setseeker <spieler> [spieler...]` |
| `/removeseeker` | Entfernt Seeker | `/removeseeker <spieler> [spieler...]` |
| `/start` | Startet das Spiel | `/start` |
| `/glowing` | Leuchteffekt umschalten | `/glowing` |

## Installation

1. Plugin-JAR in den `plugins/` Ordner kopieren
2. Server neustarten
3. Optional: TAB Plugin installieren für Nametag-Funktionen
4. Optional: PlaceholderAPI installieren für Platzhalter-Support

## Abhängigkeiten

- **Minecraft**: 1.21+
- **Bukkit/Spigot/Paper**: Kompatibel
- **Soft Dependencies**: 
  - PlaceholderAPI (optional)
  - TAB Plugin (optional, für Nametag-Features)

## Konfiguration

Das Plugin erstellt automatisch eine `config.yml` Datei zur Speicherung des Spawnpunkts. Keine manuelle Konfiguration erforderlich.

## Spielablauf

1. Admin setzt Spawnpunkt mit `/setspawn`
2. Admin bestimmt Seeker mit `/setseeker <spieler>`
3. Admin startet Spiel mit `/start`
4. 10-minütige Versteckphase beginnt
5. Nach der Versteckphase können Seeker suchen
6. Spiel endet wenn alle Hider gefunden wurden

## Technische Details

- **API Version**: 1.21
- **Sprache**: Java
- **Versteckzeit**: 10 Minuten (konfigurierbar im Code)
- **Gamemode-Verwaltung**: Automatisches Umschalten zwischen Survival/Spectator
- **Event-System**: Vollständige Integration in Bukkit Events

Dieses Plugin bietet eine komplette Hide and Seek Erfahrung mit professioneller Spielverwaltung und ist sofort einsatzbereit für jeden Minecraft Server.
