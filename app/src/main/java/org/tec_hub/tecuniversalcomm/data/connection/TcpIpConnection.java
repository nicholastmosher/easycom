package org.tec_hub.tecuniversalcomm.data.connection;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.intents.TECIntent;
import org.tec_hub.tecuniversalcomm.intents.TcpIpConnectIntent;
import org.tec_hub.tecuniversalcomm.intents.TcpIpDisconnectIntent;
import org.tec_hub.tecuniversalcomm.intents.TcpIpSendIntent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Nick Mosher on 9/15/15.
 */
public class TcpIpConnection extends Connection {

    /**
     * The socket connection to the remote.
     */
    private transient Socket mSocket;

    /**
     * The IP address of the remote device.
     */
    private String mServerIp;

    /**
     * The port to connect to the remote over.
     */
    private int mServerPort;

    /**
     * Constructs a new TcpIpConnection with a given remote IP and port.
     * @param name The name of this Connection.
     * @param ip The IP of the remote device.
     * @param port The port to connect over.
     */
    public TcpIpConnection(String name, String ip, int port) {
        super(name);
        mServerIp = ip; //TODO add a way to verify IP layout
        mServerPort = port; //TODO add a way to check port bounds
    }

    /**
     * No-argument constructor made private so that Gson can correctly
     * build this object and then populate the members with Json data.
     */
    protected TcpIpConnection() {
        mServerIp = null;
        mServerPort = -1;
    }

    /**
     * Send connect request to TcpIpConnectionService to open a TcpIpConnection
     * using this object's data.
     * @param context The context to send the intent to launch the Service.
     */
    public void connect(Context context) {
        if(!(getStatus().equals(Status.Connected))) {
            //Build a connect intent with connection data.
            TcpIpConnectIntent connectIntent = new TcpIpConnectIntent(context, mUUID);

            //Send connect intent with local broadcast manager.
            LocalBroadcastManager.getInstance(context).sendBroadcast(connectIntent);
        }
    }

    /**
     * Send disconnect request to TcpIpConnectionService to close a TcpIpConnection
     * using this object's data.
     * @param context The context to send the intent to launch the Service.
     */
    public void disconnect(Context context) {
        if(getStatus().equals(Status.Connected)) {
            //Build a disconnect intent with connection data.
            TcpIpDisconnectIntent disconnectIntent = new TcpIpDisconnectIntent(context, mUUID);

            //Send disconnect intent with local broadcast manager.
            LocalBroadcastManager.getInstance(context).sendBroadcast(disconnectIntent);
        }
    }

    /**
     * Returns the current status of this Connection, verifying that the
     * status is correct.
     * @return The connectivity status.
     */
    public Status getStatus() {
        if(mSocket != null) {
            if(!mSocket.isConnected()) {
                if(!mSocket.isClosed()) {
                    try {
                        mSocket.close();
                    } catch(IOException ioe) {
                        System.out.println("TCP/IP Socket not connected; error closing socket!");
                        ioe.printStackTrace();
                    }
                }
                //If we still think we're connected, set status to disconnected.
                mStatus = (mStatus.equals(Status.Connected)) ? Status.Disconnected : mStatus;
            } else {
                //This is the only case in which we SHOULD be connected.
                return (mStatus = Status.Connected);
            }
        }
        //As long as the current status ISN'T 'connected', return current status.
        return (mStatus != Status.Connected) ? mStatus : Status.Disconnected;
    }

    /**
     * Convenience method for use with intent extra "CONNECTION_TYPE".
     * @return The string "connection type" as defined by TECIntent.
     */
    public String getConnectionType() {
        return TECIntent.CONNECTION_TYPE_TCPIP;
    }

    /**
     * Retrieves the Input Stream if this Connection is connected and
     * the Input Stream is not null.
     * @throws java.lang.IllegalStateException If not connected.
     * @return The InputStream from the remote device.
     */
    public InputStream getInputStream() {
        if(getStatus().equals(Status.Connected)) {
            try {
                return mSocket.getInputStream();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            throw new IllegalStateException("Connection is not active!");
        }
        return null;
    }

    /**
     * Retrieves the Output Stream if this Connection is connected and
     * the Output Stream is not null.
     * @throws java.lang.IllegalStateException If not connected.
     * @return The OutputStream to the remote device.
     */
    public OutputStream getOutputStream() {
        if(getStatus().equals(Status.Connected)) {
            try {
                return mSocket.getOutputStream();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            throw new IllegalStateException("Connection is not active!");
        }
        return null;
    }

    /**
     * Sends the given data over this connection.
     * @param context The context to send the intent from.
     * @param data The data to send.
     */
    public void sendData(Context context, byte[] data) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new TcpIpSendIntent(context, getUUID(), data));
    }

    /**
     * Returns the IP address of the remote (server) device.
     * @return The IP address of the remote (server) device.
     */
    public String getServerIp() {
        return mServerIp;
    }

    /**
     * Returns the port over which connection will be established.
     * @return The port to establish connection over.
     */
    public int getServerPort() {
        return mServerPort;
    }

    /**
     * Sets the socket for this connection to use.
     * @param socket The socket to use to connect.
     */
    public void setSocket(Socket socket) {
        Preconditions.checkNotNull(socket);
        mSocket = socket;
    }

    /**
     * Gets the socket used by this connection.
     * @return The socket used by this connection.
     * @throws NullPointerException If the socket is null.
     */
    public Socket getSocket() throws NullPointerException {
        if(mSocket != null) {
            return mSocket;
        } else {
            throw new NullPointerException("TCP/IP socket is null!");
        }
    }

    @Override
    public String toString() {
        return mName + ", " + mServerIp + ":" + mServerPort;
    }
}
