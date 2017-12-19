package com.github.lukaszbudnik.iot.client;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

/**
 * Created by lukasz on 20/08/2016.
 */
public class StepCountListener implements SensorEventListener {

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        EventBus.getDefault().post(new NumericEventData("steps", (int)sensorEvent.values[0], new Date()));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.i(sensor.getName(), "accuracy: " + i);
    }


}
