package org.tec_hub.tecuniversalcomm.data.connection;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Preconditions;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Nick Mosher on 3/3/2015.
 */
public abstract class Connection implements Parcelable {

    /**
     * Holds references to observers.  Transient to avoid being parsed to Json.
     */
    private transient List<ConnectionObserver> observers = new ArrayList<ConnectionObserver>();

    /**
     * Cues to specify to observers what kind of update is occurring.
     */
    public enum Cues {
        Connected,
        Disconnected,
        ConnectFailed,
        ConnectCanceled
    }

    /**
     * The immutable name of this Connection.
     */
    private final String mConnectionName;

    /**
     * A unique identifier for this Connection, used as a reliable key
     * for storing and retrieving data from static Maps.
     */
    protected transient final UUID mUUID;

    /**
     * Constructs a Connection using a given name.  Addresses or
     * connection information are managed by subclasses.
     * @param name The name of the connection.
     */
    public Connection(String name) {
        mConnectionName = Preconditions.checkNotNull(name);
        mUUID = UUID.randomUUID();
    }

    public Connection(Parcel in) {
        Preconditions.checkNotNull(in);
        mConnectionName = Preconditions.checkNotNull(in.readString());
        mUUID = Preconditions.checkNotNull(UUID.fromString(in.readString()));
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mConnectionName);
        out.writeString(mUUID.toString());
    }

    /**
     * Returns the name of this connection.
     * @return The name of this connection.
     */
    public String getName() {
        return this.mConnectionName;
    }

    /**
     * Returns the unique identifier of this Connection.
     * @return The unique identifier of this Connection.
     */
    public UUID getUUID() {
        return mUUID;
    }

    /**
     * Requirement of Parcelable, not sure what for.
     * @return 0.
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Launches a Service action that initiates this connection's communication
     * link.
     * @param context The context to launch the Service from.
     */
    public abstract void connect(Context context);

    /**
     * Launches a Service action that disconnects this connection's communication
     * link.
     * @param context The context to launch the Service from.
     */
    public abstract void disconnect(Context context);

    /**
     * Tells whether this Connection is actively connected.
     * @return True if connected.
     */
    public abstract boolean isConnected();

    /**
     * Returns an InputStream that reads from this Connection's remote source.
     * @return An InputStream that reads from this Connection's remote source.
     * @throws IllegalStateException If this Connection is not connected.
     */
    public abstract InputStream getInputStream() throws IllegalStateException;

    /**
     * Returns an OutputStream that writes to this Connection's remote destination.
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
     * @param c The connection to compare to this object.
     * @return True if connections are the same, False otherwise.
     */
    public boolean isVersionOf(Connection c) {
        return c.getUUID().equals(this.getUUID());
    }

    /**
     * Adds an observer object to our list.  All observers are notified of relevant updates.
     * @param observer The new observer to keep track of.
     */
    public void addObserver(ConnectionObserver observer) {
        if(observer == null) {
            throw new NullPointerException("Observer is null!");
        }
        synchronized (this) {
            if(!observers.contains(observer)) {
                observers.add(observer);
            }
        }
    }

    public void notifyObservers(Cues cue) {
        for(ConnectionObserver observer : observers) {
            observer.onUpdate(this, cue);
        }
    }
}
