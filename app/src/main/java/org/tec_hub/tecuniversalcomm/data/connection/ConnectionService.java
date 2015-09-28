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
import org.tec_hub.tecuniversalcomm.intents.DataReceivedIntent;
import org.tec_hub.tecuniversalcomm.intents.TECIntent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nick Mosher on 4/23/15.
 */
public class ConnectionService extends Service implements ConnectionObserver {

    private static boolean launched = false;

    private Map<Connection, ReceiveDataThread> receiveThreads;

    public void onCreate() {
        launched = true;
        receiveThreads = new HashMap<>();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TECIntent.ACTION_BLUETOOTH_CONNECT);
        intentFilter.addAction(TECIntent.ACTION_BLUETOOTH_DISCONNECT);
        intentFilter.addAction(TECIntent.ACTION_BLUETOOTH_SEND_DATA);

        intentFilter.addAction(TECIntent.ACTION_TCPIP_CONNECT);
        intentFilter.addAction(TECIntent.ACTION_TCPIP_DISCONNECT);
        intentFilter.addAction(TECIntent.ACTION_TCPIP_SEND_DATA);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent == null) {
                    System.out.println("Received intent is null!");
                    return;
                }

                Connection connection = Connection.getConnection(intent.getStringExtra(TECIntent.CONNECTION_UUID));

                //Safety to make sure we don't get null pointer exceptions.
                if(connection == null) {
                    System.err.println("Received connection is null.");
                    return;
                }

                switch(intent.getAction()) {

                    //Received action to establish bluetooth connection
                    case TECIntent.ACTION_BLUETOOTH_CONNECT:

                        //Create callbacks for successful connection and disconnection
                        connection.addObserver(ConnectionService.this);

                        //Initiate connecting
                        new ConnectBluetoothTask((BluetoothConnection) connection).execute();
                        break;

                    //Received action to disconnect bluetooth
                    case TECIntent.ACTION_BLUETOOTH_DISCONNECT:

                        //Initiate disconnecting
                        new DisconnectBluetoothTask((BluetoothConnection) connection).execute();
                        break;

                    //Received intent with data to send over bluetooth
                    case TECIntent.ACTION_BLUETOOTH_SEND_DATA:
                        //System.out.println("Service -> Sending Data...");
                        String btData = intent.getStringExtra(TECIntent.BLUETOOTH_TO_SEND_DATA);
                        sendData(connection, btData);
                        break;

                    //Received action to establish tcpip connection.
                    case TECIntent.ACTION_TCPIP_CONNECT:

                        //Create callbacks for successful connection and disconnection
                        connection.addObserver(ConnectionService.this);

                        //Initiate connecting.
                        new ConnectTcpIpTask((TcpIpConnection) connection).execute();
                        break;

                    //Received action to disconnect tcpip connection.
                    case TECIntent.ACTION_TCPIP_DISCONNECT:

                        //Disconnect.
                        new DisconnectTcpIpTask((TcpIpConnection) connection).execute();
                        break;

                    //Received action to send data over tcpip.
                    case TECIntent.ACTION_TCPIP_SEND_DATA:
                        String tcpData = intent.getStringExtra(TECIntent.TCPIP_TO_SEND_DATA);
                        sendData(connection, tcpData);
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
     * Launches the ConnectionService if it is not already active.
     * @param context The context to launch the Service from.
     */
    public static void launch(Context context) {
        if(!launched) {
            context.startService(new Intent(context, ConnectionService.class));
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isLaunched() {
        return launched;
    }

    /**
     * Connections are susceptible to change.  They can connect or disconnect,
     * drop, etc. so we need to know when those changes happen.  This method
     * is called by each connection we've subscribed to whenever something
     * changes.
     * @param observable The connection we're observing that's changed.
     * @param status The status given by the connection during the change.
     */
    @Override
    public void onUpdate(Connection observable, Connection.Status status) {

        //If the update came from a BluetoothConnection, handle it accordingly.
        switch (status) {
            case Connected:
                ReceiveDataThread newThread;
                if(receiveThreads.containsKey(observable)) {
                    newThread = receiveThreads.get(observable);
                    newThread.interrupt();
                } else {
                    newThread = new ReceiveDataThread(observable);
                    receiveThreads.put(observable, new ReceiveDataThread(observable));
                }
                newThread.start();
                break;

            case Disconnected:
                ReceiveDataThread oldThread;
                if(receiveThreads.containsKey(observable)) {
                    oldThread = receiveThreads.get(observable);
                    oldThread.interrupt();
                    receiveThreads.remove(observable);
                }

                //Since we're disconnected, we no longer need to watch that connection.
                //observable.deleteObserver(this); TODO Check if this is necessary
                break;
            case ConnectFailed:
                break;
            default:
        }
    }

    private void sendData(Connection connection, String data) {
        if(!data.equals("")) {
            sendData(connection, data.getBytes());
        } else {
            System.out.println("Data to send is blank!");
        }
    }

    private void sendData(Connection connection, byte[] data) {
        Preconditions.checkNotNull(connection);
        Preconditions.checkNotNull(data);

        if(connection.getStatus().equals(Connection.Status.Connected)) {
            try {
                connection.getOutputStream().write(data);
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Connection is not connected!");
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
            mConnection = Preconditions.checkNotNull(connection);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothSocket = null;
            retryCount = retry;
        }

        public ConnectBluetoothTask(BluetoothConnection connection) {
            this(connection, 0);
        }

        /**
         * This method runs on a separate, non-UI thread.  Heavy lifting goes here.
         * @param params
         * @return True if connecting succeeded, false if it failed.
         */
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

        /**
         * Result method that runs on the UI thread.  Background thread reports
         * to this thread when it's finished.
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
            mConnection = Preconditions.checkNotNull(connection);
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
     * Opens a background thread that continuously monitors for input on a connection.
     */
    private class ReceiveDataThread extends Thread {

        private Connection mConnection;
        private boolean isRunning;

        public ReceiveDataThread(Connection connection) {
            mConnection = Preconditions.checkNotNull(connection);
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
                        InputStream inputStream = Preconditions.checkNotNull(mConnection.getInputStream());
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
                    DataReceivedIntent receivedInputIntent = new DataReceivedIntent(ConnectionService.this, TerminalActivity.class, line);
                    receivedInputIntent.putExtra(TECIntent.CONNECTION_TYPE, mConnection.getConnectionType());
                    receivedInputIntent.putExtra(TECIntent.CONNECTION_UUID, mConnection.getUUID());

                    //Send the data back to any listeners.
                    LocalBroadcastManager.getInstance(ConnectionService.this).sendBroadcast(receivedInputIntent);
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