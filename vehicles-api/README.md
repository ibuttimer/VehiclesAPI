# Vehicles API

A REST API to maintain vehicle data and to provide a complete
view of vehicle details including price and address.

# Table of Contents
1. [Features](#features)
1. [Instructions](#instructions)
    1. [TODOs](#todos)
    1. [Run the Code](#run-the-code)
1. [Build and Test](#build-and-test)
1. [Arguments](#arguments)
1. [Operations](#operations)
    1. [Create a Vehicle](#create-a-vehicle)
    1. [Retrieve a Vehicle](#retrieve-a-vehicle)
    1. [Update a Vehicle](#update-a-vehicle)
    1. [Delete a Vehicle](#delete-a-vehicle)
1. [Database](#database)

## Features

- REST API exploring the main HTTP verbs and features
- Hateoas
- Custom API Error handling using `ControllerAdvice`
- Swagger API docs
- HTTP WebClient
- MVC Test
- Automatic model mapping

## Instructions

#### TODOs

- Implement the `TODOs` within the `CarService.java` and `CarController.java`  files
- Add additional tests to the `CarControllerTest.java` file based on the `TODOs`
- Implement API documentation using Swagger

#### Run the Code

To properly run this application you need to start the Orders API and
the Service API first.

```
$ mvn clean package
```

```
$ java -jar target/vehicles-api-0.0.1-SNAPSHOT.jar
```
In a Windows environment, it may be build and run using
```
> build.cmd
```
and
```
> run.cmd
```

Import it in your favorite IDE as a Maven Project.

## Build and Test
There are three test classes in the project:
- [VehiclesApiApplicationTests](src/test/java/com/udacity/vehicles/VehiclesApiApplicationTests.java)
  
  Basic context load test
- [CarControllerTest](src/test/java/com/udacity/vehicles/api/CarControllerTest.java)

  Mocked test
- [CarControllerIntegrationTest](src/test/java/com/udacity/vehicles/api/CarControllerIntegrationTest.java)

  Integration test, requiring other services to be running. The environment variable `SPRING_PROFILES_ACTIVE` should be set according to the following table to avoid unnecessary test failure.

|SPRING_PROFILES_ACTIVE|Required     |Comment|
|----------------------|-------------|-------|
|not set|<ul><li>[x] Eureka server</li><li>[x] Pricing service</li><li>[x] Maps service</li></ul>|Can run all tests |
|"test" |<ul><li>[ ] Eureka server</li><li>[ ] Pricing service</li><li>[ ] Maps service</li></ul>|VehiclesApiApplicationTests and CarControllerTest only.|

As tests are run as part of the 
```
$ mvn clean package
```
and
```
> build.cmd
```
commands, please ensure to set the environment variable `SPRING_PROFILES_ACTIVE` according to the table above.

## Arguments

- --preload.manufacturer.file=`file.json`

  Loads `file.json` from the resources folder and uses it to populate the database. Defaults to Defaults to [manufacturers.json](src/main/resources/manufacturers.json).

```json
    [
      {
        "code": 100,
        "name": "Audi"
      },{
        "code": 101,
        "name": "Chevrolet"
      }
  ]
```
- --preload.car.file=`file.json`

  Loads `file.json` from the resources folder and uses it to populate the database.

```json
  [
    {
      "condition": "NEW",
      "details": {
        "body": "hatchback",
        "model": "A5 40 Tfsi S Line Mhev",
        "manufacturer": {
          "name": "Audi"
        },
        "numberOfDoors": 2,
        "fuelType": "petrol",
        "engine": "2.0LHyb/PULP",
        "mileage": 2000,
        "modelYear": 2021,
        "productionYear": 2021,
        "externalColor": "silver"
      }
    }
  ]
```

## Operations

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Create a Vehicle

`POST` `/cars`
```json
{
   "condition":"USED",
   "details":{
      "body":"sedan",
      "model":"Impala",
      "manufacturer":{
         "code":101,
         "name":"Chevrolet"
      },
      "numberOfDoors":4,
      "fuelType":"Gasoline",
      "engine":"3.6L V6",
      "mileage":32280,
      "modelYear":2018,
      "productionYear":2018,
      "externalColor":"white"
   },
   "location":{
      "lat":40.73061,
      "lon":-73.935242
   }
}
```

### Retrieve a Vehicle

`GET` `/cars/{id}`

This feature retrieves the Vehicle data from the database
and access the Pricing Service and Boogle Maps to enrich 
the Vehicle information to be presented

### Update a Vehicle

`PUT` `/cars/{id}`

```json
{
   "condition":"USED",
   "details":{
      "body":"sedan",
      "model":"Impala",
      "manufacturer":{
         "code":101,
         "name":"Chevrolet"
      },
      "numberOfDoors":4,
      "fuelType":"Gasoline",
      "engine":"3.6L V6",
      "mileage":32280,
      "modelYear":2018,
      "productionYear":2018,
      "externalColor":"white"
   },
   "location":{
      "lat":40.73061,
      "lon":-73.935242
   }
}
```

### Delete a Vehicle

`DELETE` `/cars/{id}`

## Database
The default database configuration is

|H2 console|JDBC URL|Username|Password|
|---|---|---|---|
|[http://localhost:8080/h2-console](http://localhost:8080/h2-console)|jdbc:h2:mem:vehicledb|sa| | 

To clear the database execute the following commands from the [H2 console](http://localhost:8080/h2-console)
```roomsql

```

