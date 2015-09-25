package org.tec_hub.tecuniversalcomm.data.connection;

import java.util.ArrayList;

/**
 * Created by Nick Mosher on 9/24/15.
 */
public class ConnectionList extends ArrayList<Connection> {

    /**
     * Adds the given device to this list.  If no version of the
     * device already exists, it is added as a new entry.  If a
     * previous version of the device does exist, replace it.
     * @param newConnection The device to add or update.
     * @return True if we successfully added the device.
     */
    @Override
    public boolean add(Connection newConnection) {
        for (Connection connection : this) {
            if (newConnection.isVersionOf(connection)) {
                int index = this.indexOf(connection);
                if(index != -1) {
                    super.set(index, newConnection);
                    return true;
                } else {
                    return false;
                }
            }
        }
        //If an existing device was not updated, then this is a new device, add it to the list
        return super.add(newConnection);
    }

    /**
     * Checks if the object given matches a version of a Device
     * in this list.  If so, it removes it from the list.
     * @param object The object to check against this list.
     * @return True if we successfully removed the object.
     */
    @Override
    public boolean remove(Object object) {
        if(object instanceof Connection) {
            Connection device = (Connection) object;
            for(Connection c : this) {
                if(device.isVersionOf(c)) {
                    return super.remove(device);
                }
            }
        }
        return false;
    }
}
