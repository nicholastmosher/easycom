package org.tec_hub.tecuniversalcomm.connection;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.Device;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Nick Mosher on 3/3/2015.
 */
public abstract class Connection implements Parcelable {

    private final String mConnectionName;

    //Transient to prevent infinite loops during JSON serialization.
    protected transient Device mParent = null;

    protected Map<Context, OnConnectStatusChangedListener> mOnConnectStatusChangedListeners;

    /**
     * Constructs a Connection using a given name.  Addresses or
     * connection information are managed by subclasses.
     * @param name The name of the connection.
     */
    public Connection(String name) {
        mConnectionName = Preconditions.checkNotNull(name);
        mOnConnectStatusChangedListeners = new HashMap<>();
    }

    public Connection(Parcel in) {
        Preconditions.checkNotNull(in);
        mConnectionName = Preconditions.checkNotNull(in.readString());
        mOnConnectStatusChangedListeners = (HashMap<Context, OnConnectStatusChangedListener>) in.readSerializable();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mConnectionName);
        out.writeSerializable((Serializable) mOnConnectStatusChangedListeners);
    }

    /**
     * Sets the device object that this connection is a part of.
     * @param mParent The Device that is the parent to this connection.
     */
    public void setParent(Device mParent) {
        this.mParent = mParent;
    }

    public Device getParent() {
        return this.mParent;
    }

    /**
     * Returns the name of this connection.
     * @return The name of this connection.
     */
    public String getName() {
        return this.mConnectionName;
    }

    public int describeContents() {
        return 0;
    }

    public abstract boolean isConnected();

    public abstract void connect(Context context);

    public abstract void disconnect(Context context);

    public abstract InputStream getInputStream();

    public abstract OutputStream getOutputStream();

    public interface OnConnectStatusChangedListener {
        public void onConnect();
        public void onDisconnect();
    }

    /**
     * Adds a new OnConnectStatusChangedListener to the map.  The Context
     * is used as the map key so that more than one activity may set
     * callbacks but no activity may have duplicate listeners.
     * @param context The context of the listener.
     * @param listener The OnConnectStatusChangedListener to associate with the context.
     */
    public void setOnConnectStatusChangedListener(Context context, OnConnectStatusChangedListener listener) {
        mOnConnectStatusChangedListeners.put(context, listener);
    }

    public void setOnConnectStatusChangedListeners(Map<Context, OnConnectStatusChangedListener> listeners) {
        Preconditions.checkNotNull(listeners);
        mOnConnectStatusChangedListeners = listeners;
    }

    public Map<Context, OnConnectStatusChangedListener> getOnConnectStatusChangedListeners() {
        return mOnConnectStatusChangedListeners;
    }

    /**
     * Loops through all registered OnConnectStatusChangedListeners and notifies them of connection.
     */
    protected void notifyConnected() {
        Preconditions.checkNotNull(mOnConnectStatusChangedListeners);
        Set<Context> listenerKeys = mOnConnectStatusChangedListeners.keySet();
        for(Context c : listenerKeys) {
            mOnConnectStatusChangedListeners.get(c).onConnect();
        }
    }

    /**
     * Loops through all registered OnConnectStatusChangedListeners and notifies them of disconnection.
     */
    protected void notifyDisconnected() {
        Preconditions.checkNotNull(mOnConnectStatusChangedListeners);
        Set<Context> listenerKeys = mOnConnectStatusChangedListeners.keySet();
        for(Context c : listenerKeys) {
            mOnConnectStatusChangedListeners.get(c).onDisconnect();
        }
    }
}
