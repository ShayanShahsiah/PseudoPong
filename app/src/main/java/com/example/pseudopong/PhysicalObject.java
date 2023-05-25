package com.example.pseudopong;

import android.util.Pair;

import java.util.function.DoubleSupplier;

class SingleAxis {
    private final double mPos0;
    private double mPos;
    private double mVel;
    private DoubleSupplier mCalcAccel;
    private double mPosToBe;
    private double mVelToBe;
    private double mBoundLow = Double.NEGATIVE_INFINITY;
    private double mBoundHigh = Double.POSITIVE_INFINITY;
    private double mReflectivity = 0.;

    SingleAxis(double pos0) {
        mCalcAccel = () -> 0.;
        mPos0 = pos0;

        reset();
    }

    public void reset() {
        mPos = mPos0;
        mVel = 0.;

        mPosToBe = Double.NaN;
        mVelToBe = Double.NaN;
    }

    private boolean withinBounds(double newPos) {
        return newPos > mBoundLow && newPos < mBoundHigh;
    }

    public final void updatePosWithTimeStep(double dt) {
        assert dt >= 0.;
        dt = Math.max(dt, 1e-7); // To deal with dt==0

        double accelFromAccel = mCalcAccel.getAsDouble();
        double accelFromVel = Double.isNaN(mVelToBe) ? 0. : (mVelToBe - mVel) / dt;
        double accel = accelFromAccel + accelFromVel;

        double dx = .5 * accel * dt * dt + mVel * dt;

        if (withinBounds(mPos + dx)) {
            mPosToBe = mPos + dx;
            mVelToBe = mVel + accel * dt;
        }
        else {
            mPosToBe = mPos;
            mVelToBe = -mVel * mReflectivity;
        }
    }

    public void setVelToBe(double velocity) {
        mVelToBe = velocity;
    }

    public void setPosToBe(double posToBe) {
        mPosToBe = posToBe;
    }

    public void setAccel(DoubleSupplier dynamicAccel) {
        mCalcAccel = dynamicAccel;
    }

    public void setBounds(double boundLow, double boundHigh, double reflectivity) {
        mBoundLow = boundLow;
        mBoundHigh = boundHigh;
        mReflectivity = reflectivity;
    }

    public void confirm() {
        mPos = mPosToBe;
        mVel = mVelToBe;

        mPosToBe = Double.NaN;
        mVelToBe = Double.NaN;
    }

    public double getPos() {
        return mPos;
    }

    public double getVel() {
        return mVel;
    }

    public double getPosToBe() {
        return mPosToBe;
    }

    public double getVelToBe() {
        return mVelToBe;
    }
}

class RelativePositioning {
    public double distOrthogonal = Double.NaN;
    public double distParallel = Double.NaN;
}

public class PhysicalObject {
    private SingleAxis mAxisX;
    private SingleAxis mAxisY;

    protected void reset() {
        mAxisX.reset();
        mAxisY.reset();
    }

    protected void updatePosWithTimeStep(double dt) {
        mAxisX.updatePosWithTimeStep(dt);
        mAxisY.updatePosWithTimeStep(dt);

        if (reflectionHandler()) {
            mAxisX.confirm();
            mAxisY.confirm();
            mAxisX.updatePosWithTimeStep(dt);
            mAxisY.updatePosWithTimeStep(dt);
        }

        mAxisX.confirm();
        mAxisY.confirm();
    }

    protected boolean reflectionHandler() {
        return false;
    }

    public void setAxisX(SingleAxis axisX) {
        mAxisX = axisX;
    }

    public void setAxisY(SingleAxis axisY) {
        mAxisY = axisY;
    }

    public void setVelToBeX(double velToBeX) {
        mAxisX.setVelToBe(velToBeX);
    }

    public void setVelToBeY(double velToBeY) {
        mAxisY.setVelToBe(velToBeY);
    }

    public void setAccelX(DoubleSupplier accelX) {
        mAxisX.setAccel(accelX);
    }

    public void setAccelY(DoubleSupplier accelY) {
        mAxisY.setAccel(accelY);
    }

    public void setBoundsX(double boundLow, double boundHigh, double reflectivity) {
        mAxisX.setBounds(boundLow, boundHigh, reflectivity);
    }

    public void setBoundsY(double boundLow, double boundHigh, double reflectivity) {
        mAxisY.setBounds(boundLow, boundHigh, reflectivity);
    }

    public void setPosToBeX(double posToBeX) {
        mAxisX.setPosToBe(posToBeX);
    }

    public void setPosToBeY(double posToBeY) {
        mAxisY.setPosToBe(posToBeY);
    }

    public double getPosToBeX() {
        return mAxisX.getPosToBe();
    }

    public double getPosToBeY() {
        return mAxisY.getPosToBe();
    }

    public double getVelToBeX() {
        return mAxisX.getVelToBe();
    }

    public double getVelToBeY() {
        return mAxisY.getVelToBe();
    }

    public double getX() {
        return mAxisX.getPos();
    }

    public double getY() {
        return mAxisY.getPos();
    }

    public double getVelX() {
        return mAxisX.getVel();
    }

    public double getVelY() {
        return mAxisY.getVel();
    }
}
