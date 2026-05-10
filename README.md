# RidecountSystem

Kleines Paper-Plugin fuer ein einfaches Ridecount-System mit Custom-Schild.

## Idee

Wenn ein Zug ein passendes Schild ausloest, wird fuer alle Spieler im Zug der Count der angegebenen Attraktion erhoeht.

Schildaufbau:

```text
[train]
ridecount
<attraction>
```

Beispiel:

```text
[train]
ridecount
bluefire
```

## Commands

Permission: `ridecount.admin`

```text
/ridecount show <spieler>
/ridecount clear <spieler> [attraktion]
```

## Speicherung

Die Daten werden in YAML gespeichert.

- Config: `plugins/Ridecount-System/config.yml`
- Counts: `plugins/Ridecount-System/ridecounts.yml`

## Build

```powershell
.\gradlew.bat clean shadowJar
```

Das Plugin-Jar liegt danach unter:

`build/libs/Ridecount-System-1.0-SNAPSHOT.jar`

## Hinweis

`Train_Carts` und `TCCoasters` sind als Soft-Dependency eingetragen.
Die Umsetzung nutzt aktuell Event-basiertes Sign-Handling.

