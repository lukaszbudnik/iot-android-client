package com.github.lukaszbudnik.iot.client;

import java.util.Date;

/**
 * Created by lukasz on 22/08/2016.
 */
public class QueueEvent {
    private final Date time;
    private final Integer size;

    public QueueEvent(Date time, Integer size) {
        this.time = time;
        this.size = size;
    }

    public Date getTime() {
        return time;
    }

    public Integer getSize() {
        return size;
    }
}
