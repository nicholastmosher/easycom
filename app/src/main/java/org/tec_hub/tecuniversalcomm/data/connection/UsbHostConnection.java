package org.tec_hub.tecuniversalcomm.data.connection;

import android.content.Context;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Nick Mosher on 10/15/15.
 */
public class UsbHostConnection extends Connection {

    static {

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
        return null;
    }

    @Override
    public InputStream getInputStream() throws IllegalStateException {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IllegalStateException {
        return null;
    }
}
