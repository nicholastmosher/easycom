package org.tec_hub.tecuniversalcomm.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Nick Mosher on 4/16/15.
 */
public class BluetoothConnection extends Connection implements Parcelable {

    public static final UUID BLUETOOTH_SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final Parcelable.Creator<BluetoothConnection> CREATOR = new Parcelable.Creator<BluetoothConnection>() {
        public BluetoothConnection createFromParcel(Parcel in) {
            return new BluetoothConnection(in);
        }

        public BluetoothConnection[] newArray(int size) {
            return new BluetoothConnection[size];
        }
    };

    private final String mBluetoothAddress;

    private transient BluetoothAdapter mBluetoothAdapter; //BluetoothAdapter is not Serializable
    private BluetoothSocket mBluetoothSocket;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    public BluetoothConnection(String name, String address) {
        super(Preconditions.checkNotNull(name));
        mBluetoothAddress = Preconditions.checkNotNull(address);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mConnected = false;
    }

    public BluetoothConnection(Parcel in) {
        super(Preconditions.checkNotNull(in));
        mBluetoothAddress = Preconditions.checkNotNull(in.readString());
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Android framework is lacking here (no readBoolean()), have to do array workaround.
        boolean[] connected = {false};
        in.readBooleanArray(connected);
        mConnected = connected[0];
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(mBluetoothAddress);
        out.writeBooleanArray(new boolean[]{mConnected});
    }

    public String getAddress() {
        return mBluetoothAddress;
    }

    public boolean isConnected() {
        return mConnected;
    }

    public void connect() {
        if(!mConnected) {
            new BluetoothConnectTask().execute();
        }
    }

    public void disconnect() {
        if(mConnected) {
            new BluetoothDisconnectTask().execute();
        }
    }

    /**
     * Retrieves the Output Stream if this Connection is connected and
     * the Output Stream is not null.
     * @return The OutputStream to the remote bluetooth device.
     */
    public OutputStream getOutputStream() {
        if(mConnected && mOutputStream != null) {
            return mOutputStream;
        }
        return null;
    }

    /**
     * Retrieves the Input Stream if this Connection is connected and
     * the Input Stream is not null.
     * @return The InputStream from the remote bluetooth device.
     */
    public InputStream getInputStream() {
        if(mConnected && mInputStream != null) {
            return mInputStream;
        }
        return null;
    }

    /**
     * Adds a new OnConnectStatusChangedListener to the map.  The Context
     * is used as the map key so that more than one activity may set
     * callbacks but no activity may have duplicate listeners.
     * @param context The context of the listener.
     * @param listener The OnConnectStatusChangedListener to associate with the context.
     */
    public void setOnConnectStatusChangedListener(Context context, OnConnectStatusChangedListener listener) {
        mOnConnectStatusChangedListeners.put(context, listener);
    }

    /**
     * Loops through all registered OnConnectStatusChangedListeners and notifies them of connection.
     */
    protected void notifyConnected() {
        Set<Context> listenerKeys = mOnConnectStatusChangedListeners.keySet();
        for(Context c : listenerKeys) {
            mOnConnectStatusChangedListeners.get(c).onConnect();
        }
    }

    /**
     * Loops through all registered OnConnectStatusChangedListeners and notifies them of disconnection.
     */
    protected void notifyDisconnected() {
        Set<Context> listenerKeys = mOnConnectStatusChangedListeners.keySet();
        for(Context c : listenerKeys) {
            mOnConnectStatusChangedListeners.get(c).onDisconnect();
        }
    }

    private class BluetoothConnectTask extends AsyncTask<Void, Void, Boolean> {

        protected Boolean doInBackground(Void... params) {
            //Check if BT is enabled
            if (!mBluetoothAdapter.isEnabled()) {
                System.out.println("Bluetooth not enabled!"); //TODO better handling.
            }

            //Define a BluetoothDevice with the address from our Connection.
            String address = getAddress();
            BluetoothDevice device;
            if(address != null  && BluetoothAdapter.checkBluetoothAddress(address)) {
                device = mBluetoothAdapter.getRemoteDevice(address);
            } else {
                return false;
            }

            //Try to retrieve a BluetoothSocket from the BluetoothDevice.
            try {
                mBluetoothSocket = device.createRfcommSocketToServiceRecord(BLUETOOTH_SERIAL_UUID);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            //Shouldn't need to be discovering at this point.
            mBluetoothAdapter.cancelDiscovery();

            //Attempt to connect to the bluetooth device and receive a BluetoothSocket
            try {
                mBluetoothSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mBluetoothSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                return false;
            }

            //Retrieve the output stream to the device.
            try {
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            //Retrieve the input stream from the device.
            try {
                mInputStream = mBluetoothSocket.getInputStream();
            } catch(IOException e) {
                e.printStackTrace();
                return false;
            }

            //If we've made it this far, must have been a success.
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            mConnected = true;
            if(mOnConnectStatusChangedListeners != null) {
                notifyConnected();
            }
        }
    }

    private class BluetoothDisconnectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            mConnected = false;
            boolean success = true;
            if(mConnected && mBluetoothSocket != null) {
                try {
                    mBluetoothSocket.close();
                } catch(IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }
            if(mOutputStream != null) {
                try {
                    mOutputStream.close();
                } catch(IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }
            if(mInputStream != null) {
                try {
                    mInputStream.close();
                } catch(IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if(success) {
                if(mOnConnectStatusChangedListeners != null) {
                    notifyDisconnected();
                }
            }
        }
    }
}
