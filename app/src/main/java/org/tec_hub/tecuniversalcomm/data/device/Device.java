package org.tec_hub.tecuniversalcomm.data.device;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.StorageAdapter;
import org.tec_hub.tecuniversalcomm.data.connection.Connection;
import org.tec_hub.tecuniversalcomm.data.connection.ConnectionList;

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
public class Device {

    /**
     * Static maps stores all constructed devices.  This way we
     * can reference them from different activities without needing
     * to pass through the Parcelable framework.
     */
    protected static transient Map<UUID, Device> devices = new HashMap<>();

    /**
     * Holds references to observers.  Transient to avoid being parsed to Json.
     */
    private transient List<DeviceObserver> observers = new ArrayList<>();

    /**
     * Status to specify to observers what kind of update is happening.
     */
    public enum Status {
        NameUpdated,
        ConnectionsUpdated
    }

    private String mName;
    private final UUID mUUID;
    private ConnectionList mConnections;

    /**
     * Constructs a device.  This constructor is accessible via the build methods
     * which allow different types of parameters which convert into ones matching
     * this constructor.
     * @param name The name of this device.
     * @param connections A List of Connections that this device owns.
     * @param uuid A unique identifier used for keeping track of this device's
     *             profile identity even when member data (and thus hashes) vary.
     */
    private Device(String name, ConnectionList connections, UUID uuid) {
        mName = Preconditions.checkNotNull(name);
        mConnections = Preconditions.checkNotNull(connections);
        mUUID = Preconditions.checkNotNull(uuid);
        devices.put(mUUID, this);
        addObserver(StorageAdapter.OBSERVER);
    }

    /**
     * No-argument constructor made private so that Gson can correctly
     * build this object and then populate the members with Json data.
     */
    protected Device() {
        mName = null;
        mUUID = UUID.randomUUID();
        mConnections = new ConnectionList();
        devices.put(mUUID, this);
        addObserver(StorageAdapter.OBSERVER);
    }

    /**
     * Constructs and returns a new Device based on a name, List of Connections, and uuid.
     * @param name The name of the device.
     * @param connections An existing List of connections for this device to own.
     * @param uuid The unique identifier for this Device that distinguishes the device's profile
     *             regardless of hash data (see method isVersionOf()).
     * @return A newly constructed Device with data initialized from these parameters.
     */
    public static Device build(String name, ConnectionList connections, UUID uuid) {
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
    public static Device build(String name, ConnectionList connections, String uuid) {
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
        ConnectionList connections = new ConnectionList();
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
    public static Device build(String name, ConnectionList connections) {
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
        ConnectionList connections = new ConnectionList();
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
        return build(name, new ConnectionList());
    }

    public void init() {
        devices.put(mUUID, this);
        addObserver(StorageAdapter.OBSERVER);
    }

    /**
     * Sets the name of this device.
     * @param mName The new name of the device.
     */
    public void setName(String mName) {
        this.mName = mName;
        notifyObservers(Status.NameUpdated);
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
    public String getUUID() {
        return mUUID.toString();
    }

    /**
     * Returns an existing device being held in the static map.
     * @param uuid The UUID of the device.
     * @return The Device, or null if there is no key for the UUID.
     */
    public static Device getDevice(UUID uuid) {
        return devices.get(uuid);
    }

    /**
     * Returns an existing device being held in the static map.
     * @param uuid The UUID of the device.
     * @return The Device, or null if there is no key for the UUID.
     */
    public static Device getDevice(String uuid) {
        return getDevice(UUID.fromString(uuid));
    }

    /**
     * Returns a List of all Connections belonging to this device.
     * @return A List of all Connections belonging to this device.
     */
    public ConnectionList getConnections() {
        return mConnections;
    }

    /**
     * Adds a new connection to the existing list for this Device.
     * @param connection The new connection.
     */
    public void addConnection(Connection connection) {
        mConnections.add(connection);
        notifyObservers(Status.ConnectionsUpdated);
    }

    /**
     * Removes a connection from the existing list for this Device.
     * @param connection The connection to remove.
     */
    public void removeConnection(Connection connection) {
        mConnections.remove(connection);
        notifyObservers(Status.ConnectionsUpdated);
    }

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

        return hashCode() == o.hashCode();
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

    /**
     * Adds an observer to watch this Device.  Observers are notified of
     * important changes.
     * @param observer The observer to add.
     */
    public void addObserver(DeviceObserver observer) {
        if(observer == null) {
            throw new NullPointerException("DeviceObserver is null!");
        }
        synchronized (this) {
            if(!observers.contains(observer)) {
                observers.add(observer);
            }
        }
    }

    /**
     * Notifies all observers of important changes.
     * @param cue A cue to tell observers what kind of change is happening.
     */
    public void notifyObservers(Status cue) {
        for(DeviceObserver observer : observers) {
            observer.onUpdate(this, cue);
        }
    }
}
