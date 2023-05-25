package com.example.pseudopong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView {

    private boolean mRunning;
    private Thread mGameThread = null;
    private Thread mUpdateThread = null;

    private Paddle mPaddle;
    private Ball mBall;
    private int mViewWidth, mViewHeight;
    private final SurfaceHolder mSurfaceHolder;
    private final Paint mPaint;
    private final SensorHandler mSensorHandler = new SensorHandler();

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSurfaceHolder = getHolder();
        mPaint = new Paint();
        mPaint.setColor(Color.DKGRAY);
    }

    public void resume() {
        mRunning = true;
        
        mGameThread = new Thread(this::updateUI);
        mGameThread.start();

        mUpdateThread = new Thread(this::updatePositions);
        mUpdateThread.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.e("onSizeChanged", "Size changed!");
        super.onSizeChanged(w, h, oldw, oldh);

        mViewWidth = w;
        mViewHeight = h;

        mPaddle = new Paddle(mViewWidth, mViewHeight);
        mBall = new Ball(mViewWidth, mViewHeight);

        mBall.setPaddle(mPaddle);
        mBall.setContactSoundEffectHandler(this::contactSoundEffectHandler);
    }

    public void pause() {
        mRunning = false;
        try {
            mGameThread.join();
            mUpdateThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        mBall.reset();
        mPaddle.reset();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pause();
            resume();
        }
        return true;
    }

    /**
     * Runs in a separate thread.
     * Updates positions of Ball and Paddle.
     */
    private void updatePositions() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        mSensorHandler.reset();

        long tick = 0L;
        while (mRunning) {
            long tock = System.nanoTime();
            double dt = tick == 0L ? 0. : (tock - tick) * 1e-9;
            tick = tock;

            mPaddle.setVelToBeX(mViewWidth / (Math.PI/4) * mSensorHandler.getOmega(1));
            mPaddle.setVelToBeY(-.75 * mViewHeight * mSensorHandler.getAccelY());
            mPaddle.setRot(-mSensorHandler.getAngle(2));
            mPaddle.updatePosWithTimeStep(dt);

            mBall.updatePosWithTimeStep(dt);

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Runs in a separate thread.
     * All drawing happens here.
     */
    private void updateUI() {
        Canvas canvas;

        while (mRunning) {
            invalidate();

            if (mSurfaceHolder.getSurface().isValid()) {
                canvas = mSurfaceHolder.lockHardwareCanvas();
                canvas.save();
                
                canvas.drawColor(Color.WHITE);
                canvas.drawCircle(
                        (float) mBall.getX(), (float) mBall.getY(),
                        (float) mBall.getRadius(), mPaint);
                canvas.rotate((float) (180. / Math.PI * mPaddle.getRot()), mPaddle.getCenterX(), mPaddle.getCenterY());
                canvas.drawRoundRect(mPaddle.getRectF(), 10f, 10f, mPaint);

                canvas.restore();
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void contactSoundEffectHandler() {
        Vibrator vibe = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(VibrationEffect.createOneShot(10, 128));
    }

    public SensorHandler getSensorHandler() {
        return mSensorHandler;
    }
}