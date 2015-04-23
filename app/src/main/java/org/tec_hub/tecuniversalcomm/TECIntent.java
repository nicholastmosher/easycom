package org.tec_hub.tecuniversalcomm;

/**
 * Created by Nick Mosher on 4/17/15.
 * Defines intent constants for transmitting data across this app.
 */
public interface TECIntent {

    //Actions
    public static final String ACTION_BLUETOOTH_CONNECT = "org.tec_hub.tecuniversalcomm.BLUETOOTH_CONNECT";
    public static final String ACTION_BLUETOOTH_DISCONNECT = "org.tec_hub.tecuniversalcomm.BLUETOOTH_DISCONNECT";
    public static final String ACTION_BLUETOOTH_UPDATE_INPUT = "org.tec_hub.tecuniversalcomm.BLUETOOTH_UPDATE_INPUT";
    public static final String ACTION_BLUETOOTH_NOTIFY_CONNECTED = "org.tec_hub.tecuniversalcomm.BLUETOOTH_NOTIFY_CONNECTED";
    public static final String ACTION_BLUETOOTH_NOTIFY_DISCONNECTED = "org.tec_hub.tecuniversalcomm.BLUETOOTH_NOTIFY_DISCONNECTED";
    public static final String ACTION_BLUETOOTH_REQUEST_CONNECTION_STATE = "org.tec_hub.tecuniversalcomm.BLUETOOTH_REQUEST_CONNECTION_STATE";

    //Extras
    public static final String BLUETOOTH_CONNECTION_DATA = "bluetooth_connection_data";
    public static final String BLUETOOTH_INPUT_DATA = "bluetooth_input_data";
}
