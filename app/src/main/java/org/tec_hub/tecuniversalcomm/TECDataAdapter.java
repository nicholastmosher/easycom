package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.os.AsyncTask;

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
    private static Gson mGson;

    public static void init(Context context) {
        mDevicesFolder = context.getFilesDir();
        mDevicesFile = new File(mDevicesFolder, FILE_DEVICES);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(new ArrayList<Device>().getClass(), new DeviceListTypeAdapter());
        mGson = gsonBuilder.create();

        try {
            //If the file to store device data in doesn't exist, create it.
            mDevicesFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Custom AsyncTask used to manage writing device data on a separate thread
     * than the UI thread.
     */
    private static class DeviceWriteTask extends AsyncTask<Device, Void, Boolean> {
        /**
         * Checks to see if the given device exists in the persistent data.
         * If it does, then it replaces the device instance from the file
         * input with the new, updated version of the device, then rewrites
         * it to the storage file.  Otherwise, it adds the device to a new
         * index of the ArrayList that will then be written back to the file.
         */
        @Override
        protected Boolean doInBackground(Device... paramDevices) {
            ArrayList<Device> fileDevices = readDevicesFromFile();
            for (Device paramDevice : paramDevices) {
                boolean flagNeedToAdd = true;
                for (Device fileDevice : fileDevices) {
                    /*
                     * If the device from the parameter is a version of the existing
                     * file device (regardless if the data is identical or changed),
                     * replace the file device with the parameter device.
                     */
                    if (paramDevice.isVersionOf(fileDevice)) {
                        int index = fileDevices.indexOf(fileDevice);
                        if(index != -1) {
                            fileDevices.set(index, paramDevice);
                            flagNeedToAdd = false;
                        } else {
                            throw new IllegalStateException("Cannot find device index!");
                        }
                    }
                }
                if (flagNeedToAdd) {
                    fileDevices.add(paramDevice);
                }
            }
            wipeDevicesFile();
            writeDevicesToFile(fileDevices);
            return true;
        }
    }

    /**
     * Async Task manages removing devices on a separate thread than the UI.
     * FIXME Individual deleting does not work at all currently.
     */
    public static class DeviceRemoveTask extends AsyncTask<Device, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Device... paramDevices) {
            ArrayList<Device> fileDevices = readDevicesFromFile();
            if(fileDevices != null) {
                for (Device paramDevice : paramDevices) {
                    for (Device fileDevice : fileDevices) {
                        if (paramDevice.equals(fileDevice)) {
                            boolean success = fileDevices.remove(paramDevice);
                            System.out.println("Removed Device " + paramDevice.getName() + ": " + (success ? "True" : "False"));
                        }
                    }
                }
                wipeDevicesFile();
                writeDevicesToFile(fileDevices);
                return true;
            }
            return false;
        }
    }

    /**
     * Adds a device to place in storage.
     * @param device The new or updated device to put to persistent storage.
     */
    public static void putDevice(Device device) {
        new DeviceWriteTask().execute(device);
    }

    /**
     * Removes a specified device from storage.
     * @param device The device object to find and remove from persistent storage.
     */
    public static void deleteDevice(Device device) {
        new DeviceRemoveTask().execute(device);
    }

    /**
     * Writes the given ArrayList of devices to the persistent storage file.
     * @param devices The ArrayList of devices being stored.
     */
    public static void writeDevicesToFile(ArrayList<Device> devices) {
        Preconditions.checkNotNull(devices);
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
     * Returns an ArrayList of devices retrieved from the persistent data file.
     *
     * @return an ArrayList of devices retrieved from the persistent data file.
     */
    public static ArrayList<Device> readDevicesFromFile() {
        ArrayList<Device> devices = new ArrayList<>();
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

            //If there was no data received, return the empty ArrayList.
            String jsonFile = stringBuilder.toString();
            System.out.println("Read: " + jsonFile);
            if(jsonFile == null || jsonFile.equals("")) {
                return devices;
            }

            //Type collectionType = new TypeToken<ArrayList<Device>>(){}.getType();
            ArrayList<Device> temp = mGson.fromJson(jsonFile, new ArrayList<Device>().getClass());
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

    private static final class DeviceListTypeAdapter extends TypeAdapter<ArrayList<Device>> {

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
        public void write(JsonWriter writer, ArrayList<Device> devices) throws IOException {
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
                new ConnectionListTypeAdapter().write(writer, device.getConnections());

                //End writing this object.
                writer.endObject();
            }
            //End array of devices.
            writer.endArray();
        }

        @Override
        public ArrayList<Device> read(JsonReader reader) throws IOException {
            ArrayList<Device> devices = new ArrayList<>();

            //Begin reading devices array.
            reader.beginArray();
            while(reader.hasNext()) {
                //Create local variables as a cache for the Device.
                String deviceName = null;
                String deviceUUID = null;
                ArrayList<Connection> deviceConnections = new ArrayList<>();

                //Begin reading a new Device object.
                reader.beginObject();
                while(reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals(DEVICE_NAME)) { //Read device name
                        deviceName = reader.nextString();
                    } else if (name.equals(DEVICE_UUID)) { //Read device UUID
                        deviceUUID = reader.nextString();
                    } else if (name.equals(DEVICE_CONNECTIONS) && reader.peek() != JsonToken.NULL) { //Read connections array
                        deviceConnections = new ConnectionListTypeAdapter().read(reader);
                    }
                }
                //End reading this device.
                reader.endObject();
                //Add parsed device to ArrayList.
                devices.add(new Device(deviceName, deviceConnections, deviceUUID));
            }
            reader.endArray(); //End reading device array.
            Preconditions.checkNotNull(devices);
            return devices;
        }
    }

    private static final class ConnectionListTypeAdapter extends TypeAdapter<ArrayList<Connection>> {

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
        public void write(JsonWriter writer, ArrayList<Connection> connections) throws IOException {
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
        public ArrayList<Connection> read(JsonReader reader) throws IOException {
            ArrayList<Connection> connections = new ArrayList<>();

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