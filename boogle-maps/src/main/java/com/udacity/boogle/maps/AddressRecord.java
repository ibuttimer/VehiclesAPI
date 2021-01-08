package com.udacity.boogle.maps;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Entity
public class AddressRecord {

    public static final double MIN_LATITUDE = -90;
    public static final double MAX_LATITUDE = 90;
    public static final double MIN_LONGITUDE = -180;
    public static final double MAX_LONGITUDE = 180;

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private Double lat;

    @NotNull
    private Double lon;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinTable(name = "address_vehicle",
        joinColumns = {
            @JoinColumn(name = "addressRecordId", referencedColumnName = "id") },
        inverseJoinColumns = {
            @JoinColumn(name = "vehicleId", referencedColumnName = "id") })
    private Vehicle vehicle;

    @Valid
    @Embedded
    private Address address;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public static boolean latitudeIsValid(Double lat) {
        return (lat != null) && (lat >= MIN_LATITUDE) && (lat <= MAX_LATITUDE);
    }

    public static boolean longitudeIsValid(Double lon) {
        return (lon != null) && (lon >= MIN_LONGITUDE) && (lon <= MAX_LONGITUDE);
    }
}
