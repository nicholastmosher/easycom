package org.tec_hub.tecuniversalcomm.data.connection;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.TerminalActivity;
import org.tec_hub.tecuniversalcomm.intents.BluetoothReceiveIntent;
import org.tec_hub.tecuniversalcomm.intents.TECIntent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Nick Mosher on 4/23/15.
 */
public class BluetoothConnectionService extends Service implements Observer {

    private static boolean launched = false;

    //Thread to run an input reading loop
    ReceiveInputThread receiveInputThread;

    public void onCreate() {
        launched = true;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TECIntent.ACTION_BLUETOOTH_CONNECT);
        intentFilter.addAction(TECIntent.ACTION_BLUETOOTH_DISCONNECT);
        intentFilter.addAction(TECIntent.ACTION_BLUETOOTH_SEND_DATA);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final BluetoothConnection bluetoothConnection = intent.getParcelableExtra(TECIntent.BLUETOOTH_CONNECTION_DATA);
                Preconditions.checkNotNull(bluetoothConnection);

                switch(intent.getAction()) {

                    //Received action to establish connection
                    case TECIntent.ACTION_BLUETOOTH_CONNECT:

                        //Create callbacks for successful connection and disconnection
                        bluetoothConnection.addObserver(BluetoothConnectionService.this);

                        //Initiate connecting
                        new BluetoothConnectTask(bluetoothConnection).execute();
                        break;

                    //Received action to disconnect
                    case TECIntent.ACTION_BLUETOOTH_DISCONNECT:
                        //Initiate disconnecting
                        new BluetoothDisconnectTask(bluetoothConnection).execute();
                        break;

                    //Received intent with data to send
                    case TECIntent.ACTION_BLUETOOTH_SEND_DATA:
                        //System.out.println("Service -> Sending Data...");
                        String sendData = intent.getStringExtra(TECIntent.BLUETOOTH_SEND_DATA);
                        sendBluetoothData(bluetoothConnection, sendData);
                        break;

                    default:
                }
            }
        }, intentFilter);

        return Service.START_STICKY;
    }

    public void onDestroy() {
        launched = false;
    }

    /**
     * Launches the BluetoothConnectionService if it is not already active.
     * @param context The context to launch the Service from.
     */
    public static void launch(Context context) {
        if(!launched) {
            context.startService(new Intent(context, BluetoothConnectionService.class));
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isLaunched() {
        return launched;
    }

    @Override
    public void update(Observable observable, Object data) {
        if(!(observable instanceof BluetoothConnection)) {
            throw new IllegalStateException("Update did not originate at a BluetoothConnection!");
        }

        if(data instanceof Connection.ObserverCues) {
            BluetoothConnection bluetoothConnection = (BluetoothConnection) observable;
            Connection.ObserverCues cue = (Connection.ObserverCues) data;
            switch(cue) {
                case Connected:
                    System.out.println("Observer -> Connected");
                    if (receiveInputThread != null) {
                        receiveInputThread.interrupt();
                    }
                    receiveInputThread = new ReceiveInputThread(bluetoothConnection);
                    receiveInputThread.start();
                    break;
                case Disconnected:
                    System.out.println("Observer -> Disconnected");
                    if(receiveInputThread != null) {
                        receiveInputThread.interrupt();
                    }
                    receiveInputThread = null;
                    break;
                case ConnectFailed:
                    System.out.println("Observer -> ConnectFailed");
                    break;
                default:
            }
        }
    }

    private void sendBluetoothData(BluetoothConnection connection, String data) {
        Preconditions.checkNotNull(connection);
        Preconditions.checkNotNull(data);

        if(!data.equals("")) {
            if(connection.isConnected()) {
                try {
                    connection.getOutputStream().write(data.getBytes());
                } catch(IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Connection is not connected!");
            }
        } else {
            System.out.println("Data to send is blank!");
        }
    }

    private static class BluetoothConnectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothConnection mConnection;
        private BluetoothAdapter mBluetoothAdapter;
        private BluetoothSocket mBluetoothSocket;
        private static int retryCount;

        private BluetoothConnectTask(BluetoothConnection connection, int retry) {
            mConnection = Preconditions.checkNotNull(connection);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothSocket = null;
            retryCount = retry;
        }

        public BluetoothConnectTask(BluetoothConnection connection) {
            this(connection, 0);
        }

        protected Boolean doInBackground(Void... params) {
            //Check if BT is enabled
            if (!mBluetoothAdapter.isEnabled()) {
                System.out.println("Bluetooth not enabled!"); //TODO better handling.
                new IllegalStateException("Cannot connect, Bluetooth is disabled!").printStackTrace();
            }
            System.out.println("BluetoothAdapter Enabled...");

            //Define a BluetoothDevice with the address from our Connection.
            String address = mConnection.getAddress();
            BluetoothDevice device;
            if(address != null  && BluetoothAdapter.checkBluetoothAddress(address)) {
                device = mBluetoothAdapter.getRemoteDevice(address);
                System.out.println("Bluetooth Device parsed from BluetoothAdapter...");
            } else {
                //FIXME What have I done?
                new IllegalStateException("Error connecting to bluetooth! Problem with address.").printStackTrace();
                return false;
            }

            //Try to retrieve a BluetoothSocket from the BluetoothDevice.
            try {
                mBluetoothSocket = device.createRfcommSocketToServiceRecord(BluetoothConnection.BLUETOOTH_SERIAL_UUID);
                System.out.println("BluetoothSocket retrieved from Bluetooth Device...");

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            //Shouldn't need to be discovering at this point.
            mBluetoothAdapter.cancelDiscovery();

            //Attempt to connect to the bluetooth device and receive a BluetoothSocket
            try {
                mBluetoothSocket.connect();
                mConnection.setBluetoothSocket(mBluetoothSocket);
                System.out.println("BluetoothSocket connected, success!");
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mBluetoothSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                return false;
            }

            //If we've made it this far, must have been a success.
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if(success) {
                System.out.println("Connected success");
                mConnection.notifyObservers(Connection.ObserverCues.Connected);
            } else {
                System.out.println("Connected failed");
                if(mBluetoothSocket.isConnected()) {
                    System.out.println("WARNING: BluetoothConnectTask reported error, but is connected.");
                    mConnection.notifyObservers(Connection.ObserverCues.Connected);
                } else {
                    if(retryCount < 3) {
                        retryCount++;
                        System.out.println("Error connecting! Retrying... (retry " + retryCount + ").");
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        mBluetoothSocket = null;
                        new BluetoothConnectTask(mConnection, retryCount).execute();
                        BluetoothConnectTask.this.cancel(true);
                    } else {
                        retryCount = 0;
                        System.out.println("Error connecting, Aborting!");
                        mConnection.notifyObservers(Connection.ObserverCues.ConnectFailed);
                    }
                }
            }
        }
    }

    private class BluetoothDisconnectTask extends AsyncTask<Void, Void, Void> {

        private BluetoothConnection mConnection;

        public BluetoothDisconnectTask(BluetoothConnection connection) {
            mConnection = Preconditions.checkNotNull(connection);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(mConnection.isConnected()) {
                try {
                    mConnection.getBluetoothSocket().close();
                } catch(IOException e) {
                    e.printStackTrace();
                    throw new IllegalStateException("Error closing BT socket at disconnect!");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
            if(!mConnection.isConnected()) {
                mConnection.notifyObservers(Connection.ObserverCues.Disconnected);
            }
        }
    }

    private class ReceiveInputThread extends Thread {

        private BluetoothConnection mConnection;
        private boolean isRunning;

        public ReceiveInputThread(BluetoothConnection connection) {
            mConnection = Preconditions.checkNotNull(connection);
            isRunning = true;
        }

        @Override
        public void run() {
            super.run();
            String line = "";
            BufferedReader bufferedReader = null;
            while(mConnection.isConnected() && isRunning) {
                try {
                    if(bufferedReader == null) {
                        InputStream inputStream = Preconditions.checkNotNull(mConnection.getInputStream());
                        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    }
                    line = bufferedReader.readLine();

                } catch(IOException e) {
                    //Happens if the bufferedReader's stream is closed.
                    e.printStackTrace();
                }

                //Ensure that we're not just sending blank strings; can happen if connection ends.
                if(!line.equals("")) {
                    System.out.println(line);
                    BluetoothReceiveIntent receivedInputIntent = new BluetoothReceiveIntent(BluetoothConnectionService.this, TerminalActivity.class, line);
                    LocalBroadcastManager.getInstance(BluetoothConnectionService.this).sendBroadcast(receivedInputIntent);
                }

                try {
                    Thread.sleep(50);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                    isRunning = false;
                }
            }
        }
    }
}