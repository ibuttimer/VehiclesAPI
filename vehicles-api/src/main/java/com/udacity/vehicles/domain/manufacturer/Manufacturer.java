package com.udacity.vehicles.domain.manufacturer;

import com.udacity.vehicles.domain.IValid;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

import static com.udacity.vehicles.service.ManufacturerService.UNKNOWN_MANUFACTURER_ID;
import static com.udacity.vehicles.service.ManufacturerService.UNKNOWN_MANUFACTURER_NAME;

/**
 * Declares class to hold car manufacturer information.
 */
@Entity
public class Manufacturer implements IValid<Manufacturer> {

    @Id
    @NotNull(message = "Code is mandatory")
    private Integer code;
    @NotBlank(message = "Name is mandatory")
    private String name;

    public Manufacturer() { }

    public Manufacturer(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static Manufacturer of(Integer code, String name) {
        return new Manufacturer(code, name);
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Manufacturer that = (Manufacturer) o;
        return Objects.equals(code, that.code) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }

    @Override
    public String toString() {
        return "Manufacturer{" +
                "code=" + code +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public Manufacturer ensureValid() {
        if (code == null) {
            code = UNKNOWN_MANUFACTURER_ID;
        }
        if (StringUtils.isEmpty(name)) {
            name = UNKNOWN_MANUFACTURER_NAME;
        }
        return this;
    }
}
