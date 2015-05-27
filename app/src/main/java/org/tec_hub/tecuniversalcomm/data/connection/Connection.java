package org.tec_hub.tecuniversalcomm.data.connection;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Preconditions;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Nick Mosher on 3/3/2015.
 */
public abstract class Connection implements Parcelable {

    /**
     * All Connections will eventually be transmitted through the Parcelable
     * framework, however the Parcel methods do not support Generics like
     * Maps well.  To compensate for this, a static Map is held in Connection
     * with a key of a Connection's UUID and a value of that Connection's
     * Map of OnStatusChangedListeners.  The Connection instance's Map
     * is put into the static Map at writeToParcel() and retrieved at the
     * Parcel Constructor of Connection.
     */
    private static Map<UUID, Map<Context, OnStatusChangedListener>> mConnectionListenerMaps = new HashMap<>();

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
     * Contains all registered OnStatusChangedListeners for this Connection.
     * OnStatusChangedListeners are stored using a Context as a key in order
     * to prevent duplicates being created from repeated onCreate() calls
     * or the like.
     */
    protected Map<Context, OnStatusChangedListener> mOnStatusChangedListeners;

    /**
     * Constructs a Connection using a given name.  Addresses or
     * connection information are managed by subclasses.
     * @param name The name of the connection.
     */
    public Connection(String name) {
        mConnectionName = Preconditions.checkNotNull(name);
        mOnStatusChangedListeners = new HashMap<>();
        mUUID = UUID.randomUUID();
    }

    public Connection(Parcel in) {
        Preconditions.checkNotNull(in);
        mConnectionName = Preconditions.checkNotNull(in.readString());
        mUUID = Preconditions.checkNotNull(UUID.fromString(in.readString()));
        mOnStatusChangedListeners = Preconditions.checkNotNull(mConnectionListenerMaps.get(mUUID));
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mConnectionName);
        out.writeString(mUUID.toString());
        mConnectionListenerMaps.put(mUUID, mOnStatusChangedListeners);
    }

    /**
     * Returns the name of this connection.
     * @return The name of this connection.
     */
    public String getName() {
        return this.mConnectionName;
    }

    /**
     * Requirement of Parcelable, not sure what for.
     * @return 0.
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Tells whether this Connection is actively connected.
     * @return True if connected.
     */
    public abstract boolean isConnected();

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
     * Gives a callback to listen for this connection's successful connection
     * or disconnection.
     */
    public interface OnStatusChangedListener {
        public void onConnect();
        public void onDisconnect();
        public void onConnectFailed();
    }

    /**
     * Adds a new OnStatusChangedListener to the map.  The Context
     * is used as the map key so that more than one activity may set
     * callbacks but no activity may have duplicate listeners.
     * @param context The context of the listener.
     * @param listener The OnStatusChangedListener to associate with the context.
     */
    public void putOnStatusChangedListener(Context context, OnStatusChangedListener listener) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(listener);
        mOnStatusChangedListeners.put(context, listener);
    }

    public void setOnStatusChangedListeners(Map<Context, OnStatusChangedListener> listeners) {
        Preconditions.checkNotNull(listeners);
        mOnStatusChangedListeners = listeners;
    }

    public Map<Context, OnStatusChangedListener> getOnConnectStatusChangedListeners() {
        return mOnStatusChangedListeners;
    }

    /**
     * Loops through all registered OnConnectStatusChangedListeners and notifies them of connection.
     */
    protected void notifyConnected() {
        Preconditions.checkNotNull(mOnStatusChangedListeners);
        Set<Context> listenerKeys = mOnStatusChangedListeners.keySet();
        for(Context c : listenerKeys) {
            mOnStatusChangedListeners.get(c).onConnect();
        }
    }

    /**
     * Loops through all registered OnConnectStatusChangedListeners and notifies them of disconnection.
     */
    protected void notifyDisconnected() {
        Preconditions.checkNotNull(mOnStatusChangedListeners);
        Set<Context> listenerKeys = mOnStatusChangedListeners.keySet();
        for(Context c : listenerKeys) {
            mOnStatusChangedListeners.get(c).onDisconnect();
        }
    }

    protected void notifyConnectFailed() {
        Preconditions.checkNotNull(mOnStatusChangedListeners);
        Set<Context> listenerKeys = mOnStatusChangedListeners.keySet();
        for(Context c : listenerKeys) {
            mOnStatusChangedListeners.get(c).onConnectFailed();
        }
    }
}
