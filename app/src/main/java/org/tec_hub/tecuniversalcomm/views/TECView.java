package org.tec_hub.tecuniversalcomm.views;

import android.content.Context;
import android.view.View;

import com.google.common.base.Preconditions;

import java.util.Random;

/**
 * Created by Nick Mosher on 5/7/15.
 */
public abstract class TECView extends View {

    protected static Random mRandom = new Random();
    protected String mDataCode;
    protected boolean mEditable;
    protected boolean mVerifySentData;
    protected boolean mSendContinuous;
    protected long mSendPeriod;

    protected TECView(Context context, String code) {
        super(context);
        Preconditions.checkNotNull(code);
        if(code.equals("")) {
            throw new IllegalArgumentException("Data code cannot be blank!");
        } else {
            mDataCode = code;
        }
        mEditable = false;
        mVerifySentData = false;
        mSendContinuous = true;
        mSendPeriod = 50;
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

    protected abstract void sendValue();
}
