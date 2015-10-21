package org.tec_hub.tecuniversalcomm.data.connection;

import org.tec_hub.tecuniversalcomm.R;
import org.tec_hub.tecuniversalcomm.data.connection.intents.ConnectionIntent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Nick Mosher on 9/15/15.
 * Represents a connection to a remote device over an internet TCP/IP socket.
 *
 * @author Nick Mosher, nicholastmosher@gmail.com, https://github.com/nicholastmosher
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
     *
     * @param name The name of this Connection.
     * @param ip   The IP of the remote device.
     * @param port The port to connect over.
     */
    public TcpIpConnection(String name, String ip, int port) {
        super(name);
        mServerIp = ip; //TODO add a way to verify IP layout
        mServerPort = port; //TODO add a way to check port bounds
    }

    /**
     * Returns the current status of this Connection, verifying that the
     * status is correct.
     *
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
     *
     * @return The string "connection type" as defined by ConnectionIntent.
     */
    public String getConnectionType() {
        return ConnectionIntent.CONNECTION_TYPE_TCPIP;
    }

    /**
     * Retrieves the Input Stream if this Connection is connected and
     * the Input Stream is not null.
     *
     * @return The InputStream from the remote device.
     * @throws java.lang.IllegalStateException If not connected.
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
     *
     * @return The OutputStream to the remote device.
     * @throws java.lang.IllegalStateException If not connected.
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

    @Override
    public int getImageResourceId() {
        return R.drawable.ic_wifi_black_48dp;
    }

    /**
     * Returns the IP address of the remote (server) device.
     *
     * @return The IP address of the remote (server) device.
     */
    public String getServerIp() {
        return mServerIp;
    }

    /**
     * Returns the port over which connection will be established.
     *
     * @return The port to establish connection over.
     */
    public int getServerPort() {
        return mServerPort;
    }

    /**
     * Sets the socket for this connection to use.
     *
     * @param socket The socket to use to connect.
     */
    public void setSocket(Socket socket) {
        if(socket == null) {
            new NullPointerException("Socket is null!").printStackTrace();
        } else {
            mSocket = socket;
        }
    }

    /**
     * Gets the socket used by this connection.
     *
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
