package com.nicholastmosher.easycom.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nicholastmosher.easycom.data.connection.BluetoothConnection;
import com.nicholastmosher.easycom.data.connection.Connection;
import com.nicholastmosher.easycom.data.connection.TcpIpConnection;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nick Mosher on 12/9/15.
 * The Model manages all application state data.  This includes storing
 * persistent data to a local database, coordinating data changes and responding
 * to requests, etc.
 */
public class Model {

    /**
     * The application model is a singleton to prevent weird fragmentation.
     */
    private static Model MODEL;

    /**
     * The Context of this Model, which can be used to perform Android tasks
     * that require a reference to a context.
     */
    private Context mContext;

    /**
     * Returns the singleton instance of the Model.
     * @return The singleton instance of the Model.
     */
    private static Model getInstance(Context context) {
        if(MODEL == null) {
            MODEL = new Model(context);
        }
        return MODEL;
    }

    /**
     * Initializes the instance of the Model.
     */
    private Model(Context context) {
        mContext = context;
        mConfiguredConnections = new HashMap<>();
        mConnectionDatabaseHelper = new ConnectionDatabaseHelper(mContext);
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Contains a record of all Connections that have been successfully
     * configured, stored by the UUID of the Connections themselves.
     */
    private Map<Long, Connection> mConfiguredConnections;

    /**
     * The Connection Database Helper performs SQL operations to edit Connection
     * data in persistent storage.
     */
    private class ConnectionDatabaseHelper extends SQLiteOpenHelper {

        /**
         * Indicates the version of the database.  Increment if schemas change.
         */
        public static final int DATABASE_VERSION = 1;

        /**
         * The name of the Database in which to hold the Connections.
         */
        public static final String DATABASE_NAME = "Connections.db";

        /**
         * Instructions for creating this database.
         */
        private static final String CREATE_DATABASE =
                BluetoothConnection.BluetoothConnectionEntry.CREATE_TABLE_INSTRUCTION +
                TcpIpConnection.TcpIpConnectionEntry.CREATE_TABLE_INSTRUCTION;

        /**
         * Creates a Database Helper for managing the Connection data.
         * @param context The context in which to execute database commands.
         */
        public ConnectionDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DATABASE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        /**
         * Inserts a given Connection into it's respective table.
         * @param connection The Connection to insert.
         */
        public Long insertConnection(Connection connection) {
            SQLiteDatabase db = getWritableDatabase();

            //Write to different tables depending on connection type.
            if(connection instanceof BluetoothConnection) {
                return db.insert(BluetoothConnection.BluetoothConnectionEntry.TABLE_NAME,
                        null,
                        connection.getContentValues());
            } else if(connection instanceof TcpIpConnection) {
                return db.insert(TcpIpConnection.TcpIpConnectionEntry.TABLE_NAME,
                        null,
                        connection.getContentValues());
            } else {
                return null;
            }
        }
    }

    /**
     * Instance of the Connection Database Helper.
     */
    private ConnectionDatabaseHelper mConnectionDatabaseHelper;

    /**
     * Inserts a Connection into the Connection database.
     * @param connection The Connection to insert into the database.
     */
    public void insertConnection(Connection connection) {
        mConnectionDatabaseHelper.insertConnection(connection);
    }
}
