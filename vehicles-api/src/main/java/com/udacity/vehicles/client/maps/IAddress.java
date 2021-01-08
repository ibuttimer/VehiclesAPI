package com.udacity.vehicles.client.maps;

public interface IAddress {

    String getAddress();

    void setAddress(String address);

    String getCity();

    void setCity(String city);

    String getState();

    void setState(String state);

    String getZip();

    void setZip(String zip);

    default void setFrom(IAddress address) {
        setAddress(address.getAddress());
        setCity(address.getCity());
        setState(address.getState());
        setZip(address.getZip());
    }
}
