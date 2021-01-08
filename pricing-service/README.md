# Pricing Service

The Pricing Service is a REST WebService that simulates a backend that
would store and retrieve the price of a vehicle given a vehicle id as
input. In this project, you will convert it to a microservice.


## Features

- REST WebService integrated with Spring Boot

## Instructions

#### TODOs

- Convert the Pricing Service to be a microservice.
- Add an additional test to check whether the application appropriately generates a price for a given vehicle ID

#### Run the code

To run this service you execute:

```
$ mvn clean package
```

```
$ java -jar target/pricing-service-0.0.1-SNAPSHOT.jar
```
In a Windows environment, it may be build and run using
```
> build.cmd
```
and
```
> run.cmd
```

It can also be imported in your IDE as a Maven project.

## Operations

Swagger UI: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

## Arguments

- --preload.file=`file.json`

  Loads `file.json` from the resources folder and uses it to populate the database.

```json
    [
      {
        "currency": "€",
        "price": 12345,
        "vehicleId": 100
      },
      {
        "currency": "$",
        "price": 6789,
        "vehicleId": 101
      }
    ]
```

## Database
To clear the database execute the following commands from the [H2 console](http://localhost:8082/h2-console)
```roomsql
DELETE FROM PRICE; 
```
