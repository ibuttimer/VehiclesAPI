package com.udacity.pricing.misc;

import java.util.Objects;
import java.util.ResourceBundle;

public interface ITestResource {

    default ResourceBundle getResourceBundle(String resource) {
        return ResourceBundle.getBundle(resource);
    }

    ResourceBundle getResourceBundle();

    default String getResourceString(String key) {
        return Objects.requireNonNull(getResourceBundle()).getString(key);
    }

    default int getResourceInt(String key) {
        return Integer.parseInt(getResourceString(key));
    }

}
