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
 * That device may have multiple avenues of communication between itself and this
 * android, such as Bluetooth, Wifi, etc. (more may be implemented later)
 * Each avenue of connection is represented by a Connection. (see Connection.java)
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

    public static Device build(String name, List<Connection> connections, UUID uuid) {
        return new Device(name, connections, uuid);
    }

    public static Device build(String name, List<Connection> connections, String uuid) {
        Preconditions.checkNotNull(uuid);
        return new Device(name, connections, UUID.fromString(uuid));
    }

    public static Device build(String name, Connection connection, UUID uuid) {
        Preconditions.checkNotNull(connection);
        List<Connection> connections = new ArrayList<>();
        connections.add(connection);
        return new Device(name, connections, uuid);
    }

    public static Device build(String name, Connection connection, String uuid) {
        Preconditions.checkNotNull(uuid);
        return build(name, connection, UUID.fromString(uuid));
    }

    public static Device build(String name, List<Connection> connections) {
        return new Device(name, connections, UUID.randomUUID());
    }

    public static Device build(String name, Connection connection) {
        Preconditions.checkNotNull(connection);
        List<Connection> connections = new ArrayList<>();
        connections.add(connection);
        return build(name, connections);
    }

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

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getName() {
        return this.mName;
    }

    public UUID getUUID() {
        return mUUID;
    }

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

    public boolean isVersionOf(Device d) {
        return d.getUUID().equals(this.getUUID());
    }

    public int describeContents() {
        return 0;
    }

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
