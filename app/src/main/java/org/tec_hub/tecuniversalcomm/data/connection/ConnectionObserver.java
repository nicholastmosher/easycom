package org.tec_hub.tecuniversalcomm.data.connection;

/**
 * Created by Nick Mosher on 9/25/15.
 */
public interface ConnectionObserver {

    void onUpdate(Connection connection, Connection.Status cue);
}
