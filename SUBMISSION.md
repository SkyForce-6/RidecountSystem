# Abgabehinweise

## Erfuellte Anforderungen

- Custom-Sign mit Format `[train] / ridecount / <attraction>`
- Count-Erhoehung fuer Spieler im Zug
- YAML-Speicherung fuer die Testaufgabe
- Saubere Trennung zwischen Event-Handling, Service und Storage
- Admin-Commands zum Anzeigen und Loeschen von Counts
- TrainCarts-kompatibler Runtime-Modus ohne harte Build-Abhaengigkeit
- Unit-Tests fuer Service-Logik und Attraktionsnormalisierung

## Build

```powershell
.\gradlew.bat clean build
```

Erzeugtes Jar:

```text
build/libs/Ridecount-System-1.0-SNAPSHOT.jar
```

Voraussetzung: Java 21.

Der Build fuehrt auch die Unit-Tests aus.

## Kurz erklaert

Die Kernlogik liegt im `RidecountService`. Events sammeln nur die betroffenen Spieler und delegieren dann an den Service. Die Speicherung ist ueber `RidecountStorage` abstrahiert, aktuell mit `YamlRidecountStorage`.

TrainCarts wird zur Laufzeit erkannt. Wenn die Bridge aktiv ist, wird der Bukkit-Fallback abgeschaltet, um doppelte Counts zu vermeiden.

## Manuelle Testfaelle

- Schild mit gueltigem Format erstellen
- Schild ohne Attraktionsnamen erstellen
- Spieler ohne `ridecount.sign.create` versucht Schild zu erstellen
- Zug mit einem Spieler loest Count aus
- Zug mit mehreren Spielern loest fuer alle Counts aus
- `/ridecount show <spieler>` zeigt Werte
- `/ridecount clear <spieler> [attraktion]` loescht Werte

## Automatisierte Tests

- `AttractionKeyTest`: prueft stabile YAML-Keys.
- `RidecountServiceTest`: prueft Hochzaehlen, Save-Verhalten und Validierung.
