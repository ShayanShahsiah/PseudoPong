package com.example.pseudopong;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class SensorHandler implements SensorEventListener {
    private double[] mOrientation, mOrientation0;
    private double[] mOmega;
    private double mAccelX, mAccelY;
    private long mTickGyro;

    {
        reset();
    }

    public void reset() {
        mOrientation = new double[]{0., 0., 0.};
        mOrientation0 = new double[]{0., 0., 0.};
        mOmega = new double[]{0., 0., 0.};
        mAccelX = 0.;
        mAccelY = 0.;
        mTickGyro = 0L;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GYROSCOPE: {
                long tock = event.timestamp;
                double dt = mTickGyro == 0L ? 0. : (tock - mTickGyro) * 1e-9;
                mTickGyro = tock;

                mOmega[0] = event.values[0];
                mOmega[1] = event.values[1];
                mOmega[2] = event.values[2];
                mOrientation[0] += mOmega[0] * dt;
                mOrientation[1] += mOmega[1] * dt;
                mOrientation[2] += mOmega[2] * dt;
                break;
            }
            case Sensor.TYPE_LINEAR_ACCELERATION: {
                mAccelX = event.values[0];
                mAccelY = event.values[1];
                break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public double getAngle(int axis) {
        return mOrientation[axis] - mOrientation0[axis];
    }

    public double getOmega(int axis) {
        return mOmega[axis];
    }

    public double getAccelX() {
        return mAccelX;
    }
    public double getAccelY() {
        return mAccelY;
    }
}
