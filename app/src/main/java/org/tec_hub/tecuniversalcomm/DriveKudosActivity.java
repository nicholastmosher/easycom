package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.data.Packet;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Nick Mosher on 4/24/15.
 */
public class DriveKudosActivity extends ActionBarActivity {

    private BluetoothConnection mConnection;
    private BigPanel mPanel;
    private Thread mDriveThread;
    private boolean panelHeldDown = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_kudos);

        Intent launchIntent = getIntent();
        mConnection = launchIntent.getParcelableExtra(TECIntent.BLUETOOTH_CONNECTION_DATA);
        Preconditions.checkNotNull(mConnection);

        getSupportActionBar().setTitle("Kudos Controller");
        getSupportActionBar().setSubtitle("Hold down screen and tilt to move");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
    }

    public void onStop() {
        super.onStop();
        mDriveThread.interrupt();
    }

    private class BigPanel extends FrameLayout {

        public BigPanel(Context context) {
            super(context);
            setBackgroundColor(getResources().getColor(R.color.background_screen));
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch(motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    panelHeldDown = true;
                    setBackgroundColor(getResources().getColor(R.color.green));
                    break;
                case MotionEvent.ACTION_UP:
                    panelHeldDown = false;
                    setBackgroundColor(getResources().getColor(R.color.red));
                    break;
                default:
            }
            return true;
        }
    }

    public class DriveKudosThread extends Thread {

        private boolean isRunning;

        public DriveKudosThread() {
            isRunning = true;
        }

        @Override
        public void run() {
            isRunning = true;
            System.out.println("Started background task");
            OutputStream writer;
            boolean requestedConnect = false;
            while(isRunning) {
                if(!mConnection.isConnected() && !requestedConnect) {
                    mConnection.connect(DriveKudosActivity.this);
                    requestedConnect = true;
                }

                if(mConnection.isConnected()) {
                    writer = mConnection.getOutputStream();
                    String data;
                    if(panelHeldDown) {
                        data = Packet.asDoubleArray("KudosDrive", new double[]{1.0, -1.0}).toJson();
                    } else {
                        data = Packet.asDoubleArray("KudosDrive", new double[]{0.0, 0.0}).toJson();
                    }
                    System.out.println(data);
                    try {
                        writer.write(data.getBytes());
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                    isRunning = false;
                }
            }
            System.out.println("Ended background task");
        }
    }
}