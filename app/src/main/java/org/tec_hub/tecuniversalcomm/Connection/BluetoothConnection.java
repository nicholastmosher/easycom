package org.tec_hub.tecuniversalcomm.Connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private boolean mConnected;

    public BluetoothConnection(String name, String address) {
        super(name);
        mBluetoothAddress = address;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mConnected = false;
    }

    public BluetoothConnection(Parcel in) {
        super(in);
        mBluetoothAddress = in.readString();
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
        new BluetoothConnectTask().execute("");
    }

    public void disconnect() {
        if(mBluetoothSocket != null) {
            try {
                mBluetoothSocket.close();
                mConnected = false;
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        if(mOutputStream != null) {
            try {
                mOutputStream.close();
                mConnected = false;
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        if(mInputStream != null) {
            try {
                mInputStream.close();
                mConnected = false;
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public OutputStream getOutputStream() {
        if(mConnected && mOutputStream != null) {
            return mOutputStream;
        }
        return null;
    }

    public InputStream getInputStream() {
        if(mConnected && mInputStream != null) {
            return mInputStream;
        }
        return null;
    }

    private class BluetoothConnectTask extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... params) {
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
        }
    }
}
