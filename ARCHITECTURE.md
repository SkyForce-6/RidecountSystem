# Architektur

Das Projekt ist klein, aber bewusst in klare Verantwortlichkeiten getrennt.

## Komponenten

```text
RidecountSystem
  -> Plugin-Lifecycle, Dependency Wiring, Auto-Save

RidecountSignListener
  -> Sign-Erstellung, Permission-Pruefung, Bukkit-Fallback

TrainCartsApiBridge
  -> optionale TrainCarts-Anbindung per Reflection

RidecountService
  -> validiert Attraktionsnamen und erhoeht Counts fuer Spieler

RidecountStorage
  -> Interface fuer Persistierung

YamlRidecountStorage
  -> YAML-Datei lesen, schreiben und normalisierte Pfade verwalten

AttractionKey
  -> einheitliche Normalisierung von Attraktionsnamen fuer Storage-Keys

PassengerCollector
  -> sammelt Spieler rekursiv aus Entity-Passagieren

RidecountCommand
  -> Admin-Kommandos zum Anzeigen und Loeschen
```

## Datenfluss

```text
Zug erreicht Ridecount-Schild
  -> TrainCartsApiBridge oder Bukkit-Fallback
  -> Spieler im Zug sammeln
  -> RidecountService.incrementForPlayers(...)
  -> RidecountStorage.increment(...)
  -> ridecounts.yml
```

## Wichtige Entscheidungen

`RidecountService` haengt nur am `RidecountStorage`-Interface. Dadurch ist die Speicherung austauschbar. Eine spaetere Datenbank-Implementierung muesste nur dasselbe Interface implementieren.

Die TrainCarts-Anbindung ist optional. Der Bukkit-Fallback bleibt als Reserve aktiv. Beide Wege teilen sich denselben Cooldown, damit Counts nicht doppelt entstehen.

Die YAML-Speicherung normalisiert Attraktionsnamen, damit aus Namen wie `Blue Fire` stabile Keys wie `blue_fire` werden.

Die fachliche Kernlogik ist mit Unit-Tests abgedeckt. Bukkit-spezifische Integration bleibt bewusst schmal und delegiert schnell an getestete Klassen.

## Grenzen

Fuer die Testaufgabe speichert das Plugin synchron in YAML. Das ist fuer kleine Datenmengen ausreichend und leicht nachvollziehbar. Fuer ein produktives Parksystem waeren SQLite/MySQL, Batch-Writes und Offline-Player-Namensauflösung die naechsten sinnvollen Schritte.
