package org.tec_hub.tecuniversalcomm.data.connection;

import android.content.Context;

import org.tec_hub.tecuniversalcomm.data.connection.intents.ConnectIntent;
import org.tec_hub.tecuniversalcomm.data.connection.intents.DataSendIntent;
import org.tec_hub.tecuniversalcomm.data.connection.intents.DisconnectIntent;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.UUID;

/**
 * Created by Nick Mosher on 3/3/2015.
 */
public abstract class Connection {

    private transient List<Observer> mObservers = new ArrayList<>();

    public enum Status {
        Connected,
        Disconnected,
        Connecting,
        ConnectFailed,
        ConnectCanceled,
        DataChanged
    }

    /**
     * Static maps stores all constructed connections.  This way we
     * can reference them from different activities without needing
     * to pass through the Parcelable framework.
     */
    protected static transient Map<UUID, Connection> connections = new HashMap<>();

    /**
     * The immutable name of this Connection.
     */
    protected final String mName;

    /**
     * A unique identifier for this Connection, used as a reliable key
     * for storing and retrieving data from static Maps.
     */
    protected final UUID mUUID;

    /**
     * Keeps track of the status of the connectivity.  Transient
     * to avoid being parsed as Json.
     */
    protected transient Status mStatus = Status.Disconnected;

    /**
     * Constructs a Connection using a given name.  Addresses or
     * connection information are managed by subclasses.
     *
     * @param name The name of the connection.
     */
    public Connection(String name) {
        if(name == null) {
            System.out.println("Connection has no name!");
            mName = "";
        } else {
            mName = name;
        }
        mUUID = UUID.randomUUID();
        connections.put(mUUID, this);
    }

    /**
     * No-argument constructor made private so that Gson can correctly
     * build this object and then populate the members with Json data.
     */
    protected Connection() {
        mName = null;
        mUUID = UUID.randomUUID();
        connections.put(mUUID, this);
    }

    /**
     * Returns the name of this connection.
     *
     * @return The name of this connection.
     */
    public String getName() {
        return this.mName;
    }

    /**
     * Returns the unique identifier of this Connection.
     *
     * @return The unique identifier of this Connection.
     */
    public String getUUID() {
        return mUUID.toString();
    }

    /**
     * Returns an existing connection being held in the static map.
     *
     * @param uuid The UUID of the connection.
     * @return The Connection, or null if there is no key for the UUID.
     */
    public static Connection getConnection(UUID uuid) {
        return connections.get(uuid);
    }

    /**
     * Returns an existing connection being held in the static map.
     *
     * @param uuid The UUID of the connection.
     * @return The Connection, or null if there is no kwy for the UUID.
     */
    public static Connection getConnection(String uuid) {
        return getConnection(UUID.fromString(uuid));
    }

    /**
     * Send connect request to ConnectionService to open a Connection
     * using this object's data.
     *
     * @param context The context to send the intent to launch the Service.
     */
    public void connect(Context context) {
        if(!(getStatus().equals(Status.Connected))) {

            //Send intent with this connection's data over LocalBroadcastManager
            new ConnectIntent(context, this).sendLocal();

            //Indicate that this connection's status is now "connecting".
            mStatus = Status.Connecting;
        }
    }

    /**
     * Send disconnect request to ConnectionService to close a Connection
     * using this object's data.
     *
     * @param context The context to send the intent to launch the Service.
     */
    public void disconnect(Context context) {
        if(getStatus().equals(Status.Connected)) {

            //Send intent with this connection's data over LocalBroadcastManager
            new DisconnectIntent(context, this).sendLocal();
        }
    }

    /**
     * Sends an intent to ConnectionService with data that should be sent over this
     * connection.
     *
     * @param context The context to send the intent from.
     * @param data    The data to send.
     */
    public void send(Context context, byte[] data) {
        new DataSendIntent(context, this, data).sendLocal();
    }

    /**
     * Tells what the status of this connection is.
     * Statuses include:
     * Connected
     * Connecting
     * Disconnected
     * Connect Failed
     * Connect Canceled
     *
     * @return Status of connection.
     */
    public abstract Status getStatus();

    /**
     * Convenience method for use with intent extra "CONNECTION_TYPE".
     *
     * @return The string "connection type" as defined by ConnectionIntent.
     */
    public abstract String getConnectionType();

    public abstract int getImageResourceId();

    /**
     * Returns an InputStream that reads from this Connection's remote source.
     *
     * @return An InputStream that reads from this Connection's remote source.
     * @throws IllegalStateException If this Connection is not connected.
     */
    public abstract InputStream getInputStream() throws IllegalStateException;

    /**
     * Returns an OutputStream that writes to this Connection's remote destination.
     *
     * @return An OutputStream that writes to this Connection's remote destination.
     * @throws IllegalStateException If this Connection is not connected.
     */
    public abstract OutputStream getOutputStream() throws IllegalStateException;

    /**
     * Hashing a connection object will tell if the two objects contain
     * the exact content data, but the same connection - if any
     * member values are changed - will hash differently.  This method
     * is here to compare two connection and determine whether they represent
     * the same connection regardless of the status of the member data.
     * This is determined by comparing the UUIDs of each connection.
     *
     * @param c The connection to compare to this object.
     * @return True if connections are the same, False otherwise.
     */
    public boolean isVersionOf(Connection c) {
        return c.getUUID().equals(this.getUUID());
    }

    /**
     * This class uses an Observable form without actually extending Observable,
     * since we won't have a way to deter Gson from packaging the observers into
     * our json files.  That being said, this adds a new Observer.
     *
     * @param observer The observer to add to this connection.
     */
    public void addObserver(Observer observer) {
        if(!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    /**
     * Notifies all subscribed observers about some change to this Connection's data.
     *
     * @param status The status of the change.
     */
    public void notifyObservers(Status status) {
        for(Observer observer : mObservers) {
            observer.update(null, status);
        }
    }
}
