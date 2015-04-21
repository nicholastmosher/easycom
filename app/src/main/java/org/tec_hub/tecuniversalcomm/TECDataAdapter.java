package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.tec_hub.tecuniversalcomm.Connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.Connection.Connection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Nick Mosher on 3/2/2015.
 * This class manages all persistent data of the application.
 * The mode of data storage is through GSON to files
 * where object data is kept.
 */
public class TECDataAdapter {
    public static final String FILE_DEVICES = "devices.ser";

    private static File mDevicesFolder;
    private static File mDevicesFile;
    private static HandlerThread mHandlerThread;
    private static Handler mHandler;
    private static GsonBuilder mGsonBuilder;

    static {
        mHandlerThread = new HandlerThread("TECDataAdapter Handler Thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mGsonBuilder = new GsonBuilder();
        mGsonBuilder.registerTypeAdapter(new ArrayList<Device>().getClass(), new DeviceListTypeAdapter());
    }

    public static void init(Context context) {
        mDevicesFolder = context.getFilesDir();
        mDevicesFile = new File(mDevicesFolder, FILE_DEVICES);
        wipeDevicesFile();
        try {
            //If the file to store device data in doesn't exist, create it.
            mDevicesFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a device to place in storage.
     * @param device The new or updated device to put to persistent storage.
     */
    public static void putDevice(Device device) {
        mHandler.post(new DeviceWriteTask(device));
    }

    /**
     * Removes a specified device from storage.
     * @param device The device object to find and remove from persistent storage.
     */
    public static void deleteDevice(Device device) {
        mHandler.post(new DeviceRemoveTask(device));
    }

    /**
     * Writes the given List of devices to the persistent storage file.
     * @param devices The List of devices being stored.
     */
    public static void writeDevicesToFile(List<Device> devices) {
        Preconditions.checkNotNull(devices);
        if(mDevicesFile.exists()) {
            String json = mGsonBuilder.create().toJson(devices);
            System.out.println("Wrote: " + json);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(mDevicesFile, true);
                fileOutputStream.write(json.getBytes());
                fileOutputStream.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns a List of devices retrieved from the persistent data file.
     * @return a List of devices retrieved from the persistent data file.
     */
    public static List<Device> readDevicesFromFile() {
        List<Device> devices = new ArrayList<>();
        try {
            //Reads a file line-by-line
            BufferedReader bufferedReader = new BufferedReader(
                             new InputStreamReader(
                             new FileInputStream(mDevicesFile)));

            //Read each line of the file into a buffer
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            //If there was no data received, return the empty List.
            String jsonFile = stringBuilder.toString();
            System.out.println("Read: " + jsonFile);
            if(jsonFile.equals("")) {
                return devices;
            }

            List<Device> temp = mGsonBuilder.create().fromJson(jsonFile, new ArrayList<Device>().getClass());
            Preconditions.checkNotNull(temp);
            devices = temp;
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } catch(IllegalStateException ise) {
            ise.printStackTrace();
        }
        return devices;
    }

    /**
     * Wipes all data from the persistent data file.
     */
    public static void wipeDevicesFile() {
        try {
            new PrintWriter(mDevicesFile).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Building block class meant to be passed to TECDataAdapter's Handler,
     * which runs on a separate thread so that this heavy lifting doesn't
     * interfere with the main UI thread.
     */
    private static class DeviceWriteTask implements Runnable {
        private Device mDevice;

        public DeviceWriteTask(Device device) {
            mDevice = Preconditions.checkNotNull(device);
        }

        /**
         * Checks to see if the given device exists in the persistent data.
         * If it does, then it replaces the device instance from the file
         * input with the new, updated version of the device, then rewrites
         * it to the storage file.  Otherwise, it adds the device to a new
         * index of the List that will then be written back to the file.
         */
        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            System.out.println("Begin Device Write at: " + calendar.get(Calendar.SECOND) + calendar.get(Calendar.MILLISECOND));
            List<Device> fileDevices = readDevicesFromFile();
            boolean flagNeedToAdd = true;
            for (Device fileDevice : fileDevices) {
                /*
                 * If the device from the parameter is a version of the existing
                 * file device (regardless if the data is identical or changed),
                 * replace the file device with the parameter device.
                 */
                if (mDevice.isVersionOf(fileDevice)) {
                    int index = fileDevices.indexOf(fileDevice);
                    if (index != -1) {
                        fileDevices.set(index, mDevice);
                        flagNeedToAdd = false;
                    } else {
                        throw new IllegalStateException("Cannot find device index!");
                    }
                }
            }
            if (flagNeedToAdd) {
                fileDevices.add(mDevice);
            }
            wipeDevicesFile();
            writeDevicesToFile(fileDevices);
            System.out.println("End Device Write at: " + calendar.get(Calendar.SECOND) + calendar.get(Calendar.MILLISECOND));
        }
    }

    /**
     * Building block class meant to be passed to TECDataAdapter's Handler,
     * which runs on a separate thread so that this heavy lifting doesn't
     * interfere with the main UI thread.
     */
    private static class DeviceRemoveTask implements Runnable {
        private Device mDevice;
        public DeviceRemoveTask(Device device) {
            mDevice = Preconditions.checkNotNull(device);
        }

        @Override
        public void run() {
            List<Device> fileDevices = Collections.synchronizedList(readDevicesFromFile());
            Preconditions.checkNotNull(fileDevices);
            synchronized (fileDevices) {
                Iterator iterator = fileDevices.iterator();
                while(iterator.hasNext()) {
                    if(mDevice.isVersionOf((Device) iterator.next())) {
                        iterator.remove();
                    }
                }
                wipeDevicesFile();
                writeDevicesToFile(fileDevices);
            }
        }
    }

    /**
     * Special class implementing GSON TypeAdapter.  This is used to tell
     * GSON exactly how to serialize and deserialize any and all "Device"
     * objects it encounters.  This adapter directly calls the
     * ConnectionListTypeAdapter in order to handle the serialization and
     * deserialization of the Connection lists.
     */
    private static final class DeviceListTypeAdapter extends TypeAdapter<List<Device>> {

        /**
         * Name key for device names.
         */
        public static final String DEVICE_NAME = "mName";

        /**
         * Name key for device UUIDs.
         */
        public static final String DEVICE_UUID = "mUUID";

        public static final String DEVICE_CONNECTIONS = "mConnections";

        @Override
        public void write(JsonWriter writer, List<Device> devices) throws IOException {
            //Begin array of devices
            writer.beginArray();
            for(Device device : devices) {
                Preconditions.checkNotNull(device);

                //Begin writing device object
                writer.beginObject();
                writer.name(DEVICE_NAME).value(device.getName()); //Write device name
                writer.name(DEVICE_UUID).value(device.getUUID().toString()); //Write device UUID

                //Begin writing connections array.
                writer.name(DEVICE_CONNECTIONS);
                //Directly call the ConnectionListTypeAdapter as a nested writer.
                new ConnectionListTypeAdapter().write(writer, device.getConnections());

                //End writing this object.
                writer.endObject();
            }
            //End array of devices.
            writer.endArray();
        }

        @Override
        public List<Device> read(JsonReader reader) throws IOException {
            List<Device> devices = new ArrayList<>();

            //Begin reading devices array.
            reader.beginArray();
            while(reader.hasNext()) {
                //Create local variables as a cache for the Device.
                String deviceName = null;
                String deviceUUID = null;
                List<Connection> deviceConnections = new ArrayList<>();

                //Begin reading a new Device object.
                reader.beginObject();
                while(reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals(DEVICE_NAME)) { //Read device name
                        deviceName = reader.nextString();
                    } else if (name.equals(DEVICE_UUID)) { //Read device UUID
                        deviceUUID = reader.nextString();
                    } else if (name.equals(DEVICE_CONNECTIONS) && reader.peek() != JsonToken.NULL) { //Read connections array
                        //Directly call the ConnectionListTypeAdapter as a nested reader.
                        deviceConnections = new ConnectionListTypeAdapter().read(reader);
                    }
                }
                //End reading this device.
                reader.endObject();
                //Add parsed device to the List.
                devices.add(Device.build(deviceName, deviceConnections, deviceUUID));
            }
            reader.endArray(); //End reading device array.
            Preconditions.checkNotNull(devices);
            return devices;
        }
    }

    /**
     * Special class implementing GSON's TypeAdapter.  This is used to tell
     * GSON exactly how to serialize and deserialize Connection Lists.
     */
    private static final class ConnectionListTypeAdapter extends TypeAdapter<List<Connection>> {

        /**
         * Name key of all Connections.
         */
        public static final String CONNECTION_NAME = "mName";

        /**
         * Key of Connection Implementation.
         */
        public static final String CONNECTION_IMPL = "mImpl";

        /**
         * Implementation key of Bluetooth Connections.
         */
        public static final String IMPL_BLUETOOTH = "BluetoothConnection";

        /**
         * Key to store BluetoothConnection address.
         */
        public static final String BLUETOOTH_ADDRESS = "BluetoothAddress";

        @Override
        public void write(JsonWriter writer, List<Connection> connections) throws IOException {
            //Begin array of Connections
            writer.beginArray();
            for(Connection connection : connections) {
                Preconditions.checkNotNull(connection);

                //Begin this Connection
                writer.beginObject();
                writer.name(CONNECTION_NAME).value(connection.getName()); //Write connection name

                //Write all properties specific to BluetoothConnections.
                if(connection instanceof BluetoothConnection) {
                    BluetoothConnection btConnection = (BluetoothConnection) connection;
                    writer.name(CONNECTION_IMPL).value(IMPL_BLUETOOTH); //Write connection implementation
                    writer.name(BLUETOOTH_ADDRESS).value(btConnection.getAddress()); //Write BluetoothConnection address
                }
                //End this Connection
                writer.endObject();
            }
            //End array of Connections
            writer.endArray();
        }

        @Override
        public List<Connection> read(JsonReader reader) throws IOException {
            List<Connection> connections = new ArrayList<>();

            //Begin array of Connections
            reader.beginArray();
            while(reader.hasNext()) {
                //Create local variables as a cache to build a Connection
                String connectionName = null;
                String connectionImpl = null;
                String bluetoothConnectionAddress = null;

                //Begin this Connection
                reader.beginObject();
                while(reader.hasNext()) {
                    String name = reader.nextName();
                    if(name.equals(CONNECTION_NAME)) { //Read Connection name
                        connectionName = reader.nextString();
                    } else if(name.equals(CONNECTION_IMPL)) { //Read Connection implementation
                        connectionImpl = reader.nextString();
                    } else if(name.equals(BLUETOOTH_ADDRESS)) { //Read BluetoothConnection address
                        bluetoothConnectionAddress = reader.nextString();
                    }
                }
                //End this Connection
                reader.endObject();

                //Parse Connection data into object
                Preconditions.checkNotNull(connectionName);
                Preconditions.checkNotNull(connectionImpl);
                if(connectionImpl.equals(IMPL_BLUETOOTH)) { //If this Connection is a BluetoothConnection
                    Preconditions.checkNotNull(bluetoothConnectionAddress);
                    connections.add(new BluetoothConnection(connectionName, bluetoothConnectionAddress));
                }
            }
            //End array of Connections
            reader.endArray();
            return connections;
        }
    }
}