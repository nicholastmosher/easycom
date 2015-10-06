package org.tec_hub.tecuniversalcomm.data;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nick Mosher on 10/3/15.
 */
public class NewStorageAdapter {

    private static NewStorageAdapter STORAGE_ADAPTER;

    private Context mContext;
    private File mDataFolder;

    /**
     * Constructor for singleton style Storage Adapter.
     * @param context The context of the caller.
     */
    private NewStorageAdapter(Context context) {
        mContext = context;
        mDataFolder = context.getFilesDir();
    }

    /**
     * Singleton retriever for Storage Adapter.
     * @param context The context of the caller.
     * @return The singleton instance of the Storage Adapter.
     */
    public static NewStorageAdapter getInstance(Context context) {
        if(STORAGE_ADAPTER == null) {
            STORAGE_ADAPTER = new NewStorageAdapter(context);
        }
        return STORAGE_ADAPTER;
    }

    /**
     * Data Adapters have a pseudo-singleton model.  If a new type is
     * passed, a new Data Adapter is created, but if a previously-used
     * type is passed, the existing Data Adapter is returned.
     * @param type The type of Data Adapter.
     * @param <T> The type of Data Adapter.
     * @param adapter A custom TypeAdapter for Gson to use to encode/decode json.
     * @param name The name of the Data Adapter.
     * @return The Data Adapter.
     */
    public <T> DataAdapter<T> getDataAdapter(Class<T> type, String name, TypeAdapter<T> adapter) {
        return DataAdapter.getDataAdapter(type, name, adapter);
    }

    /**
     * Data Adapters have a pseudo-singleton model.  If a new type is
     * passed, a new Data Adapter is created, but if a previously-used
     * type is passed, the existing Data Adapter is returned.
     * @param type The type of Data Adapter.
     * @param <T> The type of Data Adapter.
     * @param name The name of the Data Adapter.
     * @return The Data Adapter.
     */
    public <T> DataAdapter<T> getDataAdapter(Class<T> type, String name) {
        return DataAdapter.getDataAdapter(type, name);
    }

    /**
     * Data Adapters have a pseudo-singleton model.  If a new type is
     * passed, a new Data Adapter is created, but if a previously-used
     * type is passed, the existing Data Adapter is returned.
     * @param type The type of Data Adapter.
     * @param <T> The type of Data Adapter.
     * @return The Data Adapter.
     */
    public <T> DataAdapter<T> getDataAdapter(Class<T> type) {
        return DataAdapter.getDataAdapter(type);
    }

    /**
     * Defines a listener that external entities can use to receive
     * updates about when data transfers are complete.
     * @param <T>
     */
    public static abstract class DataEventListener<T> {
        public void onDataRead(T data) { }
        public void onDataWrite(T data) { }
    }

    /**
     * Allows us to create objects that will read from and write to
     * files in persistent storage.  Each DataAdapter has a unique
     * @param <T>
     */
    public static class DataAdapter<T> extends DataSetObserver {

        private static Map<String, DataAdapter<?>> adapters = new HashMap<>();

        private Type mDataType;
        private File mDataFile;
        private Gson mGson;

        private DataAdapter(Class<T> type, String name, TypeAdapter<T> adapter) {
            mDataType = type;
            mDataFile = new File(STORAGE_ADAPTER.mDataFolder, name);
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(type, adapter);
            mGson = builder.create();
        }

        private DataAdapter(Class<T> type, String name) {
            mDataType = type;
            mDataFile = new File(STORAGE_ADAPTER.mDataFolder, name);
            mGson = new Gson();
        }

        private DataAdapter(Class<T> type) {
            this(type, type.toString());
        }

        public static <T> DataAdapter<T> getDataAdapter(Class<T> type, String name, TypeAdapter<T> adapter) {
            //If we already have a DataAdapter of this type, return it.
            if(adapters.containsKey(name)) {
                //The fact that the adapters are stored by their type makes this safe.
                return (DataAdapter<T>) adapters.get(name);

            //If we don't have a DataAdapter of this type, create one and add it and return it.
            } else {
                DataAdapter<T> dataAdapter = new DataAdapter<>(type, name, adapter);
                adapters.put(name, dataAdapter);
                return dataAdapter;
            }
        }

