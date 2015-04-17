package org.tec_hub.tecuniversalcomm.Connection;

import android.os.Parcel;
import android.os.Parcelable;

import org.tec_hub.tecuniversalcomm.Device;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Created by Nick Mosher on 3/3/2015.
 */
public abstract class Connection implements Parcelable, Serializable {

    private static final long serialVersionUID = -7174951771893696321L/*-3032919221308563227L*/;

    private final String mConnectionName;

    /*
     * Parent is not final because it can be assigned after the Connection is
     * discovered and established.
     */
    private Device mParent = null;

    /**
     * Constructs a Connection using a given name.  Addresses or
     * connection information are managed by subclasses.
     *
     * @param name The name of the connection.
     */
    public Connection(String name) {
        mConnectionName = name;
    }

    public Connection(Parcel in) {
        mConnectionName = in.readString();
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
    }

    public abstract boolean isConnected();

    public abstract void connect();

    public abstract void disconnect();

    public abstract InputStream getInputStream();

    public abstract OutputStream getOutputStream();
}
