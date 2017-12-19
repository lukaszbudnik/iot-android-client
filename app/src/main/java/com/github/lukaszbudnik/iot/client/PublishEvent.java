package com.github.lukaszbudnik.iot.client;

import java.util.Date;

/**
 * Created by lukasz on 22/08/2016.
 */
public class PublishEvent {

    private final Date time;
    private final Integer eventsPublished;

    public PublishEvent(Date time, Integer eventsPublished) {
        this.time = time;
        this.eventsPublished = eventsPublished;
    }

    public Date getTime() {
        return time;
    }

    public Integer getEventsPublished() {
        return eventsPublished;
    }
}
