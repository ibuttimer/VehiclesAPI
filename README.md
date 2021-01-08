# ND035-P02-VehiclesAPI-Project

Project repository for JavaND Project 2, where students implement a Vehicles API using Java and Spring Boot that can communicate with separate location and pricing services.

Original code available at https://github.com/udacity/JDND/tree/master/projects/P02-VehiclesAPI as part of [Java Web Developer Nanodegree](https://www.udacity.com/course/java-developer-nanodegree--nd035)
by Udacity

## Instructions

Check each component to see its details and instructions. Note that all three applications
should be running at once for full operation. Further instructions are available in the classroom.

- [Vehicles API](vehicles-api/README.md)
- [Pricing Service](pricing-service/README.md)
- [Boogle Maps](boogle-maps/README.md)

## Dependencies

The project requires the use of Maven and Spring Boot, along with Java v11.

## Implementation

- A Eureka server has been added to the project.
  
    See [Eureka Server](eureka-server/README.md) for more details.

- All three applications have been implemented as Eureka client microservices with Spring Boot Actuator, H2 database and Swagger UI. 
- [links.html](links.html) provides convenient access to all the applications.
- [Pricing Service](pricing-service/README.md)
    + Prices are stored on a per-vehicle basis.
    + Prices may be updated and deleted as required. 
    + Database may be populated from a [json file](pricing-service/README.md#Arguments) on application boot.
- [Boogle Maps](boogle-maps/README.md)
    + Addresses are allocated randomly from a fixed pool and stored on a per-vehicle and location basis.
    + If a vehicle location changes, the previously allocated address is released, and a new address allocated.
    + Address allocations may be deleted as required.
    + Database address table is populated from a [json file](boogle-maps/src/main/resources/addresses.json) on application boot. 
- [Vehicles API](vehicles-api/README.md)
    + Vehicle data may be stored, updated and deleted.
    + Utilises [Pricing Service](pricing-service/README.md) to provide pricing data.
    + Utilises [Boogle Maps](boogle-maps/README.md) to provide address data.
    + Database manufacturer table is populated from a [json file](vehicles-api/src/main/resources/manufacturers.json) on application boot. See [Arguments](vehicles-api/README.md#arguments).
    + Database car table may be populated from a [json file](vehicles-api/src/main/resources/manufacturers.json) on application boot.. See [Arguments](vehicles-api/README.md#arguments).

## Build and Test
1. [Eureka Server](eureka-server/README.md)
   
    No special requirements
1. [Pricing Service](pricing-service/README.md) and [Boogle Maps](boogle-maps/README.md)

    No special requirements
1. [Vehicles API](vehicles-api/README.md)

    Please see [Build and Test](vehicles-api/README.md#build-and-test) for details

## Execution
1. Start [Eureka Server](eureka-server/README.md)
1. Start [Pricing Service](pricing-service/README.md) and [Boogle Maps](boogle-maps/README.md)
1. Start [Vehicles API](vehicles-api/README.md)

*Note:*
[Vehicles API](vehicles-api/README.md), [Pricing Service](pricing-service/README.md) and [Boogle Maps](boogle-maps/README.md) may be started in any order but [Eureka Server](eureka-server/README.md) should be started first.



