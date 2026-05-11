# Quickstart

## 1. Bauen

Voraussetzung: Java 21.

```powershell
cd "C:\Users\marco\Desktop\Java\Ridecount-System"
.\gradlew.bat clean build
```

## 2. Installieren

```powershell
Copy-Item ".\build\libs\Ridecount-System-1.0-SNAPSHOT.jar" "C:\path\to\server\plugins\"
```

Danach den Server neu starten.

## 3. Schild platzieren

Neben einem Gleis:

```text
[train]
ridecount
BlueFire
```

Der Spieler braucht zum Erstellen die Permission:

```text
ridecount.sign.create
```

## 4. Testen

1. Spieler in den Zug setzen.
2. Zug ueber das Gleis neben dem Schild fahren lassen.
3. Count pruefen:

```text
/ridecount show <spieler>
```

## Troubleshooting

Wenn das Plugin nicht laedt:

- Java 21 installiert?
- Richtige Jar aus `build/libs/` verwendet?
- Kotlin-Runtime ist im Shadow-Jar enthalten, daher nicht das normale Gradle-Zwischenartefakt kopieren.

Wenn kein Count entsteht:

- Sign steht direkt neben dem Gleis.
- Zeile 1 ist `[train]`.
- Zeile 2 ist `ridecount`.
- Zeile 3 enthaelt den Attraktionsnamen.
- Spieler sitzt wirklich als Passagier im Minecart/Zug.
