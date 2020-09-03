package org.schabi.newpipe.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.schabi.newpipe.classificator.HARClassifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UserContextService extends Service implements SensorEventListener {
    private static final String TAG = UserContextService.class.getSimpleName();

    private final IBinder serviceBinder = new RunServiceBinder();

    private static final int N_SAMPLES = 100;
    private static int prevIdx = -1;

    private static List<Float> ax;
    private static List<Float> ay;
    private static List<Float> az;

    private static List<Float> lx;
    private static List<Float> ly;
    private static List<Float> lz;

    private static List<Float> gx;
    private static List<Float> gy;
    private static List<Float> gz;

    private static List<Float> ma;
    private static List<Float> ml;
    private static List<Float> mg;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mLinearAcceleration;

    private HARClassifier classifier;
    private float[] results;
    private String[] labels = {"Biking", "Downstairs", "Jogging", "Sitting", "Standing", "Upstairs", "Walking"};

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Creating service");

        ax = new ArrayList<>(); ay = new ArrayList<>(); az = new ArrayList<>();
        lx = new ArrayList<>(); ly = new ArrayList<>(); lz = new ArrayList<>();
        gx = new ArrayList<>(); gy = new ArrayList<>(); gz = new ArrayList<>();
        ma = new ArrayList<>(); ml = new ArrayList<>(); mg = new ArrayList<>();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);

        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mLinearAcceleration , SensorManager.SENSOR_DELAY_FASTEST);

        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mGyroscope , SensorManager.SENSOR_DELAY_FASTEST);

        classifier = new HARClassifier(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting service");

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (results == null || results.length == 0) {
                    return;
                }
                float max = -1;
                int idx = -1;
                for (int i = 0; i < results.length; i++) {
                    if (results[i] > max) {
                        idx = i;
                        max = results[i];
                    }
                }

                if (max > 0.50 && idx != prevIdx) {
                    Log.d(TAG, "User state: " + labels[idx]);
                    prevIdx = idx;
                }
            }
        }, 1000, 3000);

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Binding service");

        return this.serviceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "Destroying service");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        activityPrediction();

        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ax.add(event.values[0]);
            ay.add(event.values[1]);
            az.add(event.values[2]);
        }
        else if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            lx.add(event.values[0]);
            ly.add(event.values[1]);
            lz.add(event.values[2]);
        }
        else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gx.add(event.values[0]);
            gy.add(event.values[1]);
            gz.add(event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void activityPrediction() {
        List<Float> data = new ArrayList<>();

        if (ax.size() >= N_SAMPLES && ay.size() >= N_SAMPLES && az.size() >= N_SAMPLES
                && lx.size() >= N_SAMPLES && ly.size() >= N_SAMPLES && lz.size() >= N_SAMPLES
                && gx.size() >= N_SAMPLES && gy.size() >= N_SAMPLES && gz.size() >= N_SAMPLES
        ) {
            double maValue, mgValue, mlValue;

            for( int i = 0; i < N_SAMPLES ; i++ ) {
                maValue = Math.sqrt(Math.pow(ax.get(i), 2) + Math.pow(ay.get(i), 2) + Math.pow(az.get(i), 2));
                mlValue = Math.sqrt(Math.pow(lx.get(i), 2) + Math.pow(ly.get(i), 2) + Math.pow(lz.get(i), 2));
                mgValue = Math.sqrt(Math.pow(gx.get(i), 2) + Math.pow(gy.get(i), 2) + Math.pow(gz.get(i), 2));

                ma.add((float)maValue);
                ml.add((float)mlValue);
                mg.add((float)mgValue);
            }

            data.addAll(ax.subList(0, N_SAMPLES));
            data.addAll(ay.subList(0, N_SAMPLES));
            data.addAll(az.subList(0, N_SAMPLES));

            data.addAll(lx.subList(0, N_SAMPLES));
            data.addAll(ly.subList(0, N_SAMPLES));
            data.addAll(lz.subList(0, N_SAMPLES));

            data.addAll(gx.subList(0, N_SAMPLES));
            data.addAll(gy.subList(0, N_SAMPLES));
            data.addAll(gz.subList(0, N_SAMPLES));

            data.addAll(ma.subList(0, N_SAMPLES));
            data.addAll(ml.subList(0, N_SAMPLES));
            data.addAll(mg.subList(0, N_SAMPLES));

            results = classifier.predictProbabilities(toFloatArray(data));

            float max = -1;
            int idx = -1;
            for (int i = 0; i < results.length; i++) {
                if (results[i] > max) {
                    idx = i;
                    max = results[i];
                }
            }

            ax.clear(); ay.clear(); az.clear();
            lx.clear(); ly.clear(); lz.clear();
            gx.clear(); gy.clear(); gz.clear();
            ma.clear(); ml.clear(); mg.clear();
        }
    }

    private float[] toFloatArray(List<Float> list) {
        int i = 0;
        float[] array = new float[list.size()];

        for (Float f : list) {
            array[i++] = (f != null ? f : Float.NaN);
        }
        return array;
    }

    public class RunServiceBinder extends Binder {
        public UserContextService getService() {
            return UserContextService.this;
        }
    }

}
