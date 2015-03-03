package org.tec_hub.tecuniversalcomm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;
import java.util.ArrayList;

import at.abraxas.amarino.BTDevice;

/**
 * Created by Nick Mosher on 3/2/2015.
 */
public class TECDatabaseAdapter
{
    /*
     * Constants to hold unique database table names.
     */
    public static final String DEVICE_TABLE_NAME = "bt_devices_tbl";

    /*
     * Constants to hold unique database field names.
     */
    public static final String KEY_DEVICE_BT_ID = "_id";
    public static final String KEY_DEVICE_NAME = "name";
    public static final String KEY_DEVICE_BT_ADDRESS = "device_address";

    /*
     * Hold core database information.
     */
    public static final String DATABASE_NAME = "TEC_Database";
    public static final int DATABASE_VERSION = 2;

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;
    private final Context context;

    public TECDatabaseAdapter(Context context)
    {
        this.context = context;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL("CREATE TABLE " + DEVICE_TABLE_NAME + " ("
                    + KEY_DEVICE_BT_ID + " INTEGER PRIMARY KEY,"
                    + KEY_DEVICE_BT_ADDRESS + " TEXT UNIQUE,"
                    + KEY_DEVICE_NAME + " TEXT"
                    + ");");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS " + DEVICE_TABLE_NAME);
            onCreate(db);
        }
    }

    /**
     * Initializes the SQL database and prepares for read/write.
     * @throws SQLException
     */
    public void open() throws SQLException
    {
        databaseHelper = new DatabaseHelper(context);
        database = databaseHelper.getWritableDatabase();
    }

    /**
     * Closes the SQL database.
     */
    public void close()
    {
        databaseHelper.close();
    }

    /**
     * Creates a new profile of information for a device's bluetooth interface.
     * @param device The BTDevice to add to the database.
     * @return
     */
    public long createBTDevice(BTDevice device)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DEVICE_BT_ADDRESS, device.getAddress());
        initialValues.put(KEY_DEVICE_NAME, (device.getName() == null ? "NONAME" : device.getName()));

        return database.insert(DEVICE_TABLE_NAME, null, initialValues);
    }

    /**
     * Deletes the BTDevice profile at the specified deviceId.
     * @param deviceId The ID of the BTDevice to delete from the database.
     * @return
     */
    public boolean deleteDevice(long deviceId)
    {
        return database.delete(DEVICE_TABLE_NAME, KEY_DEVICE_BT_ID + "=" + deviceId, null) > 0;
    }

    /**
     * Returns a BTDevice representation of the device's bluetooth interface.
     * @param address The address of the BTDevice to retrieve from the database.
     * @return The BTDevice representation of the device's bluetooth interface.
     */
    public BTDevice getBTDevice(String address)
    {
        BTDevice device = null;
        Cursor c = database.query(DEVICE_TABLE_NAME, null, KEY_DEVICE_BT_ADDRESS + " like ?", new String[]{address}, null, null, null);

        if(c == null)
        {
            return null;
        }
        if(c.moveToFirst())
        {
            String name = c.getString(c.getColumnIndex(KEY_DEVICE_NAME));
            long id = c.getLong(c.getColumnIndex(KEY_DEVICE_BT_ID));
            device = new BTDevice(id, address, name);
        }
        c.close();
        return device;
    }

    /**
     * Returns an ArrayList of all BTDevice profiles stored in the database.
     * @return An ArrayList of all BTDevice profiles stored in the database.
     */
    public ArrayList<BTDevice> fetchAllBTDevices()
    {
        ArrayList<BTDevice> devices = new ArrayList<BTDevice>();
        Cursor c = database.query(DEVICE_TABLE_NAME, null, null, null, null, null, null);

        if(c == null)
        {
            return null;
        }
        if(c.moveToFirst())
        {
            do
            {
                String address = c.getString(c.getColumnIndex(KEY_DEVICE_BT_ADDRESS));
                String name = c.getString(c.getColumnIndex(KEY_DEVICE_NAME));
                long id = c.getLong(c.getColumnIndex(KEY_DEVICE_BT_ID));
                devices.add(new BTDevice(id, address, name));
            }
            while(c.moveToNext());
        }
        c.close();
        return devices;
    }
}
