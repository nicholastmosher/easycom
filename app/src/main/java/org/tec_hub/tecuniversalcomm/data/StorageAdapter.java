package org.tec_hub.tecuniversalcomm.data;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.tec_hub.tecuniversalcomm.Device;
import org.tec_hub.tecuniversalcomm.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.connection.Connection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Nick Mosher on 3/2/2015.
 * This class manages all persistent data of the application.
 * The mode of data storage is through GSON to files
 * where object data is kept.
 */
public class StorageAdapter {
    public static final String FILE_DEVICES = "devices.ser";

    private static File mDevicesFolder;
    private static File mDevicesFile;
    private static HandlerThread mHandlerThread;
    private static Handler mHandler;
    private static GsonBuilder mGsonBuilder;
    private static Type mDeviceListType;
    private static Type mConnectionListType;
    private static Gson mGson;

    static {
        mHandlerThread = new HandlerThread("StorageAdapter Handler Thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mGsonBuilder = new GsonBuilder();
        mDeviceListType = new TypeToken<List<Device>>(){}.getType();
        mConnectionListType = new TypeToken<List<Connection>>(){}.getType();

        mGsonBuilder.registerTypeAdapter(mConnectionListType, new ConnectionListTypeAdapter());
        mGson = mGsonBuilder.create();
    }

    public static void init(Context context) {
        mDevicesFolder = context.getFilesDir();
        mDevicesFile = new File(mDevicesFolder, FILE_DEVICES);
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
        mHandler.post(new AddDeviceTask(device));
    }

    /**
     * Removes a specified device from storage.
     * @param device The device object to find and remove from persistent storage.
     */
    public static void deleteDevice(Device device) {
        mHandler.post(new RemoveDeviceTask(device));
    }

    /**
     * Puts the specified list of devices as the sole object in persistent storage.
     * @param devices The list of devices to write to disk.
     */
    public static void setDevices(List<Device> devices) {
        mHandler.post(new SetDevicesTask(devices));
    }

    public static List<Device> getDevices() {
        return readDevicesFromFile();
    }

    /**
     * Writes the given List of devices to the persistent storage file.
     * This overwrites the existing storage.
     * @param devices The List of devices being stored.
     */
    private static void writeDevicesToFile(List<Device> devices) {
        Preconditions.checkNotNull(devices);
        wipeDevicesFile();
        if(mDevicesFile.exists()) {
            String json = mGson.toJson(devices);
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
    private static List<Device> readDevicesFromFile() {
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

            List<Device> temp = mGson.fromJson(jsonFile, mDeviceListType);
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
     * Building block class meant to be passed to StorageAdapter's Handler,
     * which runs on a separate thread so that this heavy lifting doesn't
     * interfere with the main UI thread.
     */
    private static class AddDeviceTask implements Runnable {
        private Device mDevice;

        public AddDeviceTask(Device device) {
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
            List<Device> fileDevices = readDevicesFromFile();
            boolean flagUpdateExisting = false;
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
                        flagUpdateExisting = true;
                        break;
                    } else {
                        throw new IllegalStateException("[StorageAdapter.AddDeviceTask.run] Cannot find device index!");
                    }
                }
            }
            if (!flagUpdateExisting) {
                fileDevices.add(mDevice);
            }
            writeDevicesToFile(fileDevices);
        }
    }

    private static class SetDevicesTask implements Runnable {
        private List<Device> mDevices;
        public SetDevicesTask(List<Device> devices) {
            mDevices = devices;
        }

        @Override
        public void run() {
            writeDevicesToFile(mDevices);
        }
    }

    /**
     * Building block class meant to be passed to StorageAdapter's Handler,
     * which runs on a separate thread so that this heavy lifting doesn't
     * interfere with the main UI thread.
     */
    private static class RemoveDeviceTask implements Runnable {
        private Device mDevice;
        public RemoveDeviceTask(Device device) {
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
                writeDevicesToFile(fileDevices);
            }
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

        public static final String CONNECTION_ON_CONNECT_STATUS_CHANGED_LISTENERS = "mOnStatusChangedListeners";

        /**
         * Key of Connection Implementation.
         */
        public static final String CONNECTION_IMPL = "mImpl";

        //Implementations of Connection
        /**
         * Implementation key of Bluetooth Connections.
         */
        public static final String IMPL_BLUETOOTH = "BluetoothConnection";

        //BluetoothConnection specific data
        /**
         * Key to store BluetoothConnection address.
         */
        public static final String BLUETOOTH_ADDRESS = "BluetoothAddress";

        private Type mOnConnectStatusChangedListenersMapType = new TypeToken<Map<Context, Connection.OnStatusChangedListener>>(){}.getType();

        @Override
        public void write(JsonWriter writer, List<Connection> connections) throws IOException {
            //Begin array of Connections
            writer.beginArray();
            for(Connection connection : connections) {
                Preconditions.checkNotNull(connection);

                //Begin this Connection
                writer.beginObject();
                writer.name(CONNECTION_NAME).value(connection.getName()); //Write connection name

                //Give back to Gson for writing map
                writer.name(CONNECTION_ON_CONNECT_STATUS_CHANGED_LISTENERS);
                mGson.toJson(connection.getOnConnectStatusChangedListeners(), mOnConnectStatusChangedListenersMapType, writer);

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
                Map<Context, Connection.OnStatusChangedListener> onConnectStatusChangedListenerMap = null;
                String connectionImpl = null;
                String bluetoothConnectionAddress = null;

                //Begin this Connection
                reader.beginObject();
                while(reader.hasNext()) {
                    String name = reader.nextName();
                    if(name.equals(CONNECTION_NAME)) { //Read Connection name
                        connectionName = reader.nextString();
                    } else if(name.equals(CONNECTION_ON_CONNECT_STATUS_CHANGED_LISTENERS)) { //Read listeners map
                        onConnectStatusChangedListenerMap = mGson.fromJson(reader, mOnConnectStatusChangedListenersMapType);
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
                Preconditions.checkNotNull(onConnectStatusChangedListenerMap);
                if(connectionImpl.equals(IMPL_BLUETOOTH)) { //If this Connection is a BluetoothConnection
                    Preconditions.checkNotNull(bluetoothConnectionAddress);
                    BluetoothConnection bluetoothConnection = new BluetoothConnection(connectionName, bluetoothConnectionAddress);
                    bluetoothConnection.setOnStatusChangedListeners(onConnectStatusChangedListenerMap);
                    connections.add(bluetoothConnection);
                }
            }
            //End array of Connections
            reader.endArray();
            return connections;
        }
    }
}