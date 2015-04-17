package org.tec_hub.tecuniversalcomm.Connection;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.tec_hub.tecuniversalcomm.Device;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nick Mosher on 3/3/2015.
 */
public abstract class Connection implements Parcelable, Serializable {

    private static final long serialVersionUID = -7174951771893696321L/*-3032919221308563227L*/;

    private final String mConnectionName;

    //Not final because parent assignment happens after construction.
    protected Device mParent = null;

    protected boolean mConnected;

    protected Map<Context, OnConnectStatusChangedListener> mOnConnectStatusChangedListeners;

    /**
     * Constructs a Connection using a given name.  Addresses or
     * connection information are managed by subclasses.
     *
     * @param name The name of the connection.
     */
    public Connection(String name) {
        mConnectionName = name;
        mOnConnectStatusChangedListeners = new HashMap<Context, OnConnectStatusChangedListener>();
    }

    public Connection(Parcel in) {
        mConnectionName = in.readString();
        mOnConnectStatusChangedListeners = (HashMap<Context, OnConnectStatusChangedListener>) in.readSerializable();
    }

    /**
     * Sets the device object that this connection is a part of.
     *
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
     *
     * @return The name of this connection.
     */
    public String getName() {
        return this.mConnectionName;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mConnectionName);
        out.writeSerializable((Serializable) mOnConnectStatusChangedListeners);
    }

    public abstract boolean isConnected();

    public abstract void connect();

    public abstract void disconnect();

    public abstract InputStream getInputStream();

    public abstract OutputStream getOutputStream();

    public interface OnConnectStatusChangedListener {
        public void onConnect();
        public void onDisconnect();
    }

    public abstract void setOnConnectStatusChangedListener(Context context, OnConnectStatusChangedListener listener);

    protected abstract void notifyConnected();

    protected abstract void notifyDisconnected();
}
