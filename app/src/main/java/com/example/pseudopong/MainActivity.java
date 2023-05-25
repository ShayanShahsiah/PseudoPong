package com.example.pseudopong;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private SensorManager mSensorManager;
    private Sensor mGyroscopeSensor;
    private Sensor mLinearAccelerationSensor;
    private GameView mGameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mLinearAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mGameView = new GameView(this);
        setContentView(mGameView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGameView.resume();
        mSensorManager.registerListener(mGameView.getSensorHandler(), mGyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mGameView.getSensorHandler(), mLinearAccelerationSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGameView.pause();
        mSensorManager.unregisterListener(mGameView.getSensorHandler());
    }
}