package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.connection.Connection;
import org.tec_hub.tecuniversalcomm.intents.TECIntent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick Mosher on 3/18/2015.
 */
public class DeviceManagerActivity extends ActionBarActivity {

    //Stores reference to the last device used here to restore if user uses back navigation
    //private static Device lastDevice;

    private ListView mListView;
    private ConnectionListAdapter mConnectionAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manager);

        //Get the intent that launched this activity
        Intent launchIntent = getIntent();
        Preconditions.checkNotNull(launchIntent);

        //Parse the device sent to us on the launching intent
        Device activeDevice = launchIntent.getParcelableExtra(TECIntent.DEVICE_DATA);

        //Set the title of the activity to the device name
        getSupportActionBar().setTitle(activeDevice.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initialize the ListView and Adapter with the Connection data from the active device
        mListView = (ListView) findViewById(R.id.device_manager_list);
        mConnectionAdapter = new ConnectionListAdapter(activeDevice.getConnections());
        mListView.setAdapter(mConnectionAdapter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_options, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            //If the up button is pressed act like the back button.
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class ConnectionListAdapter extends BaseAdapter {

        private List<Connection> mConnections;

        public ConnectionListAdapter() {
            mConnections = new ArrayList<>();
        }

        public ConnectionListAdapter(List<Connection> connections) {
            mConnections = Preconditions.checkNotNull(connections);
        }

        @Override
        public int getCount() {
            return mConnections.size();
        }

        @Override
        public Object getItem(int position) {
            return mConnections.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout root;

            //Inflate or instantiate the root view
            if(convertView != null) {
                root = (RelativeLayout) convertView;
            } else {
                root = new RelativeLayout(DeviceManagerActivity.this);
                LayoutInflater inflater = (LayoutInflater) DeviceManagerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.inflate(R.layout.connection_list_item, root);
            }

            //Retrieve current Connection object
            final Connection connection = (Connection) getItem(position);
            Preconditions.checkNotNull(connection);

            //Load views from xml
            TextView nameView = (TextView) root.findViewById(R.id.connection_name);
            TextView detailsView = (TextView) root.findViewById(R.id.connection_details);
            ImageButton iconButton = (ImageButton) root.findViewById(R.id.connection_image_button);
            RelativeLayout listClickable = (RelativeLayout) root.findViewById(R.id.list_clickable);
            final ImageButton optionsButton = (ImageButton) root.findViewById(R.id.options_button);

            //Do actions common to all Connections
            nameView.setText(connection.getName());

            //Do things unique to BluetoothConnections
            if(connection instanceof BluetoothConnection) {
                final BluetoothConnection bluetoothConnection = (BluetoothConnection) connection;

                //Set details TextView to display bluetooth address
                detailsView.setText(bluetoothConnection.getAddress());

                //Set button icon based on sdk version
                if(Build.VERSION.SDK_INT >= 16) {
                    iconButton.setBackground(getResources().getDrawable(R.drawable.bt_icon_live));
                } else {
                    iconButton.setImageDrawable(getResources().getDrawable(R.drawable.bt_icon_live));
                }

                //Set action to do on icon button click
                iconButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                //Set action to do when list item is clicked
                listClickable.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                //Set up options menu, anchored to optionsButton
                final PopupMenu optionsMenu = new PopupMenu(DeviceManagerActivity.this, optionsButton);
                optionsMenu.inflate(R.menu.menu_connection_options);
                optionsMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()) {
                            case R.id.action_open_terminal:
                                Intent terminalIntent = new Intent(DeviceManagerActivity.this, TerminalActivity.class);
                                terminalIntent.putExtra(TECIntent.BLUETOOTH_CONNECTION_DATA, bluetoothConnection);
                                startActivity(terminalIntent);
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                //Set action to do on option button click
                optionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Open options menu
                        optionsMenu.show();
                    }
                });
            }
            return root;
        }
    }
}