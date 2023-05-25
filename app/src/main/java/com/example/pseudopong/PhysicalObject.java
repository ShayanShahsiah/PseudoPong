package com.example.pseudopong;

import java.util.function.DoubleSupplier;

class SingleAxis {
    private final double mPos0;
    double pos;
    double vel;
    DoubleSupplier accel;
    double posToBe;
    double velToBe;
    double boundHigh = Double.POSITIVE_INFINITY;
    double boundLow = Double.NEGATIVE_INFINITY;
    double reflectivity = 0.;

    SingleAxis(double pos0) {
        accel = () -> 0.;
        mPos0 = pos0;

        reset();
    }

    public void reset() {
        pos = mPos0;
        vel = 0.;

        posToBe = Double.NaN;
        velToBe = Double.NaN;
    }

    public final void updatePosWithTimeStep(double dt) {
        assert dt >= 0.;
        dt = Math.max(dt, 1e-7); // To deal with dt==0

        double accelFromAccel = accel.getAsDouble();
        double accelFromVel = Double.isNaN(velToBe) ? 0. : (velToBe - vel) / dt;
        double accel = accelFromAccel + accelFromVel;

        double dx = .5 * accel * dt * dt + vel * dt;

        if (pos +dx > boundLow && pos +dx < boundHigh) {
            posToBe = pos + dx;
            velToBe = vel + accel * dt;
        }
        else {
            posToBe = pos;
            velToBe = -vel * reflectivity;
        }
    }

    public void confirm() {
        pos = posToBe;
        vel = velToBe;

        posToBe = Double.NaN;
        velToBe = Double.NaN;
    }
}

public class PhysicalObject {
    private SingleAxis mAxisX;
    private SingleAxis mAxisY;

    protected void reset() {
        mAxisX.reset();
        mAxisY.reset();
    }

    public final void updatePosWithTimeStep(double dt) {
        mAxisX.updatePosWithTimeStep(dt);
        mAxisY.updatePosWithTimeStep(dt);

        if (doReflect()) {
            mAxisX.confirm();
            mAxisY.confirm();
            mAxisX.updatePosWithTimeStep(dt);
            mAxisY.updatePosWithTimeStep(dt);
        }

        mAxisX.confirm();
        mAxisY.confirm();
    }

    protected boolean doReflect() {
        return false;
    }

    public void setAxisX(SingleAxis axisX) {
        mAxisX = axisX;
    }

    public void setAxisY(SingleAxis axisY) {
        mAxisY = axisY;
    }

    public void setPosToBeX(double posToBeX) {
        mAxisX.posToBe = posToBeX;
    }

    public void setPosToBeY(double posToBeY) {
        mAxisY.posToBe = posToBeY;
    }

    public void setVelToBeX(double velToBeX) {
        mAxisX.velToBe = velToBeX;
    }

    public void setVelToBeY(double velToBeY) {
        mAxisY.velToBe = velToBeY;
    }

    public void setAccelX(DoubleSupplier accelX) {
        mAxisX.accel = accelX;
    }

    public void setAccelY(DoubleSupplier accelY) {
        mAxisY.accel = accelY;
    }

    public void setBoundsX(double boundLow, double boundHigh, double reflectivity) {
        mAxisX.boundLow = boundLow;
        mAxisX.boundHigh = boundHigh;
        mAxisX.reflectivity = reflectivity;
    }

    public void setBoundsY(double boundLow, double boundHigh, double reflectivity) {
        mAxisY.boundLow = boundLow;
        mAxisY.boundHigh = boundHigh;
        mAxisY.reflectivity = reflectivity;
    }

    public double getPosToBeX() {
        return mAxisX.posToBe;
    }

    public double getPosToBeY() {
        return mAxisY.posToBe;
    }

    public double getVelToBeX() {
        return mAxisX.velToBe;
    }

    public double getVelToBeY() {
        return mAxisY.velToBe;
    }

    public double getX() {
        return mAxisX.pos;
    }

    public double getY() {
        return mAxisY.pos;
    }

    public double getVelX() {
        return mAxisX.vel;
    }

    public double getVelY() {
        return mAxisY.vel;
    }
}
