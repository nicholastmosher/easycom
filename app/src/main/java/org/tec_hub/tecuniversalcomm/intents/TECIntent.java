package org.tec_hub.tecuniversalcomm.intents;

import android.content.Intent;

/**
 * Created by Nick Mosher on 4/17/15.
 * Defines intent constants for transmitting data across this app.
 */
public interface TECIntent {

    //Actions///////////////////////////////////////////////////////////////////////////////////////
    /**
     * Sent to the BluetoothConnectionService by any context that wants
     * to initiate a BluetoothConnection.  The intent sent to the
     * BluetoothConnectionService must have the BluetoothConnection in
     * question added as a Parcelable Extra using the key
     * TECIntent.BLUETOOTH_CONNECTION_DATA.
     */
    public static final String ACTION_BLUETOOTH_CONNECT = "org.tec_hub.tecuniversalcomm.BLUETOOTH_CONNECT";

    /**
     * Sent to the BluetoothConnectionService by any context that wants
     * to disconnect a BluetoothConnection.  The intent sent to the
     * BluetoothConnectionService must have the BluetoothConnection in
     * question added as a Parcelable Extra using the key
     * TECIntent.BLUETOOTH_CONNECTION_DATA.
     */
    public static final String ACTION_BLUETOOTH_DISCONNECT = "org.tec_hub.tecuniversalcomm.BLUETOOTH_DISCONNECT";

    /**
     * Sent to the BluetoothConnectionService by any context that wants to
     * send data over a BluetoothConnection.  The intent sent to the
     * BluetoothConnectionService must have the BluetoothConnection in
     * question added as a Parcelable Extra using the key
     * TECIntent.BLUETOOTH_CONNECTION_DATA.
     */
    public static final String ACTION_BLUETOOTH_SEND_DATA = "org.tec_hub.tecuniversalcomm.BLUETOOTH_SEND_DATA";

    /**
     * Used by the BluetoothConnectionService to broadcast updates to the
     * app that it has received data from a BluetoothConnection.  To indicate
     * which BluetoothConnection the data originated from, the broadcasted
     * intent contains a Parcelable Extra of the BluetoothConnection under
     * the key TECIntent.BLUETOOTH_CONNECTION_DATA.
     */
    public static final String ACTION_BLUETOOTH_UPDATE_INPUT = "org.tec_hub.tecuniversalcomm.BLUETOOTH_UPDATE_INPUT";

    /**
     * Used to indicate that a BluetoothConnection has been discovered and report it back to some
     * wanting Activity/Service.
     */
    public static final String ACTION_BLUETOOTH_DISCOVREED = "org.tec_hub.tecuniversalcomm.BLUETOOTH_DISCOVERED";

    /**
     * Sent to the TcpIpConnectionService by any context that wants
     * to connect a TcpIpConnection.  The intent sent to the
     * TcpIpConnectionService must have the TcpIpConnection in question
     * added as a Parcelable Extra using the key TECIntent.TCPIP_CONNECTION_DATA.
     */
    public static final String ACTION_TCPIP_CONNECT = "org.tec_hub.tecuniversalcomm.TCPIP_CONNECT";

    /**
     * Sent to the TcpIpConnectionService by any context that wants
     * to disconnect a TcpIpConnection.  The intent sent to the
     * TcpIpConnectionService must have the TcpIpConnection in question
     * added as a Parcelable Extra using the key TECIntent.TCPIP_CONNECTION_DATA.
     */
    public static final String ACTION_TCPIP_DISCONNECT = "org.tec_hub.tecuniversalcomm.TCPIP_DISCONNECT";

    /**
     * Sent to the TcpIpConnectionService by any context that wants to send data
     * over a TcpIpConnection.  The intent sent to the TcpIpConnectionService must
     * have the TcpIpConnection added as a Parcelable Extra with the key TECIntent.TCPIP_CONNECTION_DATA,
     * and that TcpIpConnection must already be connected.
     */
    public static final String ACTION_TCPIP_SEND_DATA = "org.tec_hub.tecuniversalcomm.TCPIP_SEND_DATA";

    /**
     * Broadcasted by the TcpIpConnectionService anytime there has been data received over a
     * TcpIpConnection.  The received data is packaged as a Parcelable Extra under the key
     * TECIntent.TCPIP_RECEIVED_DATA.
     */
    public static final String ACTION_TCPIP_UPDATE_INPUT = "org.tec_hub.tecuniveralcomm.TCPIP_UPDATE_INPUT";

    /**
     * Broadcasted by any Service or Activity that has discovered a new connection and is returning
     * it to a waiting Activity or Service.
     */
    public static final String ACTION_TCPIP_DISOVERED = "org.tec_hub.tecuniversalcomm.TCPIP_DISCOVREED";

    //Extras////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Used by any intent carrying a BluetoothConnection as a Parcelable Extra.
     */
    public static final String BLUETOOTH_CONNECTION_DATA = "bluetooth_connection_data";

    /**
     * Used to bundle the data that will be sent over a BluetoothConnection into an intent.
     * The intent using this as an Extra key should have the action "ACTION_BLUETOOTH_SEND_DATA".
     */
    public static final String BLUETOOTH_SEND_DATA = "bluetooth_send_data";

    /**
     * Used by the BluetoothConnectionService to indicate data has been received
     * by an active BluetoothConnection.
     */
    public static final String BLUETOOTH_RECEIVED_DATA = "bluetooth_input_data";

    /**
     * Used by any intent carrying a TcpIpConnection as a Parcelable Extra.
     */
    public static final String TCPIP_CONNECTION_DATA = "tcpip_connection_data";

    /**
     * Used to bundle the data that will be sent over a TcpIpConnection into an intent.
     * The intent using this as an Extra key should have the action "ACTION_TCPIP_SEND_DATA".
     */
    public static final String TCPIP_SEND_DATA = "tcpip_send_data";

    /**
     * Used as the Extra key for any intent carrying data that has been received from a
     * TcpIpConnection.
     */
    public static final String TCPIP_RECEIVED_DATA = "tcpip_input_data";

    /**
     * Used as an Extra key for any intent carrying a Device as a Parcelable.
     */
    public static final String DEVICE_DATA = "device_data";
}
