package org.tec_hub.tecuniversalcomm.data.inputs;

import android.content.Context;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.tec_hub.tecuniversalcomm.R;

/**
 * Created by Nick Mosher on 10/9/15.
 */
public class InputHex extends LinearLayout implements View.OnClickListener {

    private final int[] mButtonReferences = {
            R.id.hex0,
            R.id.hex1,
            R.id.hex2,
            R.id.hex3,
            R.id.hex4,
            R.id.hex5,
            R.id.hex6,
            R.id.hex7,
            R.id.hex8,
            R.id.hex9,
            R.id.hexA,
            R.id.hexB,
            R.id.hexC,
            R.id.hexD,
            R.id.hexE,
            R.id.hexF
    };

    private Button[] mButtons;
    private TextView mDisplay;

    public InputHex(Context context) {
        super(context);
    }

    public InputHex(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    public InputHex(Context context, AttributeSet attributes, int defStyle) {
        super(context, attributes, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setLayoutParams(new LinearLayoutCompat.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mButtons = new Button[mButtonReferences.length];
        for(int i = 0; i < mButtons.length; i++) {
            mButtons[i] = (Button) findViewById(mButtonReferences[i]);
            mButtons[i].setOnClickListener(this);
        }
        mDisplay = (TextView) findViewById(R.id.text);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.hex0:

                break;
            case R.id.hex1:

                break;
            case R.id.hex2:

                break;
            case R.id.hex3:

                break;
            case R.id.hex4:

                break;
            case R.id.hex5:

                break;
            case R.id.hex6:

                break;
            case R.id.hex7:

                break;
            case R.id.hex8:

                break;
            case R.id.hex9:

                break;
            case R.id.hexA:

                break;
            case R.id.hexB:

                break;
            case R.id.hexC:

                break;
            case R.id.hexD:

                break;
            case R.id.hexE:

                break;
            case R.id.hexF:

                break;
            default:
        }
    }
}
