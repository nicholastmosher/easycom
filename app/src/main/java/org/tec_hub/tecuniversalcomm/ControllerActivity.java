package org.tec_hub.tecuniversalcomm;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

import org.tec_hub.tecuniversalcomm.views.JoystickControl;
import org.tec_hub.tecuniversalcomm.views.TECControlView;

/**
 * Created by Nick Mosher on 5/7/15.
 */
public class ControllerActivity extends Activity {

    private FrameLayout mBase;
    private TECControlView mControlView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        mBase = (FrameLayout) findViewById(R.id.controller_frame);
        mControlView = new TECControlView(this);

        JoystickControl joystick = new JoystickControl(this, "Joystick code!");
        mControlView.addChild(joystick);

        mBase.addView(mControlView);
    }
}
