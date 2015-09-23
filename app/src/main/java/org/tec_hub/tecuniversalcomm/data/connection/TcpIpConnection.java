package org.tec_hub.tecuniversalcomm.data.connection;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.intents.TcpIpConnectIntent;
import org.tec_hub.tecuniversalcomm.intents.TcpIpDisconnectIntent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Nick Mosher on 9/15/15.
 */
public class TcpIpConnection extends Connection implements Parcelable {

    /**
     * Required for Parcelable framework.
     */
    public static final Parcelable.Creator<TcpIpConnection> CREATOR = new Parcelable.Creator<TcpIpConnection>() {
        public TcpIpConnection createFromParcel(Parcel in) {
            return new TcpIpConnection(in);
        }

        public TcpIpConnection[] newArray(int size) {
            return new TcpIpConnection[size];
        }
    };

    /**
     * This static map holds references to sockets while they are passed
     * through the parcelable system since sockets are not parcelable.
     */
    private static Map<UUID, Socket> sockets = new HashMap<>();

    /**
     * The socket connection to the remote.
     */
    private Socket mSocket;

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
     * Reconstructs a TcpIpConnection after being sent through the
     * parcelable system.
     * @param in The Parcel to read object data from.
     */
    public TcpIpConnection(Parcel in) {
        super(Preconditions.checkNotNull(in));
        mServerIp = Preconditions.checkNotNull(in.readString());
        mServerPort = Preconditions.checkNotNull(in.readInt());
        mSocket = sockets.get(mUUID);
    }

    /**
     * Deconstructs this object into a Parcel to be sent through
     * the parcelable system.
     * @param out The parcel to send object data through.
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(mServerIp);
        out.writeInt(mServerPort);
        sockets.put(mUUID, mSocket);
    }

    /**
     * Send connect request to TcpIpConnectionService to open a TcpIpConnection
     * using this object's data.
     * @param context The context to send the intent to launch the Service.
     */
    public void connect(Context context) {
        if(!isConnected()) {
            //Build a connect intent with connection data.
            TcpIpConnectIntent connectIntent = new TcpIpConnectIntent(context, this);

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
        if(isConnected()) {
            //Build a disconnect intent with connection data.
            TcpIpDisconnectIntent disconnectIntent = new TcpIpDisconnectIntent(context, this);

            //Send disconnect intent with local broadcast manager.
            LocalBroadcastManager.getInstance(context).sendBroadcast(disconnectIntent);
        }
    }

    /**
     * Tells whether this TCP/IP Connection is actively connected.
     * @return True if connected, false otherwise.
     */
    public boolean isConnected() {
        if(mSocket != null) {
            if(!mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch(IOException ioe) {
                    System.out.println("TCP/IP Socket not connected; error closing socket!");
                    ioe.printStackTrace();
                }
            }
            return mSocket.isConnected();
        }
        return false;
    }

    /**
     * Retrieves the Output Stream if this Connection is connected and
     * the Output Stream is not null.
     * @throws java.lang.IllegalStateException If not connected.
     * @return The OutputStream to the remote device.
     */
    public OutputStream getOutputStream() {
        if(isConnected()) {
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
     * Retrieves the Input Stream if this Connection is connected and
     * the Input Stream is not null.
     * @throws java.lang.IllegalStateException If not connected.
     * @return The InputStream from the remote device.
     */
    public InputStream getInputStream() {
        if(isConnected()) {
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
        sockets.put(mUUID, mSocket);
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
}
