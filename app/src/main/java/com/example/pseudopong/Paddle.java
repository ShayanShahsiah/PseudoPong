package com.example.pseudopong;

import android.content.res.Resources;
import android.graphics.RectF;

public class Paddle extends PhysicalObject {
    private final double K_HIGH, K_LOW;
    private final int mWidth, mHeight;
    private double mRot;
    private boolean mBallStuck;
    private final int mViewWidth, mViewHeight;

    public Paddle(int viewWidth, int viewHeight, Resources res) {
        mViewWidth = viewWidth;
        mViewHeight = viewHeight;

        mWidth = res.getDimensionPixelSize(R.dimen.paddle_width);//viewWidth/3;
        mHeight = res.getDimensionPixelSize(R.dimen.paddle_height);//viewWidth/10;

        double x0 = .5 * viewWidth - .5 * mWidth;
        double y0 = .82 * viewHeight;

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

    @Override
    public void setVelToBeY(double velToBeY) {
        double boundHigh = 3. * mViewHeight;
        double boundLow = -3. * mViewHeight;
        velToBeY = Math.max(Math.min(velToBeY, boundHigh), boundLow);
        super.setVelToBeY(velToBeY);
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

    public int getRefX() {
        return (int) (getCenterX() + .45*mHeight * Math.sin(mRot));
    }

    public int getRefY() {
        return (int) (getCenterY() - .45*mHeight * Math.cos(mRot));
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
