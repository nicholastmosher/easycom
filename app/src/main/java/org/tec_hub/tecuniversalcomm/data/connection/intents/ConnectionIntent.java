package org.tec_hub.tecuniversalcomm.data.connection.intents;

/**
 * Created by Nick Mosher on 4/17/15.
 * Defines intent constants for transmitting data across this app.
 *
 * @author Nick Mosher, nicholastmosher@gmail.com, https://github.com/nicholastmosher
 */
public interface ConnectionIntent {

    //Actions///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Action for intents that initiate the connection process in ConnectionService.
     */
    String ACTION_CONNECT = "org.tec_hub.tecuniversalcomm.CONNECT";

    /**
     * Action for intents that initiate the disconnection process in ConnectionService.
     */
    String ACTION_DISCONNECT = "org.tec_hub.tecuniversalcomm.DISCONNECT";

    /**
     * Action for intents that initiate the sending process in ConnectionService.
     */
    String ACTION_SEND_DATA = "org.tec_hub.tecuniversalcomm.SEND_DATA";

    /**
     * Action for intents notifying about newly entered connections.
     */
    String ACTION_NEW_CONNECTION = "org.tec_hub.tecuniversalcomm.NEW_CONNECTION";

    /**
     * Used by the ConnectionService to broadcast updates to the
     * app that it has received data from a Connection.
     */
    String ACTION_RECEIVED_DATA = "org.tec_hub.tecuniversalcomm.RECEIVED_DATA";

    //Extras////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Key for the data in DataSendIntents.
     */
    String SEND_DATA = "send_data";

    /**
     * Key for the data in DataReceiveIntents.
     */
    String RECEIVED_DATA = "received_data";

    /**
     * Key for the UUID of any connection reference in an intent.
     */
    String CONNECTION_UUID = "connection_uuid";

    /**
     * Key for placing "Connection Type" extras into intents.
     */
    String CONNECTION_TYPE = "connection_type";

    /**
     * Value for "Connection Type" extra in Bluetooth intents.
     */
    String CONNECTION_TYPE_BLUETOOTH = "connection_type_bluetooth";

    /**
     * Value for "Connection Type" extra in TCPIP intents.
     */
    String CONNECTION_TYPE_TCPIP = "connection_type_tcpip";

    /**
     * Value for "Connection Type" extra in USB intents.
     */
    String CONNECTION_TYPE_USB = "connection_type_usb";

    /**
     * Sends this intent using Android's global broadcast system.
     */
    void send();

    /**
     * Sends this intent using a broadcast system local to this application.
     */
    void sendLocal();
}
