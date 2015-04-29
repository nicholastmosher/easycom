package org.tec_hub.tecuniversalcomm;

/**
 * Created by Nick Mosher on 4/17/15.
 * Defines intent constants for transmitting data across this app.
 */
public interface TECIntent {

    //Actions
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
     * Used by the BluetoothConnectionService to broadcast updates to the
     * app that it has received data from a BluetoothConnection.  To indicate
     * which BluetoothConnection the data originated from, the broadcasted
     * intent contains a Parcelable Extra of the BluetoothConnection under
     * the key TECIntent.BLUETOOTH_CONNECTION_DATA.
     */
    public static final String ACTION_BLUETOOTH_UPDATE_INPUT = "org.tec_hub.tecuniversalcomm.BLUETOOTH_UPDATE_INPUT";

    //Extras
    /**
     * Used by any intent carrying a BluetoothConnection as a Parcelable Extra.
     */
    public static final String BLUETOOTH_CONNECTION_DATA = "bluetooth_connection_data";

    /**
     * Used by the BluetoothConnectionService to indicate data has been received
     * by an active BluetoothConnection.
     */
    public static final String BLUETOOTH_RECEIVED_DATA = "bluetooth_input_data";
    public static final String DEVICE_DATA = "device_data";
}
