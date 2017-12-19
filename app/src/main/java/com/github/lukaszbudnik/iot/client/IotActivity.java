package com.github.lukaszbudnik.iot.client;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.common.collect.FluentIterable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class IotActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_push_sensor_values);

        EventBus.getDefault().register(this);
        EventBus.getDefault().register(new Publisher());

        SensorManager mgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = mgr.getSensorList(Sensor.TYPE_ALL);

        // using Java 8 streams requires API 24, falling back to Guava
        int foundSensors = FluentIterable.from(sensors)
                .filter((s) -> s.getType() == Sensor.TYPE_STEP_COUNTER)
                .transform((s) -> mgr.registerListener(new StepCountListener(), s, SensorManager.SENSOR_DELAY_NORMAL))
                .size();

        Log.i("Sensors", String.format("Found sensors: %d", foundSensors));

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStepCountEvent(NumericEventData event) {
        TextView textView = (TextView) findViewById(R.id.step_counter_value);
        textView.setText(event.getValue().toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueueEvent(QueueEvent event) {
        TextView pendingEventsTextView = (TextView) findViewById(R.id.pending_events_value);
        pendingEventsTextView.setText(event.getSize().toString());

        TextView lastEventTextView = (TextView) findViewById(R.id.last_event_time_value);
        lastEventTextView.setText(event.getTime().toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPublishEvent(PublishEvent event) {
        TextView syncTimeTextView = (TextView) findViewById(R.id.last_sync_time_value);
        syncTimeTextView.setText(event.getTime().toString());

        TextView syncEventsTextView = (TextView) findViewById(R.id.last_sync_events_value);
        syncEventsTextView.setText(event.getEventsPublished().toString());
    }

}
