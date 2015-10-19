package org.tec_hub.tecuniversalcomm.data.connection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observer;

/**
 * Created by Nick Mosher on 9/24/15.
 * A wrapper around ArrayList specifically made for handling Connections, including
 * checking for version duplicates and handling observable interactions.
 *
 * @author Nick Mosher, nicholastmosher@gmail.com, https://github.com/nicholastmosher
 */
public class ConnectionList implements Iterable<Connection> {

    private transient List<Observer> mObservers = new ArrayList<>();

    private List<Connection> mConnections = new ArrayList<>();

    /**
     * Adds the given device to this list.  If no version of the
     * device already exists, it is added as a new entry.  If a
     * previous version of the device does exist, replace it.
     *
     * @param newConnection The device to add or update.
     * @return True if we successfully added the device.
     */
    public boolean add(Connection newConnection) {
        //For each connection, check if it's an existing version of this connection.
        for(Connection connection : this) {
            //If the new connection is a version of an existing connection, overwrite the old one.
            if(newConnection.isVersionOf(connection)) {
                int index = mConnections.indexOf(connection);
                if(index != -1) {
                    mConnections.set(index, newConnection);
                    notifyObservers(Connection.Status.MetadataChanged);
                    return true;
                } else {
                    return false;
                }
            }
        }
        //If an existing device was not updated, then this is a new device, add it to the list
        if(mConnections.add(newConnection)) {
            notifyObservers(Connection.Status.MetadataChanged);
            return true;
        }
        return false;
    }

    /**
     * Returns the connection at the given index.
     *
     * @param index The index of the connection.
     * @return The connection at the given index.
     */
    public Connection get(int index) {
        return mConnections.get(index);
    }

    /**
     * Returns the number of connections in this List.
     *
     * @return The number of connections in this List.
     */
    public int size() {
        return mConnections.size();
    }

    /**
     * Checks if the object given matches a version of a Device
     * in this list.  If so, it removes it from the list.
     *
     * @param object The object to check against this list.
     * @return True if we successfully removed the object.
     */
    public boolean remove(Object object) {
        if(object instanceof Connection) {
            Connection connection = (Connection) object;
            for(Connection c : this) {
                if(connection.isVersionOf(c)) {
                    if(mConnections.remove(connection)) {
                        notifyObservers(Connection.Status.MetadataChanged);
                        return true;
                    }
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Implement iterable so we can use a foreach loop.
     *
     * @return The underlying List's iterator.
     */
    @Override
    public Iterator<Connection> iterator() {
        return mConnections.iterator();
    }

    /**
     * Adds an observer to this List, which we call any time a significant change is
     * made (e.g. an addition or deletion).
     *
     * @param observer The new observer that we should keep notified.
     */
    public void addObserver(Observer observer) {
        if(!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    /**
     * Alerts all registered observers about a change in this list's data.
     *
     * @param status The status indicates what type of change was made.
     */
    public void notifyObservers(Connection.Status status) {
        for(Observer observer : mObservers) {
            observer.update(null, status);
        }
    }

    /**
     * Returns the String description of each connection.
     *
     * @return The String description of each connection.
     */
    @Override
    public String toString() {
        return mConnections.toString();
    }
}
