package org.tec_hub.tecuniversalcomm.data;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.Connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Nick Mosher on 3/3/2015.
 *
 * In TEC COMM, a Device represents a remote machine that you wish to connect to.
 * That device may have multiple interfaces of communication between itself and this
 * android, such as Bluetooth, Wifi, etc. (more may be implemented later)
 * Each interface is represented by a Connection. (see Connection.java)
 * These Connections are managed with an ArrayList in Device.
 */
public class Device implements Parcelable {

    public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>() {
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    private static Map<UUID, List<Connection>> connectionListsMap = new HashMap<>();

    private String mName;
    private final UUID mUUID;
    private List<Connection> mConnections;

    /**
     * Constructs a device.  This constructor is accessible via the build methods
     * which allow different types of parameters which convert into ones matching
     * this constructor.
     * @param name The name of this device.
     * @param connections A List of Connections that this device owns.
     * @param uuid A unique identifier used for keeping track of this device's
     *             profile identity even when member data (and thus hashes) vary.
     */
    private Device(String name, List<Connection> connections, UUID uuid) {
        mName = Preconditions.checkNotNull(name);
        mConnections = Preconditions.checkNotNull(connections);
        mUUID = Preconditions.checkNotNull(uuid);

        connectionListsMap.put(mUUID, mConnections);
    }

    /**
     * No-argument constructor made private so that Gson can correctly
     * build this object and then populate the members with Json data.
     */
    protected Device() {
        mName = null;
        mUUID = UUID.randomUUID();
        mConnections = new ArrayList<>();
    }

    /**
     * Constructs and returns a new Device based on a name, List of Connections, and uuid.
     * @param name The name of the device.
     * @param connections An existing List of connections for this device to own.
     * @param uuid The unique identifier for this Device that distinguishes the device's profile
     *             regardless of hash data (see method isVersionOf()).
     * @return A newly constructed Device with data initialized from these parameters.
     */
    public static Device build(String name, List<Connection> connections, UUID uuid) {
        return new Device(name, connections, uuid);
    }

    /**
     * Constructs and returns a new Device based on a name, List of Connections, and String uuid.
     * @param name The name of the device.
     * @param connections An existing List of connections for this device to own.
     * @param uuid The unique identifier for this Device that distinguishes the device's profile
     *             regardless of hash data (see method isVersionOf()).
     * @return A newly constructed Device with data initialized from these parameters.
     */
    public static Device build(String name, List<Connection> connections, String uuid) {
        Preconditions.checkNotNull(uuid);
        return new Device(name, connections, UUID.fromString(uuid));
    }

    /**
     * Constructs and returns a new device based on a name, a single Connection, and uuid.
     * @param name The name of the device.
     * @param connection An existing Connection for this device to own.
     * @param uuid A unique identifier for this Device that distinguishes the device's profile
     *             regardless of hash data (see method isVersionOf()).
     * @return A newly constructed Device with data initialized from these parameters.
     */
    public static Device build(String name, Connection connection, UUID uuid) {
        Preconditions.checkNotNull(connection);
        List<Connection> connections = new ArrayList<>();
        connections.add(connection);
        return new Device(name, connections, uuid);
    }

    /**
     * Constructs and returns a new device based on a name, a single Connection, and String uuid.
     * @param name The name of the device.
     * @param connection An existing Connection for this device to own.
     * @param uuid A unique identifier for this Device that distinguishes the device's profile
     *             regardless of hash data (see method isVersionOf()).
     * @return A newly constructed Device with data initialized from these parameters.
     */
    public static Device build(String name, Connection connection, String uuid) {
        Preconditions.checkNotNull(uuid);
        return build(name, connection, UUID.fromString(uuid));
    }

    /**
     * Constructs and returns a new device based on a name and a List of Connections.  A random
     * uuid is generated for the device.
     * @param name The name of the device.
     * @param connections An existing List of Connections for this device to own.
     * @return A newly constructed device with data initialized from these parameters.
     */
    public static Device build(String name, List<Connection> connections) {
        return new Device(name, connections, UUID.randomUUID());
    }

    /**
     * Constructs and returns a new device based on a name and a single Connection.  A random
     * uuid is generated for the device.
     * @param name The name of the device.
     * @param connection An existing Connection for this device to own.
     * @return A newly constructed device with data initialized from these parameters.
     */
    public static Device build(String name, Connection connection) {
        System.out.println("Build device: " + name);
        Preconditions.checkNotNull(connection);
        List<Connection> connections = new ArrayList<>();
        connections.add(connection);
        return build(name, connections);
    }

    /**
     * Constructs and returns a new device based on a name.  The device is initialized with an
     * empty Connections List and a random uuid.
     * @param name The name of the device.
     * @return A newly constructed device with data initialized from these parameters.1
     */
    public static Device build(String name) {
        return build(name, new ArrayList<Connection>());
    }

    /**
     * Constructs this device from it's parcelable representation.
     * @param in The parcelable representation of this object.
     */
    public Device(Parcel in) {
        mName = Preconditions.checkNotNull(in.readString());
        String uuidString = Preconditions.checkNotNull(in.readString());
        mUUID = UUID.fromString(uuidString);
        mConnections = Preconditions.checkNotNull(connectionListsMap.get(mUUID));
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mName);
        out.writeString(mUUID.toString());
        connectionListsMap.put(mUUID, mConnections);
    }

