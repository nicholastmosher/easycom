package org.tec_hub.tecuniversalcomm;

import android.os.Parcel;
import android.os.Parcelable;

import org.tec_hub.tecuniversalcomm.Connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.Connection.Connection;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Nick Mosher on 3/3/2015.
 *
 * In TEC COMM, a Device represents a remote machine that you wish to connect to.
 * That device may have multiple avenues of communication between itself and this
 * android, such as Bluetooth, Wifi, etc. (more may be implemented later)
 * Each avenue of connection is represented by a Connection. (see Connection.java)
 * These Connections are managed with an ArrayList in Device.
 */
public class Device implements Parcelable, Serializable {

    public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>() {
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    private static final long serialVersionUID = -4431171658721312519L;

    private ArrayList<Connection> mConnections;
    private String mName;

    /**
     * Constructs a new Device based on information in a Connection object.
     *
     * @param connection
     */
    public Device(String name, Connection connection) {
        mConnections = new ArrayList<Connection>();
        if (connection != null) {
            connection.setParent(this);
            mConnections.add(connection);
        }
        if (name != null) {
            this.mName = name;
        } else {
            this.mName = (connection.getName() != null ? connection.getName() : "Device (" + hashCode() + ")");
        }
    }

    /**
     * Creates a device with no mConnections.
     *
     * @param name The mName of the device.
     */
    public Device(String name) {
        this(name, null);
    }

    public Device(Connection connection) {
        this(null, connection);
    }

    /**
     * Constructs this device from it's parcelable representation.
     *
     * @param in The parcelable representation of this object.
     */
    public Device(Parcel in) {
        mName = in.readString();
        mConnections = new ArrayList<Connection>();
        Connection[] temp = (Connection[]) in.readParcelableArray(Connection.class.getClassLoader());
        for (Connection c : temp) {
            c.setParent(this);
            mConnections.add(c);
        }
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getName() {
        return this.mName;
    }

    /**
     * Returns true if this device has an associated BT interface.
     *
     * @return
     */
    public boolean hasBTConnection() {
        /*
         * For each connection in this device's mConnections,
         * if one of those mConnections is bluetooth, return true.
         */
        for (Connection c : mConnections) {
            if (c instanceof BluetoothConnection) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an ArrayList of all BT mConnections that are associated with the
     * given device.
     * //TODO will cause problems when multiple bluetooth connections are assigned to one device
     * @return
     */
    public ArrayList<Connection> getConnections() {
        return mConnections;
    }

    public BluetoothConnection getBluetoothConnection() {
        if(mConnections != null) {
            for (Connection c : mConnections) {
                if (c instanceof BluetoothConnection) {
                    return (BluetoothConnection) c;
                }
            }
        }
        return null;
    }

    public int hashCode() {
        int hash = 2;
        if (mConnections != null) {
            for (Connection c : mConnections) {
                hash += c.hashCode();
            }
        }
        return hash;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Device)) {
            return false;
        }

        if (hashCode() == o.hashCode()) {
            return true;
        }
        return false;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mName);
        out.writeParcelableArray((Connection[]) mConnections.toArray(), flags);
    }
}
