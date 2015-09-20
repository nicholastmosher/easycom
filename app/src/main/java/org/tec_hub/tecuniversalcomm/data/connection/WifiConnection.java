package org.tec_hub.tecuniversalcomm.data.connection;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Preconditions;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Nick Mosher on 9/15/15.
 */
public class WifiConnection extends Connection implements Parcelable {

    /**
     * Required for Parcelable framework.
     */
    public static final Parcelable.Creator<WifiConnection> CREATOR = new Parcelable.Creator<WifiConnection>() {
        public WifiConnection createFromParcel(Parcel in) {
            return new WifiConnection(in);
        }

        public WifiConnection[] newArray(int size) {
            return new WifiConnection[size];
        }
    };

    public WifiConnection(String name) {
        super(name);
    }

    public WifiConnection(Parcel in) {
        super(Preconditions.checkNotNull(in));
        //Grab other member data from "in".
    }

    public void connect(Context context) {

    }

    public void disconnect(Context context) {

    }

    public boolean isConnected() {
        return false;
    }

    public OutputStream getOutputStream() {
        return null;
    }

    public InputStream getInputStream() {
        return null;
    }
}
