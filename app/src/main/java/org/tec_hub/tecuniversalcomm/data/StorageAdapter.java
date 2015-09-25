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

import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.data.connection.Connection;
import org.tec_hub.tecuniversalcomm.data.connection.ConnectionList;
import org.tec_hub.tecuniversalcomm.data.connection.TcpIpConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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

    /*
     * Since the StorageAdapter is used entirely statically, this
     * initializes all values so we don't get any null pointer
     * exceptions.
     */
    static {
        mHandlerThread = new HandlerThread("StorageAdapter Handler Thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mGsonBuilder = new GsonBuilder();
        mDeviceListType = new TypeToken<DeviceList>(){}.getType();
        mConnectionListType = new TypeToken<ConnectionList>(){}.getType();

        mGsonBuilder.registerTypeAdapter(mConnectionListType, new ConnectionListTypeAdapter());
        mGson = mGsonBuilder.create();
    }

    /**
     * Initializes the StorageAdapter within a certain context.  This is
     * how the StorageAdapter has access to the filesystem and such.
     * @param context The context of the application.
     */
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
    public static void setDevices(DeviceList devices) {
        mHandler.post(new WriteDevicesTask(devices));
    }

    public static DeviceList getDevices() {
        return readDevicesFromFile();
    }

    /**
     * Writes the given List of devices to the persistent storage file.
     * This overwrites the existing storage.
     * @param devices The List of devices being stored.
     */
    private static void writeDevicesToFile(DeviceList devices) {
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
    private static DeviceList readDevicesFromFile() {
        DeviceList devices = new DeviceList();
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

            DeviceList temp = mGson.fromJson(jsonFile, mDeviceListType);
            Preconditions.checkNotNull(temp);
            devices = temp;
        } catch(IOException | IllegalStateException ioe) {
            ioe.printStackTrace();
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

        /**
         * Constructs a new AddDeviceTask with a reference to a device to
         * write to the persistent data file.
         * @param device The device to write to storage.
         */
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
            DeviceList fileDevices = readDevicesFromFile();
            fileDevices.add(mDevice);
            writeDevicesToFile(fileDevices);
        }
    }

    /**
     * This class can be instantiated to run on a handler in a different thread.
     * At construction, this class takes a List of Devices and writes it into
     * the persistent storage, erasing all previous data.
     */
    private static class WriteDevicesTask implements Runnable {
        private DeviceList mDevices;
        public WriteDevicesTask(DeviceList devices) {
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
        public synchronized void run() {
            DeviceList fileDevices = readDevicesFromFile();
            Preconditions.checkNotNull(fileDevices);
            fileDevices.remove(mDevice);
            writeDevicesToFile(fileDevices);
        }
    }

    /**
     * Special class implementing GSON's TypeAdapter.  This is used to tell
     * GSON exactly how to serialize and deserialize Connection Lists.
     */
    private static final class ConnectionListTypeAdapter extends TypeAdapter<ConnectionList> {

        /**
         * Name key of all Connections.
         */
        public static final String CONNECTION_NAME = "name";

        /**
         * Key of Connection Implementation.
         */
        public static final String CONNECTION_IMP = "imp";

        //Implementations of Connection
        /**
         * Implementation key of Bluetooth Connections.
         */
        public static final String IMP_BLUETOOTH = "impBt";

        /**
         * Implementation key of TCPIP Conncetions.
         */
        public static final String IMP_TCPIP = "impTcp";

        //BluetoothConnection specific data
        /**
         * Key to store BluetoothConnection address.
         */
        public static final String BLUETOOTH_ADDRESS = "btAddr";

        //TcpIpConnection specific data
        /**
         * Key to store TcpIp remote Ip.
         */
        public static final String TCPIP_IP = "tcpIp";

        /**
         * Key to store TcpIp remote Port.
         */
        public static final String TCPIP_PORT = "tcpPort";

        /**
         * Takes a List of Connections and writes them to the JsonWriter as JSON objects.
         * @param writer The JsonWriter to write the objects into.
         * @param connections The Connections data to convert into JSON.
         * @throws IOException
         */
        @Override
        public void write(JsonWriter writer, ConnectionList connections) throws IOException {
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
                    writer.name(CONNECTION_IMP).value(IMP_BLUETOOTH); //Write connection implementation
                    writer.name(BLUETOOTH_ADDRESS).value(btConnection.getAddress()); //Write BluetoothConnection address

                //Write all properties specific to TcpIpConnections.
                } else if(connection instanceof TcpIpConnection) {
                    TcpIpConnection tcpIpConnection = (TcpIpConnection) connection;
                    writer.name(CONNECTION_IMP).value(IMP_TCPIP);
                    writer.name(TCPIP_IP).value(tcpIpConnection.getServerIp());
                    writer.name(TCPIP_PORT).value(tcpIpConnection.getServerPort());
                }

                //End this Connection
                writer.endObject();
            }
            //End array of Connections
            writer.endArray();
        }

        /**
         * Parses data from a JsonReader back into a List of Connections.
         * @param reader The source of a JSON string to convert.
         * @return A List of Connections parsed from the reader.
         * @throws IOException
         */
        @Override
        public ConnectionList read(JsonReader reader) throws IOException {
            ConnectionList connections = new ConnectionList();

            //Begin array of Connections
            reader.beginArray();
            while(reader.hasNext()) {
                //Create local variables as a cache to build a Connection
                String connectionName = null;
                String imp = null;
                String btAddr = null;
                String tcpIp = null;
                int tcpPort = -1;

                //Begin this Connection
                reader.beginObject();
                while(reader.hasNext()) {
                    String name = reader.nextName();
                    switch(name) {
                        case CONNECTION_NAME: //Read Connection name
                            connectionName = reader.nextString();
                            break;
                        case CONNECTION_IMP: //Read Connection implementation
                            imp = reader.nextString();
                            break;
                        case BLUETOOTH_ADDRESS: //Read BluetoothConnection address
                            btAddr = reader.nextString();
                            break;
                        case TCPIP_IP:
                            tcpIp = reader.nextString();
                            break;
                        case TCPIP_PORT:
                            tcpPort = reader.nextInt();
                            break;
                        default:
                    }
                }
                //End this Connection
                reader.endObject();

                //Parse Connection data into object
                Connection connection = null;
                Preconditions.checkNotNull(connectionName);
                Preconditions.checkNotNull(imp);

                if(imp.equals(IMP_BLUETOOTH)) { //If this Connection is a BluetoothConnection
                    Preconditions.checkNotNull(btAddr);
                    connection = new BluetoothConnection(connectionName, btAddr);

                } else if(imp.equals(IMP_TCPIP)) {
                    Preconditions.checkNotNull(tcpIp);
                    if(tcpPort == -1) throw new IllegalStateException("Port was not read!");
                    connection = new TcpIpConnection(connectionName, tcpIp, tcpPort);
                }

                if(connection != null) {
                    connections.add(connection);
                } else {
                    System.err.println("Failed to read connection: " + connectionName);
                }
            }
            //End array of Connections
            reader.endArray();
            return connections;
        }
    }
}