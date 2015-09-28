package org.tec_hub.tecuniversalcomm.intents;

/**
 * Created by Nick Mosher on 4/17/15.
 * Defines intent constants for transmitting data across this app.
 */
public interface TECIntent {

    //Actions///////////////////////////////////////////////////////////////////////////////////////
    /**
     * Sent to the ConnectionService by any context that wants
     * to initiate a BluetoothConnection.  The intent sent to the
     * ConnectionService must have the BluetoothConnection in
     * question added as a Parcelable Extra using the key
     * TECIntent.BLUETOOTH_CONNECTION_UUID.
     */
    String ACTION_BLUETOOTH_CONNECT = "org.tec_hub.tecuniversalcomm.BLUETOOTH_CONNECT";

    /**
     * Sent to the ConnectionService by any context that wants
     * to disconnect a BluetoothConnection.  The intent sent to the
     * ConnectionService must have the BluetoothConnection in
     * question added as a Parcelable Extra using the key
     * TECIntent.BLUETOOTH_CONNECTION_UUID.
     */
    String ACTION_BLUETOOTH_DISCONNECT = "org.tec_hub.tecuniversalcomm.BLUETOOTH_DISCONNECT";

    /**
     * Sent to the ConnectionService by any context that wants to
     * send data over a BluetoothConnection.  The intent sent to the
     * ConnectionService must have the BluetoothConnection in
     * question added as a Parcelable Extra using the key
     * TECIntent.BLUETOOTH_CONNECTION_UUID.
     */
    String ACTION_BLUETOOTH_SEND_DATA = "org.tec_hub.tecuniversalcomm.BLUETOOTH_TO_SEND_DATA";

    /**
     * Used to indicate that a BluetoothConnection has been discovered and report it back to some
     * wanting Activity/Service.
     */
    String ACTION_BLUETOOTH_DISCOVERED = "org.tec_hub.tecuniversalcomm.BLUETOOTH_DISCOVERED";

    /**
     * Sent to the TcpIpConnectionService by any context that wants
     * to connect a TcpIpConnection.  The intent sent to the
     * TcpIpConnectionService must have the TcpIpConnection in question
     * added as a Parcelable Extra using the key TECIntent.CONNECTION_UUID.
     */
    String ACTION_TCPIP_CONNECT = "org.tec_hub.tecuniversalcomm.TCPIP_CONNECT";

    /**
     * Sent to the TcpIpConnectionService by any context that wants
     * to disconnect a TcpIpConnection.  The intent sent to the
     * TcpIpConnectionService must have the TcpIpConnection in question
     * added as a Parcelable Extra using the key TECIntent.CONNECTION_UUID.
     */
    String ACTION_TCPIP_DISCONNECT = "org.tec_hub.tecuniversalcomm.TCPIP_DISCONNECT";

    /**
     * Sent to the TcpIpConnectionService by any context that wants to send data
     * over a TcpIpConnection.  The intent sent to the TcpIpConnectionService must
     * have the TcpIpConnection added as a Parcelable Extra with the key TECIntent.CONNECTION_UUID,
     * and that TcpIpConnection must already be connected.
     */
    String ACTION_TCPIP_SEND_DATA = "org.tec_hub.tecuniversalcomm.TCPIP_TO_SEND_DATA";

    /**
     * Broadcast by any Service or Activity that has discovered a new connection and is returning
     * it to a waiting Activity or Service.
     */
    String ACTION_TCPIP_DISCOVERED = "org.tec_hub.tecuniversalcomm.TCPIP_DISCOVREED";

    /**
     * Used by the ConnectionService to broadcast updates to the
     * app that it has received data from a Connection.  To indicate
     * which Connection the data originated from, the broadcast
     * intent contains a Parcelable Extra of the Connection of the
     * type specified at Extra TECIntent.CONNECTION_TYPE.
     */
    String ACTION_RECEIVED_DATA = "org.tec_hub.tecuniversalcomm.RECEIVED_DATA";





    //Extras////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Used to bundle the data that will be sent over a BluetoothConnection into an intent.
     * The intent using this as an Extra key should have the action "ACTION_BLUETOOTH_SEND_DATA".
     */
    String BLUETOOTH_TO_SEND_DATA = "bluetooth_to_send_data";

    /**
     * Used to bundle the data that will be sent over a TcpIpConnection into an intent.
     * The intent using this as an Extra key should have the action "ACTION_TCPIP_SEND_DATA".
     */
    String TCPIP_TO_SEND_DATA = "tcpip_to_send_data";

    /**
     * Used by any intent carrying a Connection UUID as an Extra.
     */
    String CONNECTION_UUID = "connection_uuid";

    /**
     * Used as an Extra key for any intent carrying a Device as a Parcelable.
     */
    String DEVICE_UUID = "device_uuid";

    /**
     * Used by the ConnectionService to indicate data has been received
     * by an active Connection.
     */
    String RECEIVED_DATA = "received_data";

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
}
