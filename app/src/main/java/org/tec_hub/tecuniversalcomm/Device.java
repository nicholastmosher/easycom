package org.tec_hub.tecuniversalcomm;

import android.os.Parcel;
import android.os.Parcelable;

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
public class Device implements Parcelable, Serializable
{
    public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>()
    {
        public Device createFromParcel(Parcel in)
        {
            return new Device(in);
        }

        public Device[] newArray(int size)
        {
            return new Device[size];
        }
    };

    private static final long serialVersionUID = -4431171658721312519L;

    private ArrayList<Connection> connections;
    private String name;

    /**
     * Constructs a new Device based on information in a Connection object.
     * @param connection
     */
    public Device(String name, Connection connection)
    {
        connections = new ArrayList<Connection>();
        if(connection != null)
        {
            connection.setParent(this);
            connections.add(connection);
        }
        if(name != null)
        {
            this.name = name;
        }
        else
        {
            this.name = (connection.getName() != null ? connection.getName() : "Device (" + hashCode() + ")");
        }
    }

    /**
     * Creates a device with no connections.
     * @param name The name of the device.
     */
    public Device(String name)
    {
        this(name, null);
    }

    public Device(Connection connection)
    {
        this(null, connection);
    }

    /**
     * Constructs this device from it's parcelable representation.
     * @param in The parcelable representation of this object.
     */
    public Device(Parcel in)
    {
        name = in.readString();
        connections = new ArrayList<Connection>();
        Connection[] temp = (Connection[]) in.readParcelableArray(Connection.class.getClassLoader());
        for(Connection c : temp)
        {
            c.setParent(this);
            connections.add(c);
        }
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    /**
     * Returns true if this device has an associated BT interface.
     * @return
     */
    public boolean hasBTConnection()
    {
        /*
         * For each remote connection in this device's connections,
         * if one of those connections is bluetooth, return true.
         */
        for(Connection rc : connections)
        {
            if(rc.getType() == Connection.Type.Bluetooth)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an ArrayList of all BT connections that are associated with the
     * given device.
     * @return
     */
    public ArrayList<Connection> getConnections()
    {
        return connections;
    }

    public int hashCode()
    {
        int hash = 2;
        if(connections != null)
        {
            for(Connection c : connections)
            {
                hash += c.hashCode();
            }
        }
        return hash;
    }

    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }

        if(!(o instanceof Device))
        {
            return false;
        }

        if(hashCode() == o.hashCode())
        {
            return true;
        }
        return false;
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(name);
        out.writeParcelableArray((Connection[]) connections.toArray(), flags);
    }
}
