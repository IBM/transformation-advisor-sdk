/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.ta.sdk.spi.recommendation;

import com.google.gson.annotations.Expose;

import java.util.Arrays;
import java.util.List;

public class ModDimension<T> {
    @Expose
    private String name;
    @Expose
    private List<T> values;
    @Expose
    private T defaultValue;

    public ModDimension(String name, T value) {
        this(name, Arrays.asList(value));
    }

    public ModDimension(String name, List<T> values) {
        this(name, values, null);
    }

    public ModDimension(String name, List<T> values, T defaultValue) {
        this.name = name;
        this.values = values;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<T> getValues() {
        return values;
    }

    public void setValues(List<T> values) {
        this.values = values;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

}
