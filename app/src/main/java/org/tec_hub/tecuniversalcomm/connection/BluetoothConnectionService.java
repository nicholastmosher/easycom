package org.tec_hub.tecuniversalcomm.connection;

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
import org.tec_hub.tecuniversalcomm.TECIntent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by Nick Mosher on 4/23/15.
 */
public class BluetoothConnectionService extends Service {

    private static boolean launched = false;

    public void onCreate() {
        launched = true;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TECIntent.ACTION_BLUETOOTH_CONNECT);
        intentFilter.addAction(TECIntent.ACTION_BLUETOOTH_DISCONNECT);
        intentFilter.addAction(TECIntent.ACTION_BLUETOOTH_UPDATE_INPUT);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Received broadcast");
                final BluetoothConnection bluetoothConnection = intent.getParcelableExtra(TECIntent.BLUETOOTH_CONNECTION_DATA);
                switch(intent.getAction()) {

                    //Received action to establish connection
                    case TECIntent.ACTION_BLUETOOTH_CONNECT:
                        //Create callbacks for successful connection and disconnection
                        bluetoothConnection.setOnConnectStatusChangedListener(
                                BluetoothConnectionService.this,
                                new Connection.OnConnectStatusChangedListener() {
                                    @Override
                                    public void onConnect() {
                                        System.out.println("Service -> onConnect");
                                        new ReceiveInputTask(bluetoothConnection).execute();
                                    }

                                    @Override
                                    public void onDisconnect() {
                                        System.out.println("Service -> onDisconnect");
                                    }
                                });
                        //Initiate connecting
                        new BluetoothConnectTask(bluetoothConnection).execute();
                        break;

                    //Received action to disconnect
                    case TECIntent.ACTION_BLUETOOTH_DISCONNECT:
                        //Initiate disconnecting
                        new BluetoothDisconnectTask(bluetoothConnection).execute();
                        break;
                    default:
                }
            }
        }, intentFilter);

        return Service.START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isLaunched() {
        return launched;
    }

    private class BluetoothConnectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothConnection mConnection;
        private BluetoothAdapter mBluetoothAdapter;
        private BluetoothSocket mBluetoothSocket;

        public BluetoothConnectTask(BluetoothConnection connection) {
            mConnection = Preconditions.checkNotNull(connection);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        protected Boolean doInBackground(Void... params) {
            //Check if BT is enabled
            if (!mBluetoothAdapter.isEnabled()) {
                System.out.println("Bluetooth not enabled!"); //TODO better handling.
            }

            //Define a BluetoothDevice with the address from our Connection.
            String address = mConnection.getAddress();
            BluetoothDevice device;
            if(address != null  && BluetoothAdapter.checkBluetoothAddress(address)) {
                device = mBluetoothAdapter.getRemoteDevice(address);
            } else {
                return false;
            }

            //Try to retrieve a BluetoothSocket from the BluetoothDevice.
            try {
                mBluetoothSocket = device.createRfcommSocketToServiceRecord(BluetoothConnection.BLUETOOTH_SERIAL_UUID);
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
            mConnection.notifyConnected();
        }
    }

    private class BluetoothDisconnectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothConnection mConnection;

        public BluetoothDisconnectTask(BluetoothConnection connection) {
            mConnection = Preconditions.checkNotNull(connection);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = true;
            if(mConnection.isConnected()) {
                try {
                    mConnection.getBluetoothSocket().close();
                } catch(IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }

            OutputStream outputStream;
            if((outputStream = mConnection.getOutputStream()) != null) {
                try {
                    outputStream.close();
                } catch(IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }

            InputStream inputStream;
            if((inputStream = mConnection.getInputStream()) != null) {
                try {
                    inputStream.close();
                } catch(IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if(success) {
                mConnection.notifyDisconnected();
            }
        }
    }

    private class ReceiveInputTask extends AsyncTask<Void, String, Void> {

        private BluetoothConnection mConnection;

        public ReceiveInputTask(BluetoothConnection connection) {
            mConnection = Preconditions.checkNotNull(connection);
        }

        @Override
        protected Void doInBackground(Void... params) {
            String line = "";
            BufferedReader bufferedReader = null;
            while(mConnection.isConnected()) {
                try {
                    if(bufferedReader == null) {
                        InputStream inputStream = Preconditions.checkNotNull(mConnection.getInputStream());
                        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    }
                    line = bufferedReader.readLine();

                } catch(IOException e) {
                    e.printStackTrace();
                }
                System.out.println(line);
                publishProgress(line);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            //Construct data intent
            Intent receivedInputIntent = new Intent(BluetoothConnectionService.this, TerminalActivity.class);
            receivedInputIntent.setAction(TECIntent.ACTION_BLUETOOTH_UPDATE_INPUT);
            receivedInputIntent.putExtra(TECIntent.BLUETOOTH_RECEIVED_DATA, values[0]);

            //Send data intent
            LocalBroadcastManager.getInstance(BluetoothConnectionService.this).sendBroadcast(receivedInputIntent);
        }
    }
}
