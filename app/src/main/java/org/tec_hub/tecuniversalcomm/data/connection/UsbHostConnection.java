package org.tec_hub.tecuniversalcomm.data.connection;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import org.tec_hub.tecuniversalcomm.data.connection.intents.ConnectionIntent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Nick Mosher on 10/15/15.
 */
public class UsbHostConnection extends Connection {

    private UsbDevice mUsbDevice;

    public UsbHostConnection(UsbDevice device) {
        super(device.getDeviceName());
        mUsbDevice = device;
    }

    @Override
    public void connect(Context context) {

    }

    @Override
    public void disconnect(Context context) {

    }

    @Override
    public void sendData(Context context, byte[] data) {

    }

    @Override
    public Status getStatus() {
        return null;
    }

    @Override
    public String getConnectionType() {
        return ConnectionIntent.CONNECTION_TYPE_USB;
    }

    @Override
    public InputStream getInputStream() throws IllegalStateException {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
    }

    @Override
    public OutputStream getOutputStream() throws IllegalStateException {
        return new OutputStream() {
            @Override
            public void write(int oneByte) throws IOException {

            }
        };
    }

    public UsbDevice getUsbDevice() {
        return mUsbDevice;
    }
}
