# Ridecount-System

Paper-Plugin für ein Ridecount-System.

## Sign
```text
[train]
ridecount
<attraktion>

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

Java 21.

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