        public static <T> DataAdapter<T> getDataAdapter(Class<T> type, String name) {
            //If we already have a DataAdapter of this type, return it.
            if(adapters.containsKey(name)) {
                //The fact that the adapters are stored by their type makes this safe.
                return (DataAdapter<T>) adapters.get(name);

                //If we don't have a DataAdapter of this type, create one and add it and return it.
            } else {
                DataAdapter<T> dataAdapter = new DataAdapter<>(type, name);
                adapters.put(name, dataAdapter);
                return dataAdapter;
            }
        }

        public static <T> DataAdapter<T> getDataAdapter(Class<T> type) {
            return getDataAdapter(type, type.toString());
        }

        /**
         * Initiates an asynchronous task to write the given
         * data to storage.
         * @param data The data to write.
         */
        public void write(T data) {
            new WriteTask(data).execute();
        }

        /**
         * Initiate an asynchronous task to write the given data
         * to storage
         * @param data The data to write to file.
         * @param listener Listener that notifies when write is finished.
         */
        public void write(T data, DataEventListener<T> listener) {
            new WriteTask(data, listener).execute();
        }

        /**
         * Initiates an asynchronous task to read data from
         * this DataAdapter's file.  Callers provide a
         * DataEventListener which is used to deliver the
         * parsed data.
         * @param listener The listener to deliver the data to.
         */
        public void read(DataEventListener<T> listener) {
            new ReadTask(listener).execute();
        }

        /**
         * Erases this Data Adapter's data file.
         */
        public void wipeFile() {
            try {
                new PrintWriter(mDataFile).close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        public class WriteTask extends AsyncTask<Void, Void, T> {

            private T mData;
            private DataEventListener<T> mListener;

            public WriteTask(T data) {
                mData = data;
            }

            public WriteTask(T data, DataEventListener<T> listener) {
                this(data);
                mListener = listener;
            }

            /**
             * Actually performs data writing.
             * @param params Null in this case.
             * @return True if written successfully, false otherwise.
             */
            @Override
            protected T doInBackground(Void... params) {
                wipeFile();
                if(!mDataFile.exists()) {
                    try {
                        mDataFile.createNewFile();
                        System.out.println("Successfully created new DataFile " + mDataFile.toString());
                    } catch(IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                String json = mGson.toJson(mData);
                try {
                    FileOutputStream fos = new FileOutputStream(mDataFile, true);
                    fos.write(json.getBytes());
                    fos.close();
                    return mData;
                } catch(IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(T data) {
                super.onPostExecute(data);
                mListener.onDataWrite(data);
            }
        }

        public class ReadTask extends AsyncTask<Void, Void, T> {

            private DataEventListener<T> mListener;

            public ReadTask(DataEventListener<T> listener) {
                mListener = listener;
            }

            @Override
            protected T doInBackground(Void... params) {
                T data;
                try {
                    BufferedReader br = new BufferedReader(
                                        new InputStreamReader(
                                        new FileInputStream(mDataFile)));

                    StringBuilder builder = new StringBuilder();
                    String line;
                    while((line = br.readLine()) != null) {
                        builder.append(line);
                    }
                    String json = builder.toString();
                    System.out.println("Read json data: (" + json + ")");
                    if(json.equals("")) return null;

                    data = mGson.fromJson(json, mDataType);
                    System.out.println("Constructed json data: " + data.toString());
                    return data;

                } catch(IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            /**
             * When we've finished parsing the data, call the listener
             * to deliver the constructed object.
             * @param data The data read from storage.
             */
            @Override
            protected void onPostExecute(T data) {
                super.onPostExecute(data);
                mListener.onDataRead(data);
            }
        }
    }
}
