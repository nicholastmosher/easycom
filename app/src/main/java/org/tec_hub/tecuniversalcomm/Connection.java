package org.tec_hub.tecuniversalcomm;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Nick Mosher on 3/3/2015.
 */
public class Connection implements Parcelable, Serializable
{
    public enum Type
    {
        Undefined,
        Bluetooth
    }

    public static final Parcelable.Creator<Connection> CREATOR = new Parcelable.Creator<Connection>()
    {
        public Connection createFromParcel(Parcel in)
        {
            return new Connection(in);
        }

        public Connection[] newArray(int size)
        {
            return new Connection[size];
        }
    };

    private static final long serialVersionUID = -3032919221308563227L;

    private final Type connectionType;
    private final String connectionName;
    private final String bluetoothAddress;

    /*
     * Parent name is not final because it can be assigned after the Connection is
     * discovered and established.
     */
    private Device parent = null;

    /**
     * Constructs a bluetooth Connection using a given name and address.
     * @param name The name of the bluetooth connection.
     * @param address The address of the bluetooth device.
     */
    public Connection(Type type, String name, String address)
    {
        switch(type)
        {
            case Bluetooth:
                this.connectionType = Type.Bluetooth;
                this.connectionName = name;
                this.bluetoothAddress = address;
                break;

            case Undefined: //Fall through to default.
            default:
                this.connectionType = Type.Undefined;
                this.connectionName = null;
                this.bluetoothAddress = null;
        }
    }

    public Connection(Parcel in)
    {
        connectionType = getTypeFromName(in.readString());
        connectionName = in.readString();
        bluetoothAddress = in.readString();
    }

    /**
     * Sets the device object that this connection is a part of.
     * @param parent
     */
    public void setParent(Device parent)
    {
        this.parent = parent;
    }

    public Device getParent()
    {
        return this.parent;
    }

    /**
     * Returns the type of this Connection in the form of an enum.
     * @return
     */
    public Type getType()
    {
        return this.connectionType;
    }

    /**
     * Returns the name of this bluetooth connection.
     * @return
     */
    public String getName()
    {
        return this.connectionName;
    }

    /**
     * Returns the address of this bluetooth connection.
     * @return
     */
    public String getBluetoothAddress()
    {
        return (this.connectionType == Type.Bluetooth) ? this.bluetoothAddress : null;
    }

    public static Type getTypeFromName(String name)
    {
        Type type = Type.Undefined;
        if(name.equalsIgnoreCase("bluetooth"))
        {
            type = Type.Bluetooth;
        }
        return type;
    }

    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }

        if(this == null || (o.getClass() != this.getClass()))
        {
            return false;
        }

        Connection other = (Connection) o;
        if(this.getType() != other.getType())
        {
            return false;
        }

        switch(this.getType())
        {
            case Bluetooth:
                if(this.getBluetoothAddress().equals(other.getBluetoothAddress()))
                {
                    return true;
                }
                break;
            case Undefined:
                return false;
            default:
        }
        return false;
    }

    /**
     * Generates a code that will be unique from any connection with relevantly different data,
     * but the same as any connection that is functionally identical.
     * @return
     */
    public int hashCode()
    {
        int hash = 1;

        String connectionTypeString = this.connectionType.toString();
        for(int i = 0; i < connectionTypeString.length(); i++)
        {
            hash += connectionTypeString.charAt(i);
        }

        if(this.bluetoothAddress != null)
        {
            for(int i = 0; i < bluetoothAddress.length(); i++)
            {
                hash += bluetoothAddress.charAt(i);
            }
        }
        return hash;
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(connectionType.toString());
        out.writeString(connectionName);
        out.writeString(bluetoothAddress);
    }
}
