package org.tec_hub.tecuniversalcomm.data.connection.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

/**
 * Created by Nick Mosher on 4/30/15.
 */
public class DataReceivedIntent extends Intent implements ConnectionIntent {

    public DataReceivedIntent(Context context, Class target, byte[] data) {
        super(context, target);
        Preconditions.checkNotNull(data);

        setAction(ACTION_RECEIVED_DATA);
        putExtra(RECEIVED_DATA, data);
    }
}
