# Boogle Maps

This is a Mock that simulates a Maps WebService where, given a latitude
longitude, will return a random address.

## Instructions

Via shell, it can be started using

```
$ mvn clean package
```

```
$ java -jar target/boogle-maps-0.0.1-SNAPSHOT.jar
```

In a Windows environment, it may be build and run using
```
> build.cmd
```
and
```
> run.cmd
```

The service is available by default on port `9191`. You can check it on the 
command line by using

```
$ curl http://localhost:9191/maps\?lat\=20.0\&lon\=30.0
``` 
or in a browser

[http://localhost:9191/maps?lat=20.0&lon=30.0](http://localhost:9191/maps?lat=20.0&lon=30.0)

You can also import it as a Maven project on your preferred IDE and 
run the class `BoogleMapsApplication`.

## Operations

Swagger UI: [http://localhost:9191/swagger-ui.html](http://localhost:9191/swagger-ui.html)

## Arguments

- --preload.file=`file.json`

  Loads `file.json` from the resources folder and uses it to populate the database. Defaults to [addresses.json](boogle-maps/src/main/resources/addresses.json). 

```json
    [
      {
        "address": "777 Brockton Avenue",
        "city": "Abington",
        "state": "MA",
        "zip": "2351"
      }, {
        "address": "30 Memorial Drive",
        "city": "Avon",
        "state": "MA",
        "zip": "2322"
      }
  ]
```

## Database
To reinitialise the database execute the following commands from the [H2 console](http://localhost:9191/h2-console)
```roomsql
UPDATE ADDRESS_RECORD SET LAT=91.0, LON=181.0 WHERE LAT<>91.0 OR LON<>181.0;
DELETE FROM ADDRESS_VEHICLE;
DELETE FROM VEHICLES;
```
