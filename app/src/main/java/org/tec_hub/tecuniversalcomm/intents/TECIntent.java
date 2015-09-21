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
     * Sent to the WifiConnectionService by any context that wants
     * to connect a WifiConnection.  The intent sent to the
     * WifiConnectionService must have the WifiConnection in question
     * added as a Parcelable Extra using the key TECIntent.WIFI_CONNECTION_DATA.
     */
    public static final String ACTION_WIFI_CONNECT = "org.tec_hub.tecuniversalcomm.WIFI_CONNECT";

    /**
     * Sent to the WifiConnectionService by any context that wants
     * to disconnect a WifiConnection.  The intent sent to the
     * WifiConnectionService must have the WifiConnection in question
     * added as a Parcelable Extra using the key TECIntent.WIFI_CONNECTION_DATA.
     */
    public static final String ACTION_WIFI_DISCONNECT = "org.tec_hub.tecuniversalcomm.WIFI_DISCONNECT";

    /**
     * Sent to the WifiConnectionService by any context that wants to send data
     * over a WifiConnection.  The intent sent to the WifiConnectionService must
     * have the WifiConnection added as a Parcelable Extra with the key TECIntent.WIFI_CONNECTION_DATA,
     * and that WifiConnection must already be connected.
     */
    public static final String ACTION_WIFI_SEND_DATA = "org.tec_hub.tecuniversalcomm.WIFI_SEND_DATA";

    /**
     * Broadcasted by the WifiConnectionService anytime there has been data received over a
     * WifiConnection.  The received data is packaged as a Parcelable Extra under the key
     * TECIntent.WIFI_RECEIVED_DATA.
     */
    public static final String ACTION_WIFI_UPDATE_INPUT = "org.tec_hub.tecuniveralcomm.WIFI_UPDATE_INPUT";

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
     * Used by any intent carrying a WifiConnection as a Parcelable Extra.
     */
    public static final String WIFI_CONNECTION_DATA = "wifi_connection_data";

    /**
     * Used to bundle the data that will be sent over a WifiConnection into an intent.
     * The intent using this as an Extra key should have the action "ACTION_WIFI_SEND_DATA".
     */
    public static final String WIFI_SEND_DATA = "wifi_send_data";

    /**
     * Used as the Extra key for any intent carrying data that has been received from a
     * WifiConnection.
     */
    public static final String WIFI_RECEIVED_DATA = "wifi_input_data";

    /**
     * Used as an Extra key for any intent carrying a Device as a Parcelable.
     */
    public static final String DEVICE_DATA = "device_data";
}
