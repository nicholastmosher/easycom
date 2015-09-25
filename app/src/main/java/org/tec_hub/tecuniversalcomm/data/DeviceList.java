package org.tec_hub.tecuniversalcomm.data;

import java.util.ArrayList;

/**
 * Created by Nick Mosher on 9/24/15.
 */
public class DeviceList extends ArrayList<Device> {

    /**
     * Adds the given device to this list.  If no version of the
     * device already exists, it is added as a new entry.  If a
     * previous version of the device does exist, replace it.
     * @param newDevice The device to add or update.
     * @return True if we successfully added the device.
     */
    @Override
    public boolean add(Device newDevice) {
        for (Device device : this) {
            if (newDevice.isVersionOf(device)) {
                int index = this.indexOf(device);
                if(index != -1) {
                    super.set(index, newDevice);
                    return true;
                } else {
                    return false;
                }
            }
        }
        //If an existing device was not updated, then this is a new device, add it to the list
        return super.add(newDevice);
    }

    /**
     * Checks if the object given matches a version of a Device
     * in this list.  If so, it removes it from the list.
     * @param object The object to check against this list.
     * @return True if we successfully removed the object.
     */
    @Override
    public boolean remove(Object object) {
        if(object instanceof Device) {
            Device device = (Device) object;
            for(Device d : this) {
                if(device.isVersionOf(d)) {
                    return super.remove(device);
                }
            }
        }
        return false;
    }
}
