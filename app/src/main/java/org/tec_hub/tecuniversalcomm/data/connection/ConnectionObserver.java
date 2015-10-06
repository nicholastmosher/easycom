package org.tec_hub.tecuniversalcomm.data.connection;

/**
 * Created by Nick Mosher on 9/25/15.
 */
public abstract class ConnectionObserver {

    void onUpdate(Connection.Status status) {
//        System.out.println("WARNING: onUpdate(Status) is not overridden!");
    }

    void onUpdate(Connection connection, Connection.Status status) {
        System.out.println("WARNING: onUpdate(Connection, Status) is not overridden!");
    }
}
