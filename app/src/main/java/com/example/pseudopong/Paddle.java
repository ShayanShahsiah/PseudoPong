package com.example.pseudopong;

import android.graphics.RectF;

public class Paddle extends PhysicalObject {
    private final double K_HIGH, K_LOW;
    private final int mWidth, mHeight;
    private double mRot;
    private boolean mBallStuck;
    final int mViewWidth, mViewHeight;

    public Paddle(int viewWidth, int viewHeight) {
        mViewWidth = viewWidth;
        mViewHeight = viewHeight;

        mWidth = viewWidth/3;
        mHeight = mWidth/10;

        double x0 = .5 * viewWidth - .5 * mWidth;
        double y0 = .85 * viewHeight;

        K_LOW = .25 * viewHeight;
        K_HIGH = .75 * viewHeight;

        setAxisX(new SingleAxis(x0));
        setAxisY(new SingleAxis(y0));

        setBoundsX(0., viewWidth - mWidth, 0.);
        setBoundsY(.25*viewHeight, viewHeight - .5*mWidth, 0.);

        setAccelY(() -> {
            double y = getY();
            double k = y < y0 ? K_LOW : K_HIGH;
            return -k * (y - y0);
        });

        reset();
    }

    @Override
    public void reset() {
        super.reset();

        mRot = 0.;
        mBallStuck = false;
    }

    public void setRot(double newRot) {
        mRot = newRot;
    }

    public double getRot() {
        return mRot;
    }

    public RectF getRectF() {
        float x = (float) getX();
        float y = (float) getY();
        return new RectF(x, y, x + mWidth, y + mHeight);
    }

    public int getCenterX() {
        return (int) (getX() + .5*mWidth);
    }

    public int getCenterY() {
        return (int) (getY() + .5*mHeight);
    }

    public boolean isBallStuck() {
        return mBallStuck;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}
