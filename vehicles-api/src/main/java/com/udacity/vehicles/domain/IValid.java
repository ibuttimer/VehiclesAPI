package com.udacity.vehicles.domain;

public interface IValid<T> {
    T ensureValid();
}
