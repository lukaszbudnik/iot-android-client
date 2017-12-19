package com.github.lukaszbudnik.iot.client;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by lukasz on 20/08/2016.
 */
public class NumericEventData<T extends Number> {
    private final String name;
    private final T value;
    private final Date dateTime;

    public NumericEventData(@NonNull String name, @NonNull T value, @NonNull Date dateTime) {
        this.name = name;
        this.value = value;
        this.dateTime = dateTime;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public Date getDateTime() {
        return dateTime;
    }
}
