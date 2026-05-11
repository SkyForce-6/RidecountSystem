# Praesentation

## 30-Sekunden-Erklaerung

Ich habe ein Paper/Kotlin-Plugin gebaut, das Ridecounts fuer TrainCarts-Attraktionen zaehlt. Ein Schild im Format `[train] / ridecount / <attraction>` wird neben ein Gleis gestellt. Wenn ein Zug daran vorbeifaehrt, bekommen alle Spieler im Zug fuer diese Attraktion einen Count in einer YAML-Datei.

Der Code ist so getrennt, dass Events nur Spieler und Attraktion erkennen. Die eigentliche Fachlogik liegt im Service, die Speicherung hinter einem Storage-Interface. Dadurch kann YAML spaeter durch eine Datenbank ersetzt werden.

## Dateien zum Zeigen

- `RidecountSystem.kt`: Plugin-Start, Wiring, Auto-Save
- `RidecountSignListener.kt`: Schild-Erstellung und Bukkit-Fallback
- `TrainCartsApiBridge.kt`: optionale TrainCarts-Anbindung
- `RidecountService.kt`: zentrale Count-Logik
- `RidecountStorage.kt`: austauschbare Storage-Abstraktion
- `YamlRidecountStorage.kt`: konkrete YAML-Speicherung

## Gute Punkte im Code

- Kleine Klassen mit klaren Aufgaben
- Keine direkte Kopplung zwischen Service und YAML-Implementierung
- Fallback wird deaktiviert, wenn die TrainCarts-Bridge aktiv ist
- Attraktionsnamen werden normalisiert
- Counts werden beim Triggern, im Auto-Save und beim Disable gespeichert
- Unit-Tests decken die fachliche Kernlogik ab

## Ehrliche Grenzen

- YAML ist fuer die Testaufgabe okay, fuer produktive Nutzung waere eine Datenbank besser.
- Die TrainCarts-Bridge nutzt Reflection, damit das Projekt ohne lokale TrainCarts-API baubar bleibt.
- Eine produktive Version sollte eine direkte TrainCarts-API-Dependency und Datenbank-Persistierung bekommen.