    /**
     * Sets the name of this device.
     * @param mName The new name of the device.
     */
    public void setName(String mName) {
        this.mName = mName;
    }

    /**
     * Returns the name of this device.
     * @return The name of this device.
     */
    public String getName() {
        return this.mName;
    }

    /**
     * Returns the UUID of this device.
     * @return The UUID of this device.
     */
    public UUID getUUID() {
        return mUUID;
    }

    /**
     * Returns a List of all Connections belonging to this device.
     * @return A List of all Connections belonging to this device.
     */
    public List<Connection> getConnections() {
        return mConnections;
    }

/*    *//**
     * Returns an ArrayList of all BT mConnections that are associated with the
     * given device.
     * //FIXME will cause problems when multiple bluetooth connections are assigned to one device
     * @return
     *//*
    public BluetoothConnection getBluetoothConnection() {
        if(mConnections != null) {
            for (Connection c : mConnections) {
                if (c instanceof BluetoothConnection) {
                    return (BluetoothConnection) c;
                }
            }
        }
        return null;
    }*/

    /**
     * Returns a hash of this object.  Two device objects containing the same member data
     * will hash the same.
     * @return A hash of this device.
     */
    public int hashCode() {
        int hash = 2;
        if(mConnections != null) {
            for(Connection c : mConnections) {
                hash += c.hashCode();
            }
        }
        return hash;
    }

    /**
     * Determines whether an object equals this device.  Two devices are equal if they have
     * identical member data.
     * @param o The object to compare to this device.
     * @return True if the devices are equal, false otherwise.
     */
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }

        if(!(o instanceof Device)) {
            return false;
        }

        if(hashCode() == o.hashCode()) {
            return true;
        }
        return false;
    }

    /**
     * Hashing a device object will tell if the two objects contain
     * the exact content data, but the same device profile - if any
     * member values are changed - will hash differently.  This method
     * is here to compare two devices and determine whether they represent
     * the same device profile regardless of the status of the member data.
     * This is determined by comparing the UUIDs of each device.
     * @param d The device to compare to this object.
     * @return True if devices are the same device profile, False otherwise.
     */
    public boolean isVersionOf(Device d) {
        return d.getUUID().equals(this.getUUID());
    }

    public int describeContents() {
        return 0;
    }

    //TODO fix persistent data editing after prompt
    public static void promptRename(final Device device, final Context context) {
        //Create an EditText view to get user input
        final EditText input = new EditText(context);
        input.setText(device.getName());
        input.selectAll();

        //Use a Dialog Builder to set Positive and Negative action buttons
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                if(value != null && !value.equals("")) {
                    device.setName(value);
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        //Create AlertDialog from builder
        AlertDialog dialog = dialogBuilder.create();
        dialog.setTitle("Rename Device");
        dialog.setView(input);

        //Set action to happen when dialog shows
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .toggleSoftInputFromWindow(input.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            }
        });

        //Show the dialog
        dialog.show();
    }
}
