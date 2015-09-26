package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.data.Packet;
import org.tec_hub.tecuniversalcomm.intents.BluetoothSendIntent;
import org.tec_hub.tecuniversalcomm.intents.TECIntent;

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
        mConnection = launchIntent.getParcelableExtra(TECIntent.BLUETOOTH_CONNECTION_DATA);
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
                        BluetoothSendIntent enableKudosIntent = new BluetoothSendIntent(KudosActivity.this, mConnection, sendData);
                        LocalBroadcastManager.getInstance(KudosActivity.this).sendBroadcast(enableKudosIntent);
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
                    BluetoothSendIntent driveKudosIntent = new BluetoothSendIntent(KudosActivity.this, mConnection, sendData);
                    LocalBroadcastManager.getInstance(KudosActivity.this).sendBroadcast(driveKudosIntent);
                } else {
                    sendData = Packet.asBoolean("KudosEnable", false).toJson();
                    System.out.println("Data to send: " + sendData);
                    BluetoothSendIntent enableKudosIntent = new BluetoothSendIntent(KudosActivity.this, mConnection, sendData);
                    LocalBroadcastManager.getInstance(KudosActivity.this).sendBroadcast(enableKudosIntent);
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
}