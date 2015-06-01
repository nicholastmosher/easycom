package org.tec_hub.tecuniversalcomm.views;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.google.common.base.Preconditions;

import java.util.Random;

/**
 * Created by Nick Mosher on 5/7/15.
 */
public abstract class TECControl {

    protected Context mContext;

    protected static Random mRandom = new Random();
    protected String mDataCode;
    protected boolean mVerifySentData;
    protected boolean mSendContinuous;
    protected long mSendPeriod;

    protected boolean mVisible;
    protected int mXAnchor;
    protected int mYAnchor;

    protected TECControl(Context context, String code) {
        mContext = Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(code);
        if(code.equals("")) {
            throw new IllegalArgumentException("Data code cannot be blank!");
        } else {
            mDataCode = code;
        }
        mVerifySentData = false;
        mSendContinuous = true;
        mSendPeriod = 50;
        mVisible = true;
    }

    public boolean verifiesData() {
        return mVerifySentData;
    }

    public boolean sendsDataContinuously() {
        return mSendContinuous;
    }

    public long getSendPeriod() {
        return mSendPeriod;
    }

    public void setSendPeriod(long period) {
        if(period > 0L) {
            mSendPeriod = period;
        } else {
            new IllegalArgumentException("Send Period must be greater than 0!").printStackTrace();
        }
    }

    public void setXAnchor(int x) {
        if(x > 0) {
            mXAnchor = x;
        } else {
            new IllegalArgumentException("Control anchors cannot be less than 0!").printStackTrace();
        }
    }

    public void setYAnchor(int y) {
        if(y > 0) {
            mYAnchor = y;
        } else {
            new IllegalArgumentException("Control anchors cannot be less than 0!").printStackTrace();
        }
    }

    public void setVisibility(boolean visible) {
        mVisible = visible;
    }

    public boolean isVisible() {
        return mVisible;
    }

    public String getDataCode() {
        return mDataCode;
    }

    public boolean conflictsWith(TECControl view) {
        if(this.getDataCode().equals(view.getDataCode())) {
            return true;
        }
        return false;
    }

    protected abstract void sendValue();

    protected abstract void onDraw(Canvas canvas);

    protected abstract void onTouchEvent(MotionEvent event, int pointerIndex, boolean editable);

    protected abstract boolean overlaps(float x, float y);

    protected abstract boolean overlaps(MotionEvent event, int pointerIndex);
}