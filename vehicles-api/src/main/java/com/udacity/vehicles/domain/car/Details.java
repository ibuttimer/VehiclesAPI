package com.udacity.vehicles.domain.car;

import com.udacity.vehicles.domain.IValid;
import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

import static com.udacity.vehicles.service.CarService.UNKNOWN_BODY;
import static com.udacity.vehicles.service.CarService.UNKNOWN_MODEL;

/**
 * Declares the additional detail variables for each Car object,
 * along with related methods for access and setting.
 */
@Embeddable
public class Details implements IValid<Details> {

    @NotBlank(message = "Body is mandatory")
    private String body;

    @NotBlank(message = "Model is mandatory")
    private String model;

    @NotNull(message = "Manufacturer is mandatory")
    @ManyToOne
    private Manufacturer manufacturer;

    private Integer numberOfDoors;

    private String fuelType;

    private String engine;

    private Integer mileage;

    private Integer modelYear;

    private Integer productionYear;

    private String externalColor;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getNumberOfDoors() {
        return numberOfDoors;
    }

    public void setNumberOfDoors(Integer numberOfDoors) {
        this.numberOfDoors = numberOfDoors;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    public Integer getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(Integer productionYear) {
        this.productionYear = productionYear;
    }

    public String getExternalColor() {
        return externalColor;
    }

    public void setExternalColor(String externalColor) {
        this.externalColor = externalColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Details details = (Details) o;
        return Objects.equals(body, details.body) && Objects.equals(model, details.model) && Objects.equals(manufacturer, details.manufacturer) && Objects.equals(numberOfDoors, details.numberOfDoors) && Objects.equals(fuelType, details.fuelType) && Objects.equals(engine, details.engine) && Objects.equals(mileage, details.mileage) && Objects.equals(modelYear, details.modelYear) && Objects.equals(productionYear, details.productionYear) && Objects.equals(externalColor, details.externalColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, model, manufacturer, numberOfDoors, fuelType, engine, mileage, modelYear, productionYear, externalColor);
    }

    @Override
    public String toString() {
        return "Details{" +
                "body='" + body + '\'' +
                ", model='" + model + '\'' +
                ", manufacturer=" + manufacturer +
                ", numberOfDoors=" + numberOfDoors +
                ", fuelType='" + fuelType + '\'' +
                ", engine='" + engine + '\'' +
                ", mileage=" + mileage +
                ", modelYear=" + modelYear +
                ", productionYear=" + productionYear +
                ", externalColor='" + externalColor + '\'' +
                '}';
    }

    @Override
    public Details ensureValid() {
        if (StringUtils.isEmpty(body)) {
            body = UNKNOWN_BODY;
        }
        if (StringUtils.isEmpty(model)) {
            model = UNKNOWN_MODEL;
        }
        if (manufacturer == null) {
            manufacturer = new Manufacturer();
        }
        manufacturer.ensureValid();
        return this;
    }
}
