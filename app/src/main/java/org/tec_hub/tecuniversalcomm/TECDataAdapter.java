package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Nick Mosher on 3/2/2015.
 * This class manages all persistent data of the application.
 * The mode of data storage is through Serialization to files
 * where object data is kept.
 */
public class TECDataAdapter
{
    public static final String FILE_DEVICES = "devices.ser";
    private static final Handler DEVICE_HANDLER = new Handler();

    private static File devicesFolder;
    private static File devicesFile;

    public static void init(Context context)
    {
        devicesFolder = context.getFilesDir();
        devicesFile = new File(devicesFolder, FILE_DEVICES);
        try
        {
            //If the file to store device data in doesn't exist, create it.
            devicesFile.createNewFile();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Custom class implementing Runnable is needed so that we can pass custom parameters into the
     * constructor.  In this case, the parameter is the Device that needs to be added to persistent
     * data.  This runnable object is intended to be passed to the DEVICE_HANDLER using post() so
     * that it will be placed in a queue to process on a separate thread, freeing up the main thread
     * to return to the main execution.
     */
    private static class DeviceWriter implements Runnable
    {
        private Device device;
        public DeviceWriter(Device device)
        {
            this.device = device;
        }

        /**
         * Checks to see if the given device exists in the persistent data.
         * If it does, then it replaces the device instance from the file
         * input with the new, updated version of the device, then rewrites
         * it to the storage file.  Otherwise, it adds the device to a new
         * index of the ArrayList that will then be written back to the file.
         */
        @Override
        public void run()
        {
            ArrayList<Device> devices = readDevicesFromFile();
            boolean flagDuplicate = false;
            for(Device d : devices)
            {
                if(device.hashCode() == d.hashCode())
                {
                    //If the hashcode is the same, then the data is identical.
                    flagDuplicate = true;
                    break;
                }
            }

            if(!flagDuplicate)
            {
                devices.add(device);
            }
            wipeDevicesFile();
            writeDevicesToFile(devices);
        }
    }

    /**
     * Custom class implementing Runnable is intended to be passed to the DEVICE_HANDLER using
     * post() in order to add it to a queue that runs on a separate thread, thus moving the heavy
     * lifting of this timing-insensitive task off of the main thread.
     */
    public static class DeviceDeleter implements Runnable
    {
        private Device device;
        public DeviceDeleter(Device device)
        {
            this.device = device;
        }

        @Override
        public void run()
        {
            ArrayList<Device> devices = readDevicesFromFile();
            for(Iterator<Device> i = devices.iterator(); i.hasNext(); )
            {
                Device d = i.next();
                if(d.equals(device))
                {
                    i.remove();
                }
            }
            wipeDevicesFile();
            writeDevicesToFile(devices);
        }
    }

    /**
     *
     * @param device The new or updated device to put to persistent storage.
     */
    public static void putDevice(Device device)
    {
        DEVICE_HANDLER.post(new DeviceWriter(device));
    }

    public static void deleteDevice(Device device)
    {
        DEVICE_HANDLER.post(new DeviceDeleter(device));
    }

    /**
     * Writes the given ArrayList of devices to the persistent storage file.
     * @param devices The ArrayList of devices being stored.
     */
    public static void writeDevicesToFile(ArrayList<Device> devices)
    {
        if(devicesFile.exists())
        {
            try
            {
                FileOutputStream fileOutputStream = new FileOutputStream(devicesFile);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(devices);
                objectOutputStream.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns an ArrayList of devices retrieved from the persistent data file.
     * @return an ArrayList of devices retrieved from the persistent data file.
     */
    public static ArrayList<Device> readDevicesFromFile()
    {
        ArrayList<Device> fileDevices = new ArrayList<Device>();
        if(devicesFile.exists())
        {
            try
            {
                FileInputStream fileInputStream = new FileInputStream(devicesFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                fileDevices = (ArrayList<Device>) objectInputStream.readObject();
                objectInputStream.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            catch(ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        return fileDevices;
    }

    /**
     * Wipes all data from the persistent data file.
     */
    private static void wipeDevicesFile()
    {
        try
        {
            new PrintWriter(devicesFile).close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}