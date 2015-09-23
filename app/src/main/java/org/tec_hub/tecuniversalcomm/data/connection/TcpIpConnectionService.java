package org.tec_hub.tecuniversalcomm.data.connection;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.intents.TECIntent;

import java.io.IOException;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Nick Mosher on 9/15/15.
 */
public class TcpIpConnectionService extends Service implements Observer {

    private static boolean launched = false;

    public void onCreate() {
        launched = true;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TECIntent.ACTION_TCPIP_CONNECT);
        intentFilter.addAction(TECIntent.ACTION_TCPIP_DISCONNECT);
        intentFilter.addAction(TECIntent.ACTION_TCPIP_SEND_DATA);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                final TcpIpConnection connection = intent.getParcelableExtra(TECIntent.TCPIP_CONNECTION_DATA);
                Preconditions.checkNotNull(connection);

                switch(intent.getAction())
                {
                    //Received an action to establish wifi communication.
                    case TECIntent.ACTION_TCPIP_CONNECT:

                        break;
                    //Received an action to disconnect wifi communication.
                    case TECIntent.ACTION_TCPIP_DISCONNECT:

                        break;
                    //Received an action to send data over a wifi connection.
                    case TECIntent.ACTION_TCPIP_SEND_DATA:

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

    public static void launch(Context context) {
        if(!launched) {
            context.startService(new Intent(context, TcpIpConnectionService.class));
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (!(observable instanceof TcpIpConnection)) {
            throw new IllegalStateException("Update did not originate at a TcpIpConnection");
        }

        if (data instanceof Connection.ObserverCues) {
            TcpIpConnection connection = (TcpIpConnection) observable;
            Connection.ObserverCues cue = (Connection.ObserverCues) data;
            switch (cue) {
                case Connected:

                    break;
                case Disconnected:

                    break;
                case ConnectFailed:

                    break;
                default:
            }
        }
    }

    private void sendData(TcpIpConnection connection, String data) {
        if(!data.equals("")) {
            sendData(connection, data.getBytes());
        } else {
            System.out.println("Data to send is blank!");
        }
    }

    private void sendData(TcpIpConnection connection, byte[] data) {
        Preconditions.checkNotNull(connection);
        Preconditions.checkNotNull(data);

        if(connection.isConnected()) {
            try {
                connection.getOutputStream().write(data);
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            System.out.println("Connection is not connected!");
        }
    }

    private static class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        private TcpIpConnection mConnection;
        private Socket mSocket;
        private static int retryCount;

        private ConnectTask(TcpIpConnection connection, int retry) {
            mConnection = connection;
            retryCount = retry;
        }

        public ConnectTask(TcpIpConnection connection) {
            this(connection, 0);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
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
                mConnection.notifyObservers(Connection.ObserverCues.Connected);
            } else {
                System.out.println("Connected failed");
                if(mSocket.isConnected()) {
                    System.out.println("WARNING: ConnectTask reported error, but is connected.");
                    mConnection.notifyObservers(Connection.ObserverCues.Connected);
                } else {
                    if(retryCount < 3) {
                        retryCount++;
                        System.out.println("Error connecting! Retrying... (retry " + retryCount + ").");
                        mSocket = null;
                        new ConnectTask(mConnection, retryCount).execute();
                        ConnectTask.this.cancel(true);
                    } else {
                        retryCount = 0;
                        System.out.println("Error connecting, Aborting!");
                        mConnection.notifyObservers(Connection.ObserverCues.ConnectFailed);
                    }
                }
            }
        }
    }

    private class DisconnectTask extends AsyncTask<Void, Void, Void> {

        private TcpIpConnection mConnection;

        public DisconnectTask(TcpIpConnection connection) {
            mConnection = connection;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(mConnection.isConnected()) {
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
            if(!mConnection.isConnected()) {
                mConnection.notifyObservers(Connection.ObserverCues.Disconnected);
            }
        }
    }
}
