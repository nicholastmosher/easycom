package org.tec_hub.tecuniversalcomm.data;

import android.content.Context;
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
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Nick Mosher on 10/3/15.
 * Handles reading and writing data to files.  Individual data types are
 * managed by generic DataAdapters.  IO operations on DataAdapters are
 * asynchronous so as to be non-blocking, and operations that traditionally
 * block for their result (i.e. reading) use callbacks to deliver data
 * when it's ready.
 */
public class DataAdapter<T> extends Observable implements Observer {

    private static Map<String, DataAdapter<?>> adapters = new HashMap<>();

    private Type mDataType;
    private File mDataFile;
    private Gson mGson;

    private DataAdapter(Context context, Class<T> type, String name) {
        mDataType = type;
        mDataFile = new File(context.getFilesDir(), name);
        mGson = new Gson();
    }

    private DataAdapter(Context context, Class<T> type, String name, TypeAdapter<T> adapter) {
        this(context, type, name);
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(type, adapter);
        mGson = builder.create();
    }

    private DataAdapter(Context context, Class<T> type) {
        this(context, type, type.toString());
    }

    /**
     * Returns an instance of DataAdapter of the given type, name, and TypeAdapter,
     * creating such a DataAdapter if one has not been constructed yet.
     *
     * @param type    The data type for this adapter.
     * @param name    The name of this DataAdapter.  Using a name will cause
     *                this DataAdapter to write to a file of the same name.
     * @param adapter A TypeAdapter for use with Gson's json parsing, if it's needed
     *                for a particular data type.
     * @param <T>     The generic type for this adapter.
     * @return An instance of a DataAdapter fitting the arguments' specifications.
     */
    public static <T> DataAdapter<T> getDataAdapter(Context context, Class<T> type, String name, TypeAdapter<T> adapter) {
        //If we already have a DataAdapter of this type, return it.
        if(adapters.containsKey(name)) {
            //The fact that the adapters are stored by their type makes this safe.
            return (DataAdapter<T>) adapters.get(name);

            //If we don't have a DataAdapter of this type, create one and add it and return it.
        } else {
            DataAdapter<T> dataAdapter = new DataAdapter<>(context, type, name, adapter);
            adapters.put(name, dataAdapter);
            return dataAdapter;
        }
    }

    /**
     * Returns an instance of DataAdapter of the given type and name,
     * creating such a DataAdapter if one has not been constructed yet.
     *
     * @param type The data type for this adapter.
     * @param name The name of this DataAdapter.  Using a name will cause
     *             this DataAdapter to write to a file of the same name.
     * @param <T>  The generic type for this adapter.
     * @return An instance of a DataAdapter fitting the arguments' specifications.
     */
    public static <T> DataAdapter<T> getDataAdapter(Context context, Class<T> type, String name) {
        //If we already have a DataAdapter of this type, return it.
        if(adapters.containsKey(name)) {
            //The fact that the adapters are stored by their type makes this safe.
            return (DataAdapter<T>) adapters.get(name);

            //If we don't have a DataAdapter of this type, create one and add it and return it.
        } else {
            DataAdapter<T> dataAdapter = new DataAdapter<>(context, type, name);
            adapters.put(name, dataAdapter);
            return dataAdapter;
        }
    }

    /**
     * Returns an instance of DataAdapter of the given type,
     * creating such a DataAdapter if one has not been constructed yet.
     *
     * @param type The data type for this adapter.
     * @param <T>  The generic type for this adapter.
     * @return An instance of a DataAdapter fitting the arguments' specifications.
     */
    public static <T> DataAdapter<T> getDataAdapter(Context context, Class<T> type) {
        return getDataAdapter(context, type, type.toString());
    }

    /**
     * Initiates an asynchronous task to write the given
     * data to storage.
     *
     * @param data The data to write.
     */
    public void write(T data) {
        new WriteTask(data).execute();
    }

    /**
     * Initiate an asynchronous task to write the given
     * data to storage.  Accepts an observer to notify
     * when the write operation is complete.
     *
     * @param data     The data to write to file.
     * @param observer To notify when write is finished.
     */
    public void write(T data, Observer observer) {
        new WriteTask(data, observer).execute();
    }

    /**
     * Initiates an asynchronous task to read data from
     * this DataAdapter's file.  Callers provide a
     * DataObserver which is used to deliver the
     * parsed data.
     *
     * @param observer The observer to deliver the data to.
     */
    public void read(Observer observer) {
        new ReadTask(observer).execute();
    }

    /**
     * Allows us to register certain pieces of data that extend Observable.
     * In this scenario, the DataAdapter subscribes to the data as an
     * Observer, so that whenever the data object is updated, the DataAdapter
     * is notified and can update the changes in the file.
     *
     * @param data The observable data to subscribe to.
     */
    public void registerObservableData(T data) {
        if(data instanceof Observable) {
            Observable observable = (Observable) data;
            observable.addObserver(this);
        } else {
            new IllegalArgumentException("Data is not observable!").printStackTrace();
        }
    }

    /**
     * Acts as a flag for our Observer update method for determining whether
     * a particular update is an event that is relevant to us.
     */
    public enum Event {
    }

    /**
     * Called by data we've subscribed to when there are important changes
     * that need to be rewritten to file.
     *
     * @param observable The data we've subscribed to.
     * @param event      Will be an instance of DataAdapter.Event if it's a
     *                   notification that is relevant to us.
     */
    @Override
    public void update(Observable observable, Object event) {
        if(!(event instanceof Event)) {
            //If this is not a DataAdapter.Event, it's not something we're concerned about.
            return;
        }

        //Unchecked cast because the only observables we subscribe to are confirmed T's
        T updatedData = (T) observable;

        new WriteTask(updatedData).execute();
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

    /**
     * Asynchronous task that takes care of writing data to a file.
     */
    public class WriteTask extends AsyncTask<Void, Void, T> {

        private T mData;
        private Observer mObserver;

        public WriteTask(T data) {
            mData = data;
        }

        public WriteTask(T data, Observer listener) {
            this(data);
            mObserver = listener;
        }

        /**
         * Actually performs data writing.
         *
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
            if(mObserver != null) {
                mObserver.update(DataAdapter.this, data);
            }
        }
    }

    /**
     * Asynchronous task that reads from a file and delivers with a callback.
     */
    public class ReadTask extends AsyncTask<Void, Void, T> {

        private Observer mObserver;

        public ReadTask(Observer observer) {
            mObserver = observer;
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
                if(json.equals("")) {
                    System.out.println("RETURNING NULL");
                    return null;
                }

                System.out.println("About to construct data: ");
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
         *
         * @param data The data read from storage.
         */
        @Override
        protected void onPostExecute(T data) {
            super.onPostExecute(data);
            mObserver.update(DataAdapter.this, data);
        }
    }
}