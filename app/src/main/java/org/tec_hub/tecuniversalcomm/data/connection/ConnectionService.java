package org.tec_hub.tecuniversalcomm.data.connection;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import org.tec_hub.tecuniversalcomm.MainActivity;
import org.tec_hub.tecuniversalcomm.data.connection.intents.ConnectionIntent;
import org.tec_hub.tecuniversalcomm.data.connection.intents.DataReceiveIntent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nick Mosher on 4/23/15.
 * The ConnectionService handles all heavy networking operations with Connections
 * such as connecting, disconnecting, sending, and receiving data.  The
 * ConnectionService multiplexes tasks with all types of Connections, as well as
 * multiple instances of the same type of Connection (e.g. a TcpIpConnection and
 * a BluetoothConnection, or multiple BluetoothConnections, or any combination).
 * <p/>
 * All data flowing into and out of the ConnectionService is passed through custom
 * intents, with data Extras assigned via keys located in ConnectionIntent.  Most
 * typical operations such as Connecting, Disconnecting, Sending, and Receiving
 * have custom intents representing them (ConnectIntent, DisconnectIntent,
 * SendIntent, and ReceiveIntent, respectively).
 */
public class ConnectionService extends Service {

    private static boolean launched = false;

    private Map<Connection, ReceiveDataThread> receiveThreads;

