package com.udacity.vehicles.api;


import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.service.CarService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.udacity.vehicles.config.Config.*;
import static com.udacity.vehicles.config.OpenApiConfig.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Implements a REST-based controller for the Vehicles API.
 */
@RestController
@ApiResponses(value = {
    @ApiResponse(responseCode = BAD_REQUEST, description = "This is a bad request, please follow the API documentation for the proper request format."),
    @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = "The server is down. Please make sure that the Vehicle microservice is running.")
})
@RequestMapping(CARS_URL)
class CarController {

    private final CarService carService;
    private final CarResourceAssembler assembler;

    CarController(CarService carService, CarResourceAssembler assembler) {
        this.carService = carService;
        this.assembler = assembler;
    }

    /**
     * Creates a list to store any vehicles.
     * @return list of vehicles
     */
    @GetMapping
    CollectionModel<EntityModel<Car>> list() {
        List<EntityModel<Car>> resources = carService.list().stream().map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(resources,
                linkTo(methodOn(CarController.class).list()).withSelfRel());
    }

    /**
     * Gets information of a specific car by ID.
     * @param id the id number of the given vehicle
     * @return all information for the requested vehicle
     */
    @ApiResponses(value = {
        @ApiResponse(responseCode = NOT_FOUND, description = "A record could not be found matching the request, please verify the request parameters."),
    })
    @GetMapping(CARS_GET_BY_ID_URL)
    EntityModel<Car> get(@PathVariable Long id) {
        /**
         * TODO: Use the `findById` method from the Car Service to get car information.
         * TODO: Use the `assembler` on that car and return the resulting output.
         *   Update the first line as part of the above implementing.
         */
        Car car = carService.findById(id);
        return assembler.toModel(car);
    }

    /**
     * Posts information to create a new vehicle in the system.
     * @param car A new vehicle to add to the system.
     * @return response that the new vehicle was added to the system
     * @throws URISyntaxException if the request contains invalid fields or syntax
     */
    @ApiResponses(value = {
        @ApiResponse(responseCode = CREATED, description = "Entity successfully created"),
    })
    @PostMapping
    ResponseEntity<?> post(@Valid @RequestBody Car car) throws URISyntaxException {
        /**
         * TODO: Use the `save` method from the Car Service to save the input car.
         * TODO: Use the `assembler` on that saved car and return as part of the response.
         *   Update the first line as part of the above implementing.
         */
        car.setId(null);    // new addition, so id is assigned
        Car newCar = carService.save(car.ensureValid());
        EntityModel<Car> resource = assembler.toModel(newCar);
        return ResponseEntity.created(
                    linkTo(Car.class)
                            .slash(Objects.requireNonNull(resource.getContent()).getId())
                            .withSelfRel()
                            .toUri()
            ).body(resource);
    }

    /**
     * Updates the information of a vehicle in the system.
     * @param id The ID number for which to update vehicle information.
     * @param car The updated information about the related vehicle.
     * @return response that the vehicle was updated in the system
     */
    @ApiResponses(value = {
        @ApiResponse(responseCode = NOT_FOUND, description = "A record could not be found matching the request, please verify the request parameters."),
    })
    @PutMapping(CARS_PUT_BY_ID_URL)
    ResponseEntity<?> put(@PathVariable Long id, @Valid @RequestBody Car car) {
        /**
         * TODO: Set the id of the input car object to the `id` input.
         * TODO: Save the car using the `save` method from the Car service
         * TODO: Use the `assembler` on that updated car and return as part of the response.
         *   Update the first line as part of the above implementing.
         */
        Car existing = carService.findById(id); // check if id is valid
        car.setId(existing.getId());
        Car newCar = carService.save(car);
        EntityModel<Car> resource = assembler.toModel(newCar);
        return ResponseEntity.ok(resource);
    }

    /**
     * Removes a vehicle from the system.
     * @param id The ID number of the vehicle to remove.
     * @return response that the related vehicle is no longer in the system
     */
    @ApiResponses(value = {
        @ApiResponse(responseCode = NOT_FOUND, description = "A record could not be found matching the request, please verify the request parameters."),
    })
    @DeleteMapping(CARS_DELETE_BY_ID_URL)
    ResponseEntity<?> delete(@PathVariable Long id) {
        /**
         * TODO: Use the Car Service to delete the requested vehicle.
         */
        Car existing = carService.findById(id); // check if id is valid

        ResponseEntity<?> responseEntity;
        Car deleted = carService.delete(id);
        if (deleted.equals(Car.EMPTY)) {
            responseEntity = ResponseEntity.badRequest().build();
        } else {
            EntityModel<Car> resource = assembler.toModel(existing);
            responseEntity = ResponseEntity.ok(resource);
        }
        return responseEntity;
    }
}
