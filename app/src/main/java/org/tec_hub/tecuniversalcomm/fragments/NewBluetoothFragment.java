package org.tec_hub.tecuniversalcomm.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.tec_hub.tecuniversalcomm.R;
import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick Mosher on 11/17/15.
 * @author Nick Mosher, nicholastmosher@gmail.com, github.com/nicholastmosher
 */
public class NewBluetoothFragment extends Fragment {

    private BroadcastReceiver mBluetoothReceiver;

    /**
     * Generate a view that will configure a new Bluetooth Connection.
     * @param inflater A view inflater for using xml layouts.
     * @param parent A reference to the new view's parent.
     * @param savedInstanceState A bundle to save the instance data.
     * @return A view to configure a new Bluetooth Connection.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        //Inflate the fragment's root view using the inflater.
        LinearLayout bluetoothView = (LinearLayout) inflater.inflate(R.layout.fragment_new_bluetooth, parent, false);

        //Receive a reference to this view's recycler view from the root view.
        RecyclerView recyclerView = (RecyclerView) bluetoothView.findViewById(R.id.connection_new_bluetooth_recycler);

        //Configure the LayoutManager for the recycler to use.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        //Configure Recycler options.
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        //Initialize the adapter used to populate the recycler.
        final RecyclerAdapter recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);

        //Register a BroadcastReceiver with the system to notify when bluetooth devices are discovered.
        getContext().registerReceiver(mBluetoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //Create a Bluetooth Connection from the newly discovered device.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = (device.getName() == null ? "Unknown" : device.getName());
                BluetoothConnection connection = new BluetoothConnection(name, device.getAddress(), true);
                recyclerAdapter.addConnection(connection);

            }
        }, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        return bluetoothView;
    }

    /**
     * Start scanning for bluetooth devices when this Fragment gets started.
     */
    @Override
    public void onStart() {
        super.onStart();
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    /**
     * Stop scanning for bluetooth devices when this Fragment gets stopped.
     */
    @Override
    public void onStop() {
        super.onStop();
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        getContext().unregisterReceiver(mBluetoothReceiver);
    }

    /**
     * Defines how to populate the Recycler on this Fragment.  Namely,
     * with entries for Bluetooth Connections as they become discovered.
     */
    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        /**
         * Defines a collection of views in an element of the RecyclerAdapter.
         */
        public class ViewHolder extends RecyclerView.ViewHolder {

            public View mContainer;
            public ImageView mIcon;
            public TextView mTitle;
            public TextView mAddress;

            public ViewHolder(View view) {
                super(view);

                mContainer = view;
                mIcon = (ImageView) view.findViewById(R.id.element_new_bluetooth_icon);
                mTitle = (TextView) view.findViewById(R.id.element_new_bluetooth_title);
                mAddress = (TextView) view.findViewById(R.id.element_new_bluetooth_address);

                mContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContainer.setSelected(true);
                        System.out.println("BLUETOOTH CLICKED!" + mTitle.getText().toString());
                    }
                });
            }
        }

        private List<BluetoothConnection> mDiscoveredConnections = new ArrayList<>();

        public RecyclerAdapter() {

        }

        /**
         * Used to add a newly discovered Bluetooth Connection to this recycler.
         * @param connection The new Bluetooth Connection to add.
         */
        public void addConnection(BluetoothConnection connection) {
            mDiscoveredConnections.add(connection);
            notifyDataSetChanged();
        }

        /**
         * Returns the number of items in this adapter.
         * @return The number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return mDiscoveredConnections.size();
        }

        /**
         * Defines how the Adapter should construct the ViewHolders as needed.
         * @param parent The View that this ViewHolder represents.
         * @param viewType Specifies if different types of ViewHolders should be generated.
         * @return A ViewHolder to fit this Recycler.
         */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Inflate the root view in order to create a new ViewHolder.
            View bluetoothView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.element_new_bluetooth, parent, false);
            return new ViewHolder(bluetoothView);
        }

        /**
         * Populates a ViewHolder at a given position.
         * @param holder The ViewHolder to populate.
         * @param position The position of data with which to populate the ViewHolder.
         */
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            //Retrieve the Connection at this address.
            BluetoothConnection connection = mDiscoveredConnections.get(position);

            //Configure bluetooth icon.
            Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_bluetooth_black_48dp).getConstantState().newDrawable();
            icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.bluetoothBlue), PorterDuff.Mode.SRC_ATOP);

            //Apply data to views.
            holder.mIcon.setImageDrawable(icon);
            holder.mTitle.setText(connection.getName());
            holder.mAddress.setText(connection.getAddress());
        }
    }
}
