# RidecountSystem

Kleines Paper-Plugin für ein einfaches Ridecount-System mit Custom-Schild.

## Idee

Wenn ein Zug ein passendes Schild ausgelöst, wird für alle Spieler im Zug der Count der angegebenen Attraktion +1.

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
Wenn `Train_Carts` aktiv ist, nutzt das Plugin eine API-Bridge (MemberBlockChangeEvent).
Ohne `Train_Carts` wird automatisch auf das normale Bukkit-Event-Fallback gewechselt.

