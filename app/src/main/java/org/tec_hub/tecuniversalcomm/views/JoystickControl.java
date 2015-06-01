package org.tec_hub.tecuniversalcomm.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

import org.tec_hub.tecuniversalcomm.R;

/**
 * Created by Nick Mosher on 5/7/15.
 */
public class JoystickControl extends TECControl {

    public static final String DEFAULT_DATA_CODE = "Joystick";

    private Paint mBasePaint;
    private Paint mStickPaint;
    private Paint mTextPaint;

    private int mBaseRadius = 200;
    private int mStickRadius = 100;
    private int mRangePadding = 20;
    private int mStickX;
    private int mStickY;

    public JoystickControl(Context context) {
        super(context, DEFAULT_DATA_CODE + Integer.toString(mRandom.nextInt(100000)));
        init();
    }

    public JoystickControl(Context context, String code) {
        super(context, code);
        init();
    }

    private void init() {
        mBasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mBasePaint.setColor(mContext.getResources().getColor(R.color.joystick_base));
        mStickPaint.setColor(mContext.getResources().getColor(R.color.joystick_stick));
        mTextPaint.setColor(0xff030303);

        mXAnchor = 500;
        mYAnchor = 500;
        mStickX = mXAnchor;
        mStickY = mYAnchor;
    }

    public void onDraw(Canvas canvas) {
        if(mVisible) {
            canvas.drawCircle(mXAnchor, mYAnchor, mBaseRadius, mBasePaint);
            canvas.drawCircle(mStickX, mStickY, mStickRadius, mStickPaint);
        }
    }

    public void onTouchEvent(MotionEvent event, int pointer, boolean editable) {

        if(editable) {
            float x = event.getX(pointer);
            float y = event.getY(pointer);

            mXAnchor = (int) x;
            mYAnchor = (int) y;
            mStickX = mXAnchor;
            mStickY = mYAnchor;

        } else {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mStickX = mXAnchor;
                mStickY = mYAnchor;
            } else {
                float x = event.getX(pointer);
                float y = event.getY(pointer);

                updateStickPosition((int) x, (int) y);
            }
        }
    }

    public boolean overlaps(float x, float y) {
        double d = Math.sqrt(Math.pow(x - mXAnchor, 2) + Math.pow(x - mYAnchor, 2));
        return d < mBaseRadius;
    }

    public boolean overlaps(MotionEvent event, int pointer) {
        return this.overlaps(event.getX(pointer), event.getY(pointer));
    }

    protected void sendValue() {

    }

    private void updateStickPosition(int x, int y) {
        double maxDistance = (mBaseRadius - mRangePadding) - mStickRadius;
        double actualDistance = Math.sqrt(Math.pow(x - mXAnchor, 2) + Math.pow(y - mYAnchor, 2));

        if(actualDistance < maxDistance) {
            mStickX = x;
            mStickY = y;
        } else {
            double shrinkFactor = maxDistance / actualDistance;
            int deltaX = (int) ((x - mXAnchor) * shrinkFactor);
            int deltaY = (int) ((y - mYAnchor) * shrinkFactor);
            mStickX = mXAnchor + deltaX;
            mStickY = mYAnchor + deltaY;
        }
    }
}