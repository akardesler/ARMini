/*
 * *
 *  * Created by Haydar Kardesler on 6.06.2022 10:33
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.hkardesler.armini.R;

/**
 * Created by rild on 2017/04/10.
 */

public class JoyStickSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private final int RESID_STICK_DEFAULT = R.drawable.s_joystick_stick;
    private final int RESID_BACKGROUND_DEFAULT = R.drawable.s_joystick_base;
    private final int RESID_SHADOW_DEFAULT = R.drawable.s_joystick_shadow;

    private final int RESID_SIG_UP_DEFAULT = R.drawable.s_signal_up;
    private final int RESID_SIG_RIGHT_DEFAULT = R.drawable.s_signal_right;
    private final int RESID_SIG_DOWN_DEFAULT = R.drawable.s_signal_down;
    private final int RESID_SIG_LEFT_DEFAULT = R.drawable.s_signal_left;

    private final boolean USE_SHADOW_DEFAULT = true;
    private final boolean USE_SIG_DEFAULT = true;

    private final int DENO_RATE_STICK_TALL_TO_SIZE = 25;
    private final int DENO_RATE_STICK_SIZE_TO_PAD = 2;
    private final int MARGIN_SHADOW = 32;
    private final int DENO_RATE_OFFSET_TO_PAD = 3;
    private final int DENO_RATE_MIN_DISTANCE_TO_PAD = 12;
    private final int ALPHA_PAD_DEFAULT = 150;
    private final int ALPHA_STICK_DEFAULT = 180;
    private final int ALPHA_SIGNAL_DEFAULT = 140;

    private int alphaStick = 200;
    private int alphaLayout = 200;
    private int alphaSignal = 200;
    private int offset = 0;

    private SurfaceHolder surfaceHolder;

    //    private Context mContext;
    private ViewGroup.LayoutParams params;
    private int stickTall;
    private int stickWidth, stickHeight;
    private int shadowWidth, shadowHeight;
    private int positionX = 0, positionY = 0, minDistance = 0;
    private float distance = 0, angle = 0;

    private JoyStickEntity jsEntity; // joy stick entity

    private Paint alphaSigPaint;
    private Paint alphaBacksPaint;
    private Paint alphaStickPaint;

    private Resources res;
    private Bitmap background;
    private Bitmap stick;
    private Bitmap shadow; // joy stick shadow
    private Bitmap signalUp;
    private Bitmap signalRight;
    private Bitmap signalDown;
    private Bitmap signalLeft;

    private boolean canUseShadow = USE_SHADOW_DEFAULT;
    private boolean canUseSignal = USE_SIG_DEFAULT;

    private JoyStick stickState = JoyStick.NONE;

    /**
     * ----- variables for event managing below here ---------
     */
    private OnChangeStateListener onChangeStateListener;

    /**
     * {@code hasFastLoop == false} method:
     * onJoyStickMoveListener.(float angle, float power, JoyStickSurfaceView.JoyStick state)
     * will loop only with loopInterval
     * <p>
     * {@code hasFastLoop == true} the method will loop with
     * two different interval.
     *
     * @param loopInterval
     * @param loopFastInterval
     * <p>
     * loop interval depends on
     * @param distance
     * <-- ignore -->
     *     minDistance
     *     <-- slow interval, weak signal -->
     *         midDistance = (params.width / 2) - offset
     *         <-- fast interval, strong signal -->
     */
    private final long LOOP_INTERVAL_DEFAULT = 800; // original 100 ms
    public final static long LOOP_INTERVAL_SLOW = 800;
    public final static long LOOP_INTERVAL_FAST = 100;
    private long loopInterval = LOOP_INTERVAL_DEFAULT;
    private long loopFastInterval = LOOP_INTERVAL_DEFAULT;
    private boolean hasFastLoop = false;
    private OnJoystickMoveListener onJoyStickMoveListener;
    private Thread threadJoyStickMove;

    private final int TIME_LONG_PUSH_EVENT_ACTIVATE = 1500;
    private Handler handlerOnLongPush = new Handler();
    private OnLongPushRunnable onLongPushed;


    public JoyStickSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        mContext = context;
        if (!isInEditMode()) setZOrderOnTop(true);

        initHolder();

        res = context.getResources();
        loadImages(res);

        initAlphaPaints();
        jsEntity = new JoyStickEntity();

        registerOnTouchEvent();
    }

    private void initHolder() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
    }

    private void initAlphaPaints() {
        alphaSigPaint = new Paint();
        alphaBacksPaint = new Paint();
        alphaStickPaint = new Paint();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        init();
        Canvas canvas = surfaceHolder.lockCanvas();
        drawBaseCanvas(canvas);
        drawStick(canvas);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * DEFALUT
     * pad (params) 504 // 126 * 4
     * stick size   220 // 55 * 4
     * shadow size  252 // 63 * 4
     * offset       180 // 45 * 4
     * min distance 40  // 10 * 4
     * pad alpha    150
     * stick alpha  180
     */
    private void init() {
        registerScreenSize();
        registerLayoutCenter(params.width, params.height);

        registerStickSize();
        stickTall = stickHeight / DENO_RATE_STICK_TALL_TO_SIZE; // make user feel sticky
        setStickSize(params.width / DENO_RATE_STICK_SIZE_TO_PAD, params.height / DENO_RATE_STICK_SIZE_TO_PAD);
        setShadowSize(params.width / DENO_RATE_STICK_SIZE_TO_PAD + MARGIN_SHADOW,
                params.height / DENO_RATE_STICK_SIZE_TO_PAD + MARGIN_SHADOW);
        setLayoutAlpha(ALPHA_PAD_DEFAULT);
        setStickAlpha(ALPHA_STICK_DEFAULT);
        setSignalAlpha(ALPHA_SIGNAL_DEFAULT);
        setOffset(params.width / DENO_RATE_OFFSET_TO_PAD);
        setMinimumDistance(params.width / DENO_RATE_MIN_DISTANCE_TO_PAD);

        resizeImages();
    }

    /**
     * this method should call after (or while) view creation
     *
     * getWidth(), getHeight() method return 0
     * in constructor JoyStickSurfaceView()
     */
    private void registerScreenSize() {
        params = new ViewGroup.LayoutParams(getWidth(), getHeight());
    }

    /**
     * this size register method should call
     * after bitmap images is loaded.
     */
    private void registerStickSize() {
        if (stick == null) return;
        stickWidth = stick.getWidth();
        stickHeight = stick.getHeight();
    }

    private void registerOnTouchEvent() {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                drawJoyStickWith(event);

                performPostLongPushEvent(event);

                if (stickState != JoyStick.NONE) {
                    handlerOnLongPush.removeCallbacks(onLongPushed);
                }

                if (event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_MOVE) {

                    if (distance > minDistance && jsEntity.isTouched()) {
                        performOnChangeState(judgeStateWith(angle, distance));
//                        performOnChangeState(judgeStateWith(angle));
                    } else if (distance <= minDistance && jsEntity.isTouched()) {
                        // STICK_NONE;
                        performReleaseJoyStick();
                    }

                } else {
                    handlerOnLongPush.removeCallbacks(onLongPushed);

                    performReleaseJoyStick();
                }
                return true;
            }
        });
    }

    private void performReleaseJoyStick() {
        setStickState(JoyStick.NONE);
        interruptJoyStickMoveThread();
        performOnJoyStickMove();
    }

    private void performPostLongPushEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            handlerOnLongPush.postDelayed(onLongPushed, TIME_LONG_PUSH_EVENT_ACTIVATE);
        }
    }

    private JoyStick judgeStateWith(float angle) {
        JoyStick state = JoyStick.NONE;
        if (angle >= 247.5 && angle < 292.5) {
            return JoyStick.UP;
        } else if (angle >= 292.5 && angle < 337.5) {
            return JoyStick.UPRIGHT;
        } else if (angle >= 337.5 || angle < 22.5) {
            return JoyStick.RIGHT;
        } else if (angle >= 22.5 && angle < 67.5) {
            return JoyStick.DOWNRIGHT;
        } else if (angle >= 67.5 && angle < 112.5) {
            return JoyStick.DOWN;
        } else if (angle >= 112.5 && angle < 157.5) {
            return JoyStick.DOWNLEFT;
        } else if (angle >= 157.5 && angle < 202.5) {
            return JoyStick.LEFT;
        } else if (angle >= 202.5 && angle < 247.5) {
            return JoyStick.UPLEFT;
        }
        return state;
    }

    private JoyStick judgeStateWith(float angle, float distance) {
        float midDistance = (params.width / 2) - offset;
        JoyStick state = JoyStick.NONE;
        if (angle >= 247.5 && angle < 292.5) {
            if (distance > midDistance) return JoyStick.MORE_UP;
            return JoyStick.UP;
        } else if (angle >= 292.5 && angle < 337.5) {
            if (distance > midDistance) return JoyStick.MORE_UPRIGHT;
            return JoyStick.UPRIGHT;
        } else if (angle >= 337.5 || angle < 22.5) {
            if (distance > midDistance) return JoyStick.MORE_RIGHT;
            return JoyStick.RIGHT;
        } else if (angle >= 22.5 && angle < 67.5) {
            if (distance > midDistance) return JoyStick.MORE_DOWNRIGHT;
            return JoyStick.DOWNRIGHT;
        } else if (angle >= 67.5 && angle < 112.5) {
            if (distance > midDistance) return JoyStick.MORE_DOWN;
            return JoyStick.DOWN;
        } else if (angle >= 112.5 && angle < 157.5) {
            if (distance > midDistance) return JoyStick.MORE_DOWNLEFT;
            return JoyStick.DOWNLEFT;
        } else if (angle >= 157.5 && angle < 202.5) {
            if (distance > midDistance) return JoyStick.MORE_LEFT;
            return JoyStick.LEFT;
        } else if (angle >= 202.5 && angle < 247.5) {
            if (distance > midDistance) return JoyStick.MORE_UPLEFT;
            return JoyStick.UPLEFT;
        }
        return state;
    }

    private void performOnChangeState(JoyStick next) {
        if (stickState != next) {
            // change from other state
            performOnChangeStateFromOthers();
        }
        setStickState(next);
    }

    private void performOnChangeStateFromOthers() {
        if (threadJoyStickMove != null && threadJoyStickMove.isAlive()) {
            threadJoyStickMove.interrupt();
        }
        threadJoyStickMove = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    // why post ?
                    post(new Runnable() {
                        public void run() {
                            performOnJoyStickMove();
                        }
                    });
                    try {
                        sleepJoyStick();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        threadJoyStickMove.start();
        performOnJoyStickMove();
    }

    private void sleepJoyStick() throws InterruptedException {
        long interval = loopInterval;
        if (hasFastLoop)
            interval = calCurrentInterval();
        Thread.sleep(interval);
    }

    private void performOnJoyStickMove() {
        if (onJoyStickMoveListener != null)
            onJoyStickMoveListener.onValueChanged(getAngle(), getDistance(),
                    getStickState());
    }

    private void interruptJoyStickMoveThread() {
        if (threadJoyStickMove != null) threadJoyStickMove.interrupt();
    }

    private void registerLayoutCenter(int width, int height) {
        jsEntity.center_x = width / 2;
        jsEntity.center_y = height / 2;
    }

    private void loadImages(Resources res) {
        releaseJoyStickImages();

        loadImages(res,
                RESID_BACKGROUND_DEFAULT,
                RESID_STICK_DEFAULT,
                RESID_SHADOW_DEFAULT);
        // if you remove shadow, you should also remove "stickTall" : stickTall = 0

        if (canUseSignal) loadSignalImages(res);
    }

    private void loadImages(Resources res,
                            int resIdBacks, int resIdStick, int resIdShadow) {
        background = BitmapFactory.decodeResource(res, resIdBacks);
        stick = BitmapFactory.decodeResource(res, resIdStick);
        shadow = BitmapFactory.decodeResource(res, resIdShadow);
    }

    private void loadSignalImages(Resources res) {
        releaseSignalImages();

        loadSignalImages(res,
                RESID_SIG_UP_DEFAULT,
                RESID_SIG_RIGHT_DEFAULT,
                RESID_SIG_DOWN_DEFAULT,
                RESID_SIG_LEFT_DEFAULT);
    }

    private void loadSignalImages(Resources res,
                                  int resIdUp, int resIdRight, int resIdDown, int resIdLeft) {
        signalUp = BitmapFactory.decodeResource(res, resIdUp);
        signalRight = BitmapFactory.decodeResource(res, resIdRight);
        signalDown = BitmapFactory.decodeResource(res, resIdDown);
        signalLeft = BitmapFactory.decodeResource(res, resIdLeft);
    }

    private void releaseJoyStickImages() {
        if (background != null) background.recycle();
        if (stick != null) stick.recycle();
        if (shadow != null) shadow.recycle();
    }

    private void releaseSignalImages() {
        if (signalUp != null) signalUp.recycle();
        if (signalRight != null) signalRight.recycle();
        if (signalDown != null) signalDown.recycle();
        if (signalLeft != null) signalLeft.recycle();
    }

    private void drawJoyStickWith(MotionEvent event) {
        Canvas canvas = surfaceHolder.lockCanvas();
        drawBaseCanvas(canvas);
        drawStick(canvas, event);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    private void drawBaseCanvas(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawBackground(canvas);
    }

    private void drawStick(Canvas canvas, MotionEvent event) {
        positionX = (int) (event.getX() - (params.width / 2));
        positionY = (int) (event.getY() - (params.height / 2));
        distance = (float) Math.sqrt(Math.pow(positionX, 2) + Math.pow(positionY, 2));
        angle = (float) calAngle(positionX, positionY);

        final float midDistanceX = (params.width / 2) - offset;
        final float midDistanceY = (params.height / 2) - offset;


        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (distance <= midDistanceX) {
                jsEntity.position(event.getX(), event.getY());
                jsEntity.setTouched(true);
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && jsEntity.isTouched()) {
            if (distance <= midDistanceX) {
                jsEntity.position(event.getX(), event.getY());
                drawSignal(canvas);
            } else if (distance > midDistanceX) {
                float x = (float) (Math.cos(Math.toRadians(calAngle(positionX, positionY))) * midDistanceX);
                float y = (float) (Math.sin(Math.toRadians(calAngle(positionX, positionY))) * midDistanceY);
                x += (params.width / 2);
                y += (params.height / 2);
                jsEntity.position(x, y);
                drawDarkenSignal(canvas);
            } else {
                // reset stick pad
//                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                drawBackground(canvas);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            // reset stick pad
            drawBaseCanvas(canvas);
            jsEntity.setTouched(false);
        }
        // reset stick
        if (jsEntity.isTouched()) drawDarkenStick(canvas); // darken stick on touched
        else drawStick(canvas);
    }

    private void drawStick(Canvas canvas) {
        if (jsEntity.isTouched()) {
            if (shadow != null && canUseShadow)
                canvas.drawBitmap(shadow, jsEntity.s_x, jsEntity.s_y, alphaStickPaint);
            canvas.drawBitmap(stick, jsEntity.x, jsEntity.y, alphaStickPaint);
        } else {
            if (shadow != null && canUseShadow) {
                canvas.drawBitmap(shadow,
                        jsEntity.center_x - (shadowWidth / 2),
                        jsEntity.center_y - (shadowHeight / 2) + stickTall,
                        alphaStickPaint);
                canvas.drawBitmap(stick, jsEntity.center_x - (stickWidth / 2), jsEntity.center_y - (stickHeight / 2) - stickTall, alphaStickPaint);
            } else {
                canvas.drawBitmap(stick, jsEntity.center_x - (stickWidth / 2), jsEntity.center_y - (stickHeight / 2), alphaStickPaint);
            }
        }
    }

    private void drawDarkenStick(Canvas canvas) {
        drawStick(canvas);
        drawStick(canvas);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawBitmap(background, 0, 0, alphaBacksPaint);
    }

    private void drawSignal(Canvas canvas) {

        if (!canUseSignal) return;
        Log.e("stickState",stickState.toString());

        switch (stickState) {

            case MORE_UP:
            case UP:
                canvas.drawBitmap(signalUp, 0, 0, alphaSigPaint);
                break;
            case MORE_RIGHT:
            case RIGHT:
                canvas.drawBitmap(signalRight, 0, 0, alphaSigPaint);
                break;
            case MORE_DOWN:
            case DOWN:
                canvas.drawBitmap(signalDown, 0, 0, alphaSigPaint);
                break;
            case MORE_LEFT:
            case LEFT:
                canvas.drawBitmap(signalLeft, 0, 0, alphaSigPaint);
                break;

            case MORE_UPLEFT:
            case UPLEFT:
                canvas.drawBitmap(signalLeft, 0, 0, alphaSigPaint);
                canvas.drawBitmap(signalUp, 0, 0, alphaSigPaint);
                break;

            case MORE_UPRIGHT:
            case UPRIGHT:
                canvas.drawBitmap(signalRight, 0, 0, alphaSigPaint);
                canvas.drawBitmap(signalUp, 0, 0, alphaSigPaint);
                break;

            case MORE_DOWNLEFT:
            case DOWNLEFT:
                canvas.drawBitmap(signalLeft, 0, 0, alphaSigPaint);
                canvas.drawBitmap(signalDown, 0, 0, alphaSigPaint);
                break;

            case MORE_DOWNRIGHT:
            case DOWNRIGHT:
                canvas.drawBitmap(signalRight, 0, 0, alphaSigPaint);
                canvas.drawBitmap(signalDown, 0, 0, alphaSigPaint);
                break;
        }
    }

    private void drawDarkenSignal(Canvas canvas) {
        drawSignal(canvas);
        drawSignal(canvas);
        drawSignal(canvas);
    }

    public int getPosX() {
        if (distance > minDistance && jsEntity.isTouched()) {
            return positionX;
        }
        return 0;
    }

    public int getPosY() {
        if (distance > minDistance && jsEntity.isTouched()) {
            return positionY;
        }
        return 0;
    }

    public float getAngle() {
        if (distance > minDistance && jsEntity.isTouched()) {
            return angle;
        }
        return 0;
    }

    public float getDistance() {
        if (distance > minDistance && jsEntity.isTouched()) {
            return distance;
        }
        return 0;
    }

    public int getMinimumDistance() {
        return minDistance;
    }

    public int getOffset() {
        return offset;
    }

    public int getLayoutAlpha() {
        return alphaLayout;
    }

    public int getStickAlpha() {
        return alphaStick;
    }

    public int getAlphaSignal() {
        return alphaSignal;
    }

    public JoyStick getStickState() {
        return stickState;
    }

    public boolean isShadowVisible() {
        return canUseShadow;
    }

    private void resizeImages() {
        if (shadow != null) {
            shadow = resizeImage(shadow, shadowWidth, shadowHeight);
        }

        stick = resizeImage(stick, stickWidth, stickHeight);
        background = resizeImage(background, params.width, params.height);

        if (canUseSignal) {
            resizeSignalImages();
        }
    }

    private void resizeSignalImages() {
        signalUp = resizeImage(signalUp, params.width, params.height);
        signalRight = resizeImage(signalRight, params.width, params.height);
        signalDown = resizeImage(signalDown, params.width, params.height);
        signalLeft = resizeImage(signalLeft, params.width, params.height);
    }


    private Bitmap resizeImage(final Bitmap original, int targetWidth, int targetHeight) {
        return Bitmap.createScaledBitmap(original, targetWidth, targetHeight, false);
    }

    private void setStickState(JoyStick next) {
        if (next != this.stickState) onChangeStateListener.onChangeState(next, this.stickState);
        this.stickState = next;
    }

    public void setStickSize(int width, int height) {
        stickWidth = width;
        stickHeight = height;
    }

    public void setMinimumDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setLayoutAlpha(int alpha) {
        alphaLayout = alpha;
        alphaBacksPaint.setAlpha(alpha);
    }

    public void setStickAlpha(int alpha) {
        alphaStick = alpha;
        alphaStickPaint.setAlpha(alpha);
    }

    public void setSignalAlpha(int alpha) {
        alphaSignal = alpha;
        alphaSigPaint.setAlpha(alpha);
    }

    public void setShadowSize(int width, int height) {
        this.shadowWidth = width;
        this.shadowHeight = height;
    }

    public void setShadowVisibility(boolean shouldDrawShadow) {
        this.canUseShadow = shouldDrawShadow;
    }

    public void setSignalAvailability(boolean canUseSignal) {
        this.canUseSignal = canUseSignal;
        if (canUseSignal) {
            loadImages(res);
            resizeSignalImages();
        } else {
            releaseSignalImages();
        }
    }

    private double calAngle(float x, float y) {
        if (x >= 0 && y >= 0)
            return Math.toDegrees(Math.atan(y / x));
        else if (x < 0 && y >= 0)
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if (x < 0 && y < 0)
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if (x >= 0 && y < 0)
            return Math.toDegrees(Math.atan(y / x)) + 360;
        return 0;
    }

    public void setOnJoyStickMoveListener(OnJoystickMoveListener listener,
                                          long loopInterval) {
        this.onJoyStickMoveListener = listener;
        this.loopInterval = loopInterval;
        this.hasFastLoop = false;
    }


    public void setOnJoyStickMoveListener(OnJoystickMoveListener listener,
                                          long loopSlowInterval, long loopFastInterval) {
        setOnJoyStickMoveListener(listener, loopSlowInterval);
        this.loopFastInterval = loopFastInterval;
        this.hasFastLoop = true;
    }

    public void setOnLongPushListener(OnLongPushListener onLongPushListener) {
        this.onLongPushed = new OnLongPushRunnable(onLongPushListener);

    }

    public void setOnChangeStateListener(OnChangeStateListener onChangeStateListener) {
        this.onChangeStateListener = onChangeStateListener;
    }

    private long calCurrentInterval() {
        long in = loopInterval;
        final float midDistance = (params.width / 2) - offset;
        if (distance <= midDistance) in = loopInterval;
        else if (distance > midDistance) in = loopFastInterval;
        return in;
    }

    /**
     * Event Listeners (and Runnable)
     */
    public interface OnLongPushListener {
        void onLongPush();
    }

    public interface OnChangeStateListener {
        void onChangeState(JoyStick next, JoyStick previous);
    }

    public interface OnJoystickMoveListener {
        void onValueChanged(float angle, float power, JoyStick state);
    }

    private class OnLongPushRunnable implements Runnable {
        OnLongPushListener listener;

        public OnLongPushRunnable(OnLongPushListener l) {
            this.listener = l;
        }

        @Override
        public void run() {
            performLongPushed();
        }

        private void performLongPushed() {
            this.listener.onLongPush();
            setStickState(JoyStick.LONGPUSH);
        }
    }

    // seems to cause ERROR
//    public interface OnJoyStickMoveListener {
//        void onValueChanged(float angle, float power, JoyStickState direction);
//    }

    public enum JoyStick {
        NONE,
        UP,
        UPRIGHT,
        RIGHT,
        DOWNRIGHT,
        DOWN,
        DOWNLEFT,
        LEFT,
        UPLEFT,
        LONGPUSH,

        MORE_UP,
        MORE_UPRIGHT,
        MORE_RIGHT,
        MORE_DOWNRIGHT,
        MORE_DOWN,
        MORE_DOWNLEFT,
        MORE_LEFT,
        MORE_UPLEFT;

        public static boolean isMore(JoyStick next, JoyStick previous) {
            boolean isMore = false;
            if (previous == UP && next == MORE_UP) isMore = true;
            if (previous == RIGHT && next == MORE_RIGHT) isMore = true;
            if (previous == DOWN && next == MORE_DOWN) isMore = true;
            if (previous == LEFT && next == MORE_LEFT) isMore = true;
            return isMore;
        }

        public static boolean isLess(JoyStick next, JoyStick previous) {
            boolean isMore = false;
            if (next == UP && previous == MORE_UP) isMore = true;
            if (next == RIGHT && previous == MORE_RIGHT) isMore = true;
            if (next == DOWN && previous == MORE_DOWN) isMore = true;
            if (next == LEFT && previous == MORE_LEFT) isMore = true;
            return isMore;
        }
    }

    private class JoyStickEntity {
        private boolean isTouched = false;
        private float x, y;
        private float s_x, s_y; // shadow
        private float center_x, center_y; // center

        private void position(float pos_x, float pos_y) {
            x = pos_x - (stickWidth / 2);
            y = pos_y - (stickHeight / 2);

            // vecPC : position - center vec
            float vecPC_x = center_x - pos_x;
            float vecPC_y = center_y - pos_y;
            s_x = pos_x - (shadowWidth / 2) + vecPC_x / 3;
            s_y = pos_y - (shadowHeight / 2) + vecPC_y / 3;
        }

        private boolean isTouched() {
            return isTouched;
        }

        private void setTouched(boolean touched) {
            isTouched = touched;
        }
    }
}
