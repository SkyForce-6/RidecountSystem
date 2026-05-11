# Ridecount-System

Paper/Kotlin-Plugin fuer ein einfaches Ridecount-System mit TrainCarts-Bezug.

```text
[train]
ridecount
<attraction>
```

## Commands

Permission: `ridecount.admin`

```text
/ridecount show <spieler>
/ridecount clear <spieler> [attraktion]
```

## Permissions

```text
ridecount.admin
ridecount.sign.create
```


## Build

Voraussetzung: Java 21.

```powershell
.\gradlew.bat clean build
```

```text
build/libs/Ridecount-System-1.0-SNAPSHOT.jar
```

## Tests

Unit-Tests:

-AttractionKeyTest
-RidecountServiceTest
