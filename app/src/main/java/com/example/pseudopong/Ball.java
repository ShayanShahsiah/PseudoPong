package com.example.pseudopong;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.content.res.Resources;

class RelativePositioning {
    double distOrthogonal = Double.NaN;
    double distParallel = Double.NaN;
}

public class Ball extends PhysicalObject {
    private final double GRAV;
    private final double mRadius;
    private Runnable mContactSoundEffectHandler;
    private final int mViewWidth, mViewHeight;
    private Paddle mPaddle;

    public Ball(int viewWidth, int viewHeight, Resources res) {
        mViewWidth = viewWidth;
        mViewHeight = viewHeight;

        mRadius = res.getDimensionPixelOffset(R.dimen.ball_radius);

        double x0 = viewWidth/2.;
        double y0 = viewHeight/3.;

        setAxisX(new SingleAxis(x0));
        setAxisY(new SingleAxis(y0));

        setBoundsX(mRadius, viewWidth - mRadius, .85);
        setBoundsY(mRadius, Double.POSITIVE_INFINITY, .85);

        GRAV = 9.8 * .9*viewWidth;
        setAccelY(() -> GRAV);

        reset();
    }

    @Override
    public void reset() {
        super.reset();
    }

    public RelativePositioning calcRelPositioning(double x, double y) {
        RelativePositioning res = new RelativePositioning();
        double dx = x - mPaddle.getRefX();
        double dy = y - mPaddle.getRefY();

        double a = mPaddle.getRot();

        double dotProd = dx * cos(a) + dy * sin(a);
        double crossProd = dx * sin(a) - dy * cos(a);

        res.distOrthogonal = crossProd;
        res.distParallel = crossProd > 0. ? dotProd : -dotProd;

        return res;
    }

    @Override
    protected boolean doReflect() {
        RelativePositioning rp = calcRelPositioning(getPosToBeX(), getPosToBeY());
        boolean touches = rp.distOrthogonal > 0. && rp.distOrthogonal < mRadius &&
                Math.abs(rp.distParallel) < .5 * mPaddle.getWidth();
        if (touches) {
            double a = mPaddle.getRot();
            double v1x = getVelToBeX();
            double v1y = getVelToBeY();

            double v2x = .55 * mPaddle.getVelX();
            double v2y = .55 * mPaddle.getVelY();

            if (getVelToBeY() > mViewWidth)
                mContactSoundEffectHandler.run();

            double wannabeVelX = v1x * cos(2*a) + v1y * sin(2*a) + 2*v2x * sin(a)*sin(a) - v2y * sin(2*a);
            double wannabeVelY = v1x * sin(2*a) - v1y * cos(2*a) - v2x * sin(2*a) + 2*v2y * cos(a)*cos(a);
            double cor = .75;
            setVelToBeX( .5 * ((1.+cor) * wannabeVelX + (1.-cor) * v1x) );
            setVelToBeY( .5 * ((1.+cor) * wannabeVelY + (1.-cor) * v1y) );

            setPosToBeX(mPaddle.getRefX() + rp.distParallel * cos(a) + mRadius * sin(a));
            setPosToBeY(mPaddle.getRefY() + rp.distParallel * sin(a) - mRadius * cos(a));

            return true;

        }

        return false;

    }

    public void setContactSoundEffectHandler(Runnable contactSoundEffectHandler) {
        mContactSoundEffectHandler = contactSoundEffectHandler;
    }

    public void setPaddle(Paddle paddle) {
        mPaddle = paddle;
    }

    public double getRadius() {
        return mRadius;
    }
}
