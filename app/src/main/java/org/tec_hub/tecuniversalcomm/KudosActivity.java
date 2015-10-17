package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.data.Packet;
import org.tec_hub.tecuniversalcomm.data.connection.intents.ConnectionIntent;
import org.tec_hub.tecuniversalcomm.data.connection.intents.DataSendIntent;

/**
 * Created by Nick Mosher on 4/24/15.
 * Temporary activity made to demonstrate use of the BluetoothConnection
 * in a practical application, namely the driving of the Kudos rover.
 * Senses Accelerometer readings and sends Json data of drive commands
 * in a UDP-style packet spamming with no verification.
 */
public class KudosActivity extends AppCompatActivity {

    private BluetoothConnection mConnection;
    private BigPanel mPanel;
    private Thread mDriveThread;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private boolean panelHeldDown = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kudos);

        Intent launchIntent = getIntent();
        mConnection = launchIntent.getParcelableExtra(ConnectionIntent.CONNECTION_UUID);
        Preconditions.checkNotNull(mConnection);

        Toolbar toolbar = new Toolbar(this);
        toolbar.setTitle("Kudos Controller");
        toolbar.setSubtitle("Hold down screen and tilt to move");
        setSupportActionBar(toolbar);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.kudos_screen);
        mPanel = new BigPanel(this);
        frameLayout.addView(mPanel);
    }

    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
        mDriveThread = new DriveKudosThread();
        mDriveThread.start();
        mSensorManager.registerListener((SensorEventListener) mDriveThread, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        super.onPause();
        mDriveThread.interrupt();
        mSensorManager.unregisterListener((SensorEventListener) mDriveThread);
    }

    public void onStop() {
        super.onStop();
        mDriveThread.interrupt();
    }

    public void onDestroy() {
        super.onDestroy();
        mDriveThread.interrupt();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            //If the up button is pressed, act like the back button.
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class BigPanel extends FrameLayout {

        public BigPanel(Context context) {
            super(context);
            setBackgroundColor(ContextCompat.getColor(KudosActivity.this, R.color.neutral));
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch(motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    panelHeldDown = true;
                    setBackgroundColor(ContextCompat.getColor(KudosActivity.this, R.color.enabled));
                    break;
                case MotionEvent.ACTION_UP:
                    panelHeldDown = false;
                    setBackgroundColor(ContextCompat.getColor(KudosActivity.this, R.color.disabled));
                    break;
                default:
            }
            return true;
        }

        public boolean isHeldDown() {
            return panelHeldDown;
        }
    }

    public class DriveKudosThread extends Thread implements SensorEventListener {
        private boolean running;

        private double mX = 0.0;
        private double mY = 0.0;
        private double mZ = 0.0;

        public DriveKudosThread() {
            running = true;
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;

            if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mX = event.values[0];
                mY = event.values[1];
                mZ = event.values[2];

                System.out.println("Accelerometer values: x=" + mX + ", y=" + mY + ", z=" + mZ);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void run() {
            running = true;
            System.out.println("Started background task");

            int thinner = 0;
            while(running) {

                String sendData = null;

                if(mPanel.isHeldDown()) {

                    thinner = (++thinner % 5);
                    if(thinner % 5 == 0) {
                        sendData = Packet.asBoolean("KudosEnable", true).toJson();
                        System.out.println("Data to send: " + sendData);
                        new DataSendIntent(KudosActivity.this, mConnection.getUUID(), sendData.getBytes()).sendLocal();
                    }

                    double x = mX;
                    double y = mY;

                    double upperCap = 7.0;
                    double lowerCap = -7.0;

                    if(x > upperCap) {
                        x = upperCap;
                    } else if(x < lowerCap) {
                        x = lowerCap;
                    }

                    if(y > upperCap) {
                        y = upperCap;
                    } else if(y < lowerCap) {
                        y = lowerCap;
                    }

                    x = map(x, lowerCap, upperCap, 150, 30);
                    y = map(y, lowerCap, upperCap, 30, 150);

                    //Trim to two decimals
                    x = Math.floor(x * 100) / 100;
                    y = Math.floor(y * 100) / 100;

                    sendData = Packet.asDoubleArray("KudosDrive", new double[]{x, y}).toJson();
                    System.out.println("Data to send: " + sendData);
                    new DataSendIntent(KudosActivity.this, mConnection.getUUID(), sendData.getBytes()).sendLocal();
                } else {
                    sendData = Packet.asBoolean("KudosEnable", false).toJson();
                    System.out.println("Data to send: " + sendData);
                    new DataSendIntent(KudosActivity.this, mConnection.getUUID(), sendData.getBytes()).sendLocal();
                }

                try {
                    Thread.sleep(100);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                    running = false;
                }
            }
            System.out.println("Ended background task");
        }

        private double map(double x, double in_min, double in_max, double out_min, double out_max)
        {
            return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
        }
    }

    /**
     * Created by Nick Mosher on 5/1/15.
     * Provides static methods that are generally useful
     * but do not necessarily require their own class.
     */
    public static class Utility {

        /**
         * Scales the input from the old range to a new one.
         * @param input The input to be transformed.
         * @param oldMin The old range's minimum.
         * @param oldMax The old range's maximum.
         * @param newMin The new range's minimum.
         * @param newMax The new range's maximum.
         * @return The input as represented on the new range.
         */
        public static double scale(double input, double oldMin, double oldMax, double newMin, double newMax) {
            return (input - oldMin) * (newMax - newMin) / (oldMax - oldMin) + newMin;
        }

        /**
         * Scales the input from the old range to a new one.
         * @param input The input to be transformed.
         * @param oldMin The old range's minimum.
         * @param oldMax The old range's maximum.
         * @param newMin The new range's minimum.
         * @param newMax The new range's maximum.
         * @return The input as represented on the new range.
         */
        public static int scale(int input, int oldMin, int oldMax, int newMin, int newMax) {
            return (int) scale((double) input, (double) oldMin, (double) oldMax,(double) newMin, (double) newMax);
        }

        /**
         * If the input is within the range from [min, max], it is unaltered.
         * Otherwise, min or max is returned depending on the range offense.
         * @param input The input to be trimmed.
         * @param min The minimum value that the output may be.
         * @param max The maximum value that the output may be.
         * @return The input, or the minimum or maximum cap.
         */
        public static double trim(double input, double min, double max) {
            if(input > max) {
                input = max;
            } else if(input < min) {
                input = min;
            }
            return input;
        }

        /**
         * If the input is within the range from [-range, range], it is unaltered.
         * Otherwise, -range or range is returned depending on the range offense.
         * If the range is less than 0, then 0 is always returned.
         * @param input The input to be trimmed.
         * @param range The range centered about 0 to trim the input to.
         * @return The input, or a range cap.
         */
        public static double trim(double input, double range) {
            if(range < 0) {
                return 0.0;
            }

            if(input > range) {
                input = range;
            } else if(input < -range) {
                input = -range;
            }
            return input;
        }

        /**
         * If the input is within the range from [min, max], it is unaltered.
         * Otherwise, min or max is returned depending on the range offense.
         * @param input The input to be trimmed.
         * @param min The minimum value that the output may be.
         * @param max The maximum value that the output may be.
         * @return The input, or the minimum or maximum cap.
         */
        public static int trim(int input, int min, int max) {
            if(input > max) {
                input = max;
            } else if(input < min) {
                input = min;
            }
            return input;
        }

        /**
         * If the input is within the range from [-range, range], it is unaltered.
         * Otherwise, -range or range is returned depending on the range offense.
         * If the range is less than 0, then 0 is always returned.
         * @param input The input to be trimmed.
         * @param range The range centered about 0 to trim the input to.
         * @return The input, or a range cap.
         */
        public static int trim(int input, int range) {
            if(range < 0) {
                return 0;
            }

            if(input > range) {
                input = range;
            } else if(input < -range) {
                input = -range;
            }
            return input;
        }

        /**
         * Creates a deadband from range [min, max].  If the input lands
         * in this deadband, then the value def (default) is returned.
         * @param input The input to check against the deadband.
         * @param min The minimum value of the deadband range.
         * @param max The maximum value of the deadband range.
         * @param def The default value to return if the input is inside the deadband.
         * @return The input if it's outside of the deadband, the default value otherwise.
         */
        public static double deadband(double input, double min, double max, double def) {
            if(input <= max && input >= min) {
                return def;
            } else {
                return input;
            }
        }

        /**
         * Creates a deadband from range [min, max].  If the input lands
         * in this deadband, then a value of 0.0 is returned.
         * @param input The input to check against the deadband.
         * @param min The minimum value of the deadband range.
         * @param max The maximum value of the deadband range.
         * @return The input if it's outside of the deadband, 0.0 otherwise.
         */
        public static double deadband(double input, double min, double max) {
            return deadband(input, min, max, 0.0);
        }

        /**
         * Creates a deadband from range [-range, range].  If the input lands
         * in this deadband, then a value of 0.0 is returned.
         * @param input The input to check against the deadband.
         * @param range The distance about 0.0 to place the deadband.
         * @return The input if it's outside of the deadband, 0.0 otherwise.
         */
        public static double deadband(double input, double range) {
            return deadband(input, -range, range);
        }

        /**
         * Creates a deadband from range [min, max].  If the input lands
         * in this deadband, then the value def (default) is returned.
         * @param input The input to check against the deadband.
         * @param min The minimum value of the deadband range.
         * @param max The maximum value of the deadband range.
         * @param def The default value to return if the input is inside the deadband.
         * @return The input if it's outside of the deadband, the default value otherwise.
         */
        public static int deadband(int input, int min, int max, int def) {
            if(input <= max && input >= min) {
                return def;
            } else {
                return input;
            }
        }

        /**
         * Creates a deadband from range [min, max].  If the input lands
         * in this deadband, then a value of 0 is returned.
         * @param input The input to check against the deadband.
         * @param min The minimum value of the deadband range.
         * @param max The maximum value of the deadband range.
         * @return The input if it's outside of the deadband, 0 otherwise.
         */
        public static int deadband(int input, int min, int max) {
            return deadband(input, min, max, 0);
        }

        /**
         * Creates a deadband from range [-range, range].  If the input lands
         * in this deadband, then a value of 0 is returned.
         * @param input The input to check against the deadband.
         * @param range The distance about 0 to place the deadband.
         * @return The input if it's outside of the deadband, 0 otherwise.
         */
        public static int deadband(int input, int range) {
            return deadband(input, -range, range);
        }
    }

    /**
     * Created by Nick Mosher on 4/29/15.
     */
    public static class Latch {
        private boolean lastBool;

        /**
         * Constructs a new latch.
         */
        public Latch()
        {
            lastBool = true;
        }

        /**
         * Keeps track of the parameter and returns true only
         * if the last onTrue() call was false and this onTrue()
         * call is true.
         * @param nowBool The current status of the latching input.
         * @return True when the parameter CHANGES from false to true.
         */
        public boolean onTrue(boolean nowBool)
        {
            boolean result = nowBool && !lastBool;
            lastBool = nowBool;
            return result;
        }

        /**
         * Keeps track of the parameter and returns true only
         * if the last onFalse() call was true and this onFalse()
         * call is false.
         * @param nowBool The current status of the latching input.
         * @return True when the parameter CHANGES from true to false.
         */
        public boolean onFalse(boolean nowBool)
        {
            return onTrue(!nowBool);
        }

        /**
         * Keeps track of the parameter and returns true only
         * if the last onChange() is different from this onChange() call.
         * @param nowBool The current status of the latching input.
         * @return True when the parameter changes.
         */
        public boolean onChange(boolean nowBool)
        {
            return onTrue(nowBool) || onFalse(nowBool);
        }
    }
}