package com.example.pseudopong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

public class GameView extends View {

    private boolean mRunning;
    private Thread mUpdateThread = null;

    private Paddle mPaddle;
    private Ball mBall;
    private int mViewWidth, mViewHeight;
    private final Paint mPaintBall;
    private final SensorHandler mSensorHandler = new SensorHandler();
    private boolean didSetSize = false;
    private Bitmap mBitmap;
    private final Matrix mMatrix;

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaintBall = new Paint();
        mPaintBall.setColor(
                ResourcesCompat.getColor(
                        getResources(),
                        R.color.ball_color,
                        getContext().getTheme()
                )
        );
        mPaintBall.setAntiAlias(true);

        mMatrix = new Matrix();
    }

    private static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void resume() {
        mRunning = true;

        mUpdateThread = new Thread(this::updatePositions);
        mUpdateThread.start();

        mBitmap = drawableToBitmap(
                ResourcesCompat.getDrawable(
                        getResources(),
                        R.drawable.paddle_drawable,
                        getContext().getTheme()
                )
        );
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mViewWidth = w;
        mViewHeight = h;

        mPaddle = new Paddle(mViewWidth, mViewHeight, getResources());
        mBall = new Ball(mViewWidth, mViewHeight);

        mBall.setPaddle(mPaddle);
        mBall.setContactSoundEffectHandler(this::contactSoundEffectHandler);
        didSetSize = true;
    }

    public void pause() {
        mRunning = false;
        try {
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
            while (!didSetSize)
                Thread.sleep(1000);

            Thread.sleep(500);
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

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        canvas.drawColor(Color.WHITE);
        canvas.drawCircle(
                (float) mBall.getX(), (float) mBall.getY(),
                (float) mBall.getRadius(), mPaintBall);

        /*
        // Draw paddle as a simple rect instead of bitmap:
        canvas.rotate((float) (180. / Math.PI * mPaddle.getRot()), mPaddle.getCenterX(), mPaddle.getCenterY());
        canvas.drawRoundRect(mPaddle.getRectF(), 10f, 10f, mPaintPaddle);
        */

        mMatrix.reset();
        mMatrix.postTranslate((float) mPaddle.getX(), (float) mPaddle.getY());
        mMatrix.postRotate((float) (180. / Math.PI * mPaddle.getRot()), mPaddle.getCenterX(), mPaddle.getCenterY());
        canvas.drawBitmap(mBitmap, mMatrix, null);

        canvas.restore();
        invalidate();
    }

    public void contactSoundEffectHandler() {
        Vibrator vibe = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(VibrationEffect.createOneShot(10, 128));
    }

    public SensorHandler getSensorHandler() {
        return mSensorHandler;
    }
}