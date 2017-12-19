package com.github.lukaszbudnik.iot.client;

import android.os.Build;
import android.util.Log;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by lukasz on 20/08/2016.
 */
public class Publisher {

    private static final int QUEUE_SIZE = 1000;
    private static final int BATCH_SIZE = 100;

    private final DateFormat dateFormat;
    // ScheduledExecutorService does not work on Android
    private final Timer timer = new Timer();
    private final OkHttpClient client = new OkHttpClient();
    private final Map<String, CircularFifoQueue<NumericEventData>> queues = Maps.newConcurrentMap();

    public Publisher() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendData();
            }
        }, 10 * 1000, 10 * 1000);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Subscribe
    public void onEventData(NumericEventData event) {
        int size = 0;
        if (queues.containsKey(event.getName())) {
            queues.get(event.getName()).offer(event);
            size = queues.get(event.getName()).size();
        } else {
            CircularFifoQueue<NumericEventData> queue = new CircularFifoQueue<>(QUEUE_SIZE);
            queue.offer(event);
            queues.put(event.getName(), queue);
        }
        EventBus.getDefault().post(new QueueEvent(new Date(), size + 1));
    }

    public void sendData() {

        if (queues.size() == 0) {
            return;
        }

        boolean isEmulator = Build.PRODUCT.matches(".*_?sdk_?.*");

        for (String name: queues.keySet()) {

            CircularFifoQueue<NumericEventData> queue = queues.get(name);

            Log.i("Publisher", String.format("About to publish %d %s events to remote server", queue.size(), name));

            while (!queue.isEmpty()) {

                FormBody.Builder formBodyBuilder = new FormBody.Builder();
                List<NumericEventData> processedEvents = new ArrayList<>(BATCH_SIZE);
                // in batches of max BATCH_SIZE items
                for (int i = 0; i < BATCH_SIZE && i < queue.size(); i++) {
                    // get the head, removed only when successfully processed
                    NumericEventData event = queue.get(i);
                    processedEvents.add(event);

                    String v = event.getValue().toString();
                    String t = dateFormat.format(event.getDateTime());

                    formBodyBuilder
                            .add("v", v)
                            .add("t", t);
                }

                FormBody formBody = formBodyBuilder.build();

                // for the backend application see:
                // https://github.com/lukaszbudnik/iot-heroku-server
                String url;

                if (isEmulator) {
                    url = "http://192.168.1.102:5000";
                } else {
                    url = "https://XXX.herokuapp.com";
                }

                Request request = new Request.Builder()
                        .url(url + "/v1/telemetry/YYY/" + name)
                        .post(formBody)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    Log.i("Publisher", response.body().string());
                    queue.removeAll(processedEvents);
                    EventBus.getDefault().post(new PublishEvent(new Date(), processedEvents.size()));
                    EventBus.getDefault().post(new QueueEvent(new Date(), queue.size()));
                } catch (Throwable tr) {
                    Log.e("Publisher", Log.getStackTraceString(tr));
                    break;
                }

            }
        }
    }

}
