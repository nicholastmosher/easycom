package org.tec_hub.tecuniversalcomm.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Nick Mosher on 5/7/15.
 */
public class JoystickView extends TECView {

    public static final String DEFAULT_DATA_CODE = "Joystick";

    private Paint mBasePaint;
    private Paint mStickPaint;
    private Paint mTextPaint;

    private int mBaseRadius;
    private int mStickRadius;

    public JoystickView(Context context) {
        super(context, DEFAULT_DATA_CODE + Integer.toString(mRandom.nextInt(100000)));
    }

    public JoystickView(Context context, String code) {
        super(context, code);
    }

    private void init() {
        mBasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mBasePaint.setColor(0xff101010);
        mStickPaint.setColor(0xff050505);
        mTextPaint.setColor(0xff030303);
    }

    public void onDraw(Canvas canvas) {

    }

    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {

    }

    protected void sendValue() {

    }
}