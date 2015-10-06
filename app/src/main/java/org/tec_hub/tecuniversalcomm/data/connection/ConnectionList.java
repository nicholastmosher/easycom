package org.tec_hub.tecuniversalcomm.data.connection;

import com.google.common.base.Preconditions;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Nick Mosher on 9/24/15.
 */
public class ConnectionList implements Iterable<Connection> {

    private static transient ConnectionListTypeAdapter mTypeAdapter = new ConnectionListTypeAdapter();

    private transient List<ConnectionObserver> mObservers = new ArrayList<>();

    private List<Connection> mConnections = new ArrayList<>();

    /**
     * Adds the given device to this list.  If no version of the
     * device already exists, it is added as a new entry.  If a
     * previous version of the device does exist, replace it.
     * @param newConnection The device to add or update.
     * @return True if we successfully added the device.
     */
    public boolean add(Connection newConnection) {
        for (Connection connection : this) {
            if (newConnection.isVersionOf(connection)) {
                int index = mConnections.indexOf(connection);
                if(index != -1) {
                    mConnections.set(index, newConnection);
                    notifyObservers(Connection.Status.DataChanged);
                    return true;
                } else {
                    return false;
                }
            }
        }
        //If an existing device was not updated, then this is a new device, add it to the list
        if(mConnections.add(newConnection)) {
            notifyObservers(Connection.Status.DataChanged);
            return true;
        }
        return false;
    }

    public Connection get(int i) {
        return mConnections.get(i);
    }

    public int size() {
        return mConnections.size();
    }

    /**
     * Checks if the object given matches a version of a Device
     * in this list.  If so, it removes it from the list.
     * @param object The object to check against this list.
     * @return True if we successfully removed the object.
     */
    public boolean remove(Object object) {
        if(object instanceof Connection) {
            Connection connection = (Connection) object;
            for(Connection c : this) {
                if(connection.isVersionOf(c)) {
                    if(mConnections.remove(connection)) {
                        notifyObservers(Connection.Status.DataChanged);
                        return true;
                    }
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public Iterator<Connection> iterator() {
        return mConnections.iterator();
    }

    public void addObserver(ConnectionObserver observer) {
        if(!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    public void notifyObservers(Connection.Status status) {
        for(ConnectionObserver observer : mObservers) {
            observer.onUpdate(status);
        }
    }

    @Override
    public String toString() {
        return mConnections.toString();
    }

    public static ConnectionListTypeAdapter getTypeAdapter() {
        return mTypeAdapter;
    }

    /**
     * Special class implementing GSON's TypeAdapter.  This is used to tell
     * GSON exactly how to serialize and deserialize Connection Lists.
     */
    private static final class ConnectionListTypeAdapter extends TypeAdapter<ConnectionList> {

        /** Name key of all Connections.*/
        public static final String CONNECTION_NAME = "name";
        /** Key of Connection Implementation.*/
        public static final String CONNECTION_IMP = "imp";
        /** Key of Connection Universal Identifier.*/
        public static final String CONNECTION_UUID = "uuid";

        //Implementations of Connection
        /** Implementation key of Bluetooth Connections.*/
        public static final String IMP_BLUETOOTH = "impBt";
        /** Implementation key of TCPIP Connections.*/
        public static final String IMP_TCPIP = "impTcp";

        //BluetoothConnection specific data
        /** Key to store BluetoothConnection address.*/
        public static final String BLUETOOTH_ADDRESS = "btAddr";

        //TcpIpConnection specific data
        /** Key to store TcpIp remote Ip.*/
        public static final String TCPIP_IP = "tcpIp";
        /** Key to store TcpIp remote Port.*/
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