    private UsbManager mUsbManager;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        launched = true;
        receiveThreads = new HashMap<>();
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        //Listen for broadcasts regarding connect and disconnect.
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectionIntent.ACTION_CONNECT);
        filter.addAction(ConnectionIntent.ACTION_DISCONNECT);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent == null) {
                    System.out.println("Received intent is null!");
                    return;
                }

                Connection connection = Connection.getConnection(intent.getStringExtra(ConnectionIntent.CONNECTION_UUID));

                //Safety to make sure we don't get null pointer exceptions.
                if(connection == null) {
                    System.err.println("Received connection is null.");
                    return;
                }

                //Launch different handlers based on the type of connection.
                switch(connection.getConnectionType()) {
                    //If it's a BluetoothConnection.
                    case ConnectionIntent.CONNECTION_TYPE_BLUETOOTH: {
                        //Launch different handlers based on the action specified.
                        switch(intent.getAction()) {
                            //If it's a connect action.
                            case ConnectionIntent.ACTION_CONNECT: {
                                new ConnectBluetoothTask((BluetoothConnection) connection).execute();
                            }
                            break;
                            //If it's a disconnect action.
                            case ConnectionIntent.ACTION_DISCONNECT: {
                                new DisconnectBluetoothTask((BluetoothConnection) connection).execute();
                            }
                            break;
                            default:
                        }
                    }
                    break;
                    //If it's a TcpIpConnection.
                    case ConnectionIntent.CONNECTION_TYPE_TCPIP: {
                        //Launch different handlers based on the action specified.
                        switch(intent.getAction()) {
                            //If it's a connect action.
                            case ConnectionIntent.ACTION_CONNECT: {
                                new ConnectTcpIpTask((TcpIpConnection) connection).execute();
                            }
                            break;
                            //If it's a disconnect action.
                            case ConnectionIntent.ACTION_DISCONNECT: {
                                new ConnectTcpIpTask((TcpIpConnection) connection).execute();
                            }
                            break;
                            default:
                        }
                    }
                    break;
                    //If it's a UsbHostConnection
                    case ConnectionIntent.CONNECTION_TYPE_USB: {
                        //Launch different handlers based on the action specified.
                        switch(intent.getAction()) {
                            //If it's a connect action.
                            case ConnectionIntent.ACTION_CONNECT: {
                                new ConnectUsbTask((UsbHostConnection) connection).execute();
                            }
                            break;
                            //If it's a disconnect action.
                            case ConnectionIntent.ACTION_DISCONNECT: {
                                new DisconnectUsbTask((UsbHostConnection) connection).execute();
                            }
                            break;
                            default:
                        }
                    }
                    break;
                    default:
                }
            }
        }, filter);

        /*
         * This Filter/Receiver pair listens for requests to send data over some
         * Connection.
         */
        IntentFilter sendFilter = new IntentFilter();
        sendFilter.addAction(ConnectionIntent.ACTION_SEND_DATA);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent == null) {
                    System.out.println("Received Send Intent");
                }
            }
        }, sendFilter);

        /*
         * This Filter/Receiver pair listens for an Android system broadcast
         * notifying about a newly attached USB device.  When a valid device
         * is attached, create a new UsbConnection and pass it wherever it's
         * needed.
         */
        IntentFilter usbAttachedFilter = new IntentFilter();
        usbAttachedFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                System.out.println("USB Device Attached!");
                System.out.println("USB Name: " + usbDevice.getDeviceName());
                System.out.println("USB Class: " + usbDevice.getDeviceClass());
                System.out.println("USB ID: " + usbDevice.getDeviceId());
                System.out.println("USB Protocol: " + usbDevice.getDeviceProtocol());
                System.out.println("USB Subclass: " + usbDevice.getDeviceSubclass());
            }
        }, usbAttachedFilter);

        /*
         * This Filter/Receiver pair listens for an Android system broadcast
         * notifying that a USB device has been detached.  When this happens,
         * we need to close any open connections we may have had with that
         * device.
         */
        IntentFilter usbDetachedFilter = new IntentFilter();
        usbDetachedFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Detach here
            }
        }, usbDetachedFilter);

        return Service.START_STICKY;
    }

    public void onDestroy() {
        launched = false;
    }

    /**
     * Launches the ConnectionService if it is not already active.
     *
     * @param context The context to launch the Service from.
     */
    public static void launch(Context context) {
        if(!launched) {
            context.startService(new Intent(context, ConnectionService.class));
        }
    }

    /**
     * Opens an asynchronous task that does not run on the UI thread
     * to handle opening BluetoothConnections.
     * Usage: new ConnectBluetoothTask(myBluetoothConnection).execute();
     */
    private static class ConnectBluetoothTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothConnection mConnection;
        private BluetoothAdapter mBluetoothAdapter;
        private BluetoothSocket mBluetoothSocket;
        private static int retryCount;

        private ConnectBluetoothTask(BluetoothConnection connection, int retry) {
            if(connection == null) {
                throw new NullPointerException("Connection is null!");
            }
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothSocket = null;
            retryCount = retry;
        }

        public ConnectBluetoothTask(BluetoothConnection connection) {
            this(connection, 0);
        }

        /**
         * This method runs on a separate, non-UI thread.  Heavy lifting goes here.
         *
         * @param params
         * @return True if connecting succeeded, false if it failed.
         */
        protected Boolean doInBackground(Void... params) {
            //Check if BT is enabled
            if(!mBluetoothAdapter.isEnabled()) {
                System.out.println("Bluetooth not enabled!"); //TODO better handling.
                new IllegalStateException("Cannot connect, Bluetooth is disabled!").printStackTrace();
            }
            System.out.println("BluetoothAdapter Enabled...");

            //Define a BluetoothDevice with the address from our Connection.
            String address = mConnection.getAddress();
            BluetoothDevice device;
            if(address != null && BluetoothAdapter.checkBluetoothAddress(address)) {
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

            } catch(IOException e) {
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
            } catch(IOException e) {
                e.printStackTrace();
                try {
                    mBluetoothSocket.close();
                } catch(IOException e2) {
                    e2.printStackTrace();
                }
                return false;
            }

            //If we've made it this far, must have been a success.
            return true;
        }

        /**
         * Result method that runs on the UI thread.  Background thread reports
         * to this thread when it's finished.
         *
         * @param success Whether the background thread succeeded or failed.
         */
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if(success) {
                System.out.println("Connected success");
                mConnection.notifyObservers(Connection.Status.Connected);
            } else {
                System.out.println("Connected failed");
                if(mBluetoothSocket.isConnected()) {
                    System.out.println("WARNING: ConnectBluetoothTask reported error, but is connected.");
                    mConnection.notifyObservers(Connection.Status.Connected);
                } else {
                    if(retryCount < 3) {
                        retryCount++;
                        System.out.println("Error connecting! Retrying... (retry " + retryCount + ").");
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        mBluetoothSocket = null;
                        new ConnectBluetoothTask(mConnection, retryCount).execute();
                        ConnectBluetoothTask.this.cancel(true);
                    } else {
                        retryCount = 0;
                        System.out.println("Error connecting, Aborting!");
                        mConnection.notifyObservers(Connection.Status.ConnectFailed);
                    }
                }
            }
        }
    }

    /**
     * Uses an asynchronous task not on the UI thread to close BluetoothConnections.
     * Usage: new DisconnectBluetoothTask(myBluetoothConnection).execute();
     */
    private class DisconnectBluetoothTask extends AsyncTask<Void, Void, Void> {

        private BluetoothConnection mConnection;

        public DisconnectBluetoothTask(BluetoothConnection connection) {
            if(connection == null) {
                throw new NullPointerException("Connection is null!");
            }
            mConnection = connection;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(mConnection.getStatus().equals(Connection.Status.Connected)) {
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
            if(!mConnection.getStatus().equals(Connection.Status.Connected)) {
                mConnection.notifyObservers(Connection.Status.Disconnected);
            }
        }
    }

    /**
     * Uses an asynchronous task not on the UI thread to open a TCPIP connection.
     * Usage: new ConnectTcpIpTask(myTcpIpConnection).execute();
     */
    private static class ConnectTcpIpTask extends AsyncTask<Void, Void, Boolean> {

        private TcpIpConnection mConnection;
        private Socket mSocket;
        private static int retryCount;

        private ConnectTcpIpTask(TcpIpConnection connection, int retry) {
            if(connection == null) {
                throw new NullPointerException("Connection is null!");
            }
            mConnection = connection;
            retryCount = retry;
        }

        public ConnectTcpIpTask(TcpIpConnection connection) {
            this(connection, 0);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                System.out.println("Connecting to " + mConnection.getServerIp() + ":" + mConnection.getServerPort());
                mSocket = new Socket(mConnection.getServerIp(), mConnection.getServerPort());
                mConnection.setSocket(mSocket);
            } catch(IOException ioe) {
                ioe.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if(success) {
                System.out.println("Connected success");
                mConnection.notifyObservers(Connection.Status.Connected);
            } else if(mSocket != null) {
                System.out.println("Connected failed");
                if(mSocket.isConnected()) {
                    System.out.println("WARNING: ConnectBluetoothTask reported error, but is connected.");
                    mConnection.notifyObservers(Connection.Status.Connected);
                } else {
                    if(retryCount < 3) {
                        retryCount++;
                        System.out.println("Error connecting! Retrying... (retry " + retryCount + ").");
                        mSocket = null;
                        new ConnectTcpIpTask(mConnection, retryCount).execute();
                        ConnectTcpIpTask.this.cancel(true);
                    } else {
                        retryCount = 0;
                        System.out.println("Error connecting, Aborting!");
                        mConnection.notifyObservers(Connection.Status.ConnectFailed);
                    }
                }
            } else {
                System.err.println("Error, TcpIp socket is null");
                mConnection.notifyObservers(Connection.Status.ConnectFailed);
            }
        }
    }

    /**
     * Uses an asynchronous task not on the UI thread to close a TcpIpConnection.
     * Usage: new DisconnectTcpIpTask(myTcpIpConnection).execute();
     */
    private class DisconnectTcpIpTask extends AsyncTask<Void, Void, Void> {

        private TcpIpConnection mConnection;

        public DisconnectTcpIpTask(TcpIpConnection connection) {
            mConnection = connection;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(mConnection.getStatus().equals(Connection.Status.Connected)) {
                try {
                    mConnection.getSocket().close();
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                    throw new IllegalStateException("Error closing socket at disconnect!");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
            if(!mConnection.getStatus().equals(Connection.Status.Connected)) {
                mConnection.notifyObservers(Connection.Status.Disconnected);
            }
        }
    }

    /**
     * Uses an asynchronous task not on the UI thread to open a UsbHostConnection.
     * Usage: new ConnectUsbTask(myUsbHostConnection).execute();
     */
    private class ConnectUsbTask extends AsyncTask<Void, Void, Boolean> {

        private UsbHostConnection mConnection;

        public ConnectUsbTask(UsbHostConnection connection) {
            mConnection = connection;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            UsbDevice usbDevice = mConnection.getUsbDevice();
            UsbInterface usbInterface = usbDevice.getInterface(0);
            UsbDeviceConnection usbDeviceConnection = mUsbManager.openDevice(usbDevice);

            //Force-claim the interface for this usb connection.  Will release at disconnect.
            usbDeviceConnection.claimInterface(usbInterface, true);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }

    /**
     * Uses an asynchronous task not on the UI thread to close a UsbHostConnection.
     * Usage: new DisconnectUsbTask(myUsbHostConnection).execute();
     */
    private class DisconnectUsbTask extends AsyncTask<Void, Void, Void> {

        private UsbHostConnection mConnection;

        public DisconnectUsbTask(UsbHostConnection connection) {
            mConnection = connection;
        }

        @Override
        protected Void doInBackground(Void... params) {

            UsbDevice usbDevice = mConnection.getUsbDevice();
            UsbInterface usbInterface = usbDevice.getInterface(0);
            UsbDeviceConnection usbDeviceConnection = mUsbManager.openDevice(usbDevice);

            //Releases our claim on this usb connection.
            usbDeviceConnection.releaseInterface(usbInterface);

            return null;
        }
    }

    /**
     * Uses an asynchronous task not on the UI thread to send data over a Connection.
     * Usage: new SendDataTask(myConnection, myData).execute();
     */
    private class SendDataTask extends AsyncTask<Void, Void, Boolean> {

        private Connection mConnection;
        private byte[] mData;

        public SendDataTask(Connection connection, byte[] data) {
            if(connection == null) {
                throw new NullPointerException("[ConnectionService.sendData()] Connection is null!");
            } else if(data == null) {
                throw new NullPointerException("[ConnectionService.sendData()] Data is null!");
            }
            mConnection = connection;
            mData = data;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(mConnection.getStatus().equals(Connection.Status.Connected)) {
                try {
                    mConnection.getOutputStream().write(mData);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Connection is not connected!");
            }
            return null;
        }
    }

    /**
     * Opens a background thread that continuously monitors for input on a connection.
     */
    private class ReceiveDataThread extends Thread {

        private Connection mConnection;
        private boolean isRunning;

        public ReceiveDataThread(Connection connection) {
            if(connection == null) {
                throw new NullPointerException("Connection is null!");
            }
            isRunning = true;
        }

        @Override
        public void run() {
            super.run();
            String line = "";
            BufferedReader bufferedReader = null;
            while((mConnection.getStatus().equals(Connection.Status.Connected)) && isRunning) {
                try {
                    if(bufferedReader == null) {
                        InputStream inputStream = mConnection.getInputStream();
                        if(inputStream == null) {
                            throw new NullPointerException("Input Stream is null!");
                        }
                        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    }
                    //Fun fact, if the input stream disconnects, readLine() decides to return null :/
                    line = bufferedReader.readLine();

                } catch(IOException e) {
                    //Happens if the bufferedReader's stream is closed.
                    e.printStackTrace();
                }

                //Ensure that we're not just sending blank strings; can happen if connection ends.
                if(line != null && !line.equals("")) {

                    System.out.println(line);
                    new DataReceiveIntent(ConnectionService.this, MainActivity.class, mConnection, line.getBytes()).sendLocal();
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