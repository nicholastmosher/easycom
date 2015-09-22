package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnectionService;
import org.tec_hub.tecuniversalcomm.data.connection.Connection;
import org.tec_hub.tecuniversalcomm.data.Device;
import org.tec_hub.tecuniversalcomm.data.connection.WifiConnection;
import org.tec_hub.tecuniversalcomm.intents.BluetoothConnectIntent;
import org.tec_hub.tecuniversalcomm.intents.BluetoothDisconnectIntent;
import org.tec_hub.tecuniversalcomm.intents.TECIntent;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Nick Mosher on 3/18/2015.
 */
public class DeviceActivity extends AppCompatActivity {

    private ListView mListView;
    private ConnectionListAdapter mConnectionAdapter;
    private Device mDevice;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manager);

        //Get the intent that launched this activity
        Intent launchIntent = getIntent();
        Preconditions.checkNotNull(launchIntent);

        //Parse the device sent to us on the launching intent
        mDevice = launchIntent.getParcelableExtra(TECIntent.DEVICE_DATA);

        //Set the title of the activity to the device name
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mDevice.getName() + " | Connections");
        setSupportActionBar(toolbar);

        //Initialize the ListView and Adapter with the Connection data from the active device
        mListView = (ListView) findViewById(R.id.device_manager_list);
        mConnectionAdapter = new ConnectionListAdapter(mDevice.getConnections());
        mListView.setAdapter(mConnectionAdapter);

        BluetoothConnectionService.launch(this);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_options, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.action_rename_device:
                Device.promptRename(mDevice, DeviceActivity.this);
                return true;

            case R.id.action_delete_device:

                return true;

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
                root = new RelativeLayout(DeviceActivity.this);
                LayoutInflater inflater = (LayoutInflater) DeviceActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.inflate(R.layout.list_item_connection, root);
            }

            //Retrieve current Connection object
            final Connection connection = (Connection) getItem(position);
            Preconditions.checkNotNull(connection);

            //Load views from xml
            TextView nameView = (TextView) root.findViewById(R.id.connection_name);
            TextView detailsView = (TextView) root.findViewById(R.id.connection_details);
            final ImageButton iconButton = (ImageButton) root.findViewById(R.id.connection_image_button);
            RelativeLayout listClickable = (RelativeLayout) root.findViewById(R.id.list_clickable);
            final ProgressBar progressIndicator = (ProgressBar) root.findViewById(R.id.progress_indicator);
            final ImageButton optionsButton = (ImageButton) root.findViewById(R.id.options_button);

            //Do actions common to all Connections
            nameView.setText(connection.getName());

            //Do things unique to BluetoothConnections
            if(connection instanceof BluetoothConnection) {
                final BluetoothConnection bluetoothConnection = (BluetoothConnection) connection;

                //Set details TextView to display bluetooth address
                detailsView.setText(bluetoothConnection.getAddress());

                //Set button icon based on sdk version
                int bluetoothIconId = bluetoothConnection.isConnected() ?
                        R.drawable.ic_bluetooth_connected_black_36dp :
                        R.drawable.ic_bluetooth_disabled_black_36dp;
                setImageButtonDrawable(iconButton, bluetoothIconId);
                iconButton.setColorFilter(ContextCompat.getColor(DeviceActivity.this, R.color.colorAccent));

                //Set callback for connection status changed to change icon
                bluetoothConnection.addObserver(new Observer() {
                    @Override
                    public void update(Observable observable, Object data) {
                        if (data instanceof Connection.ObserverCues) {
                            Connection.ObserverCues cue = (Connection.ObserverCues) data;
                            switch (cue) {
                                case Connected:
                                    setImageButtonDrawable(iconButton, R.drawable.ic_bluetooth_connected_black_36dp);
                                    progressIndicator.setVisibility(View.GONE);
                                    break;
                                case Disconnected:
                                    setImageButtonDrawable(iconButton, R.drawable.ic_bluetooth_disabled_black_36dp);
                                    progressIndicator.setVisibility(View.GONE);
                                    break;
                                case ConnectFailed:
                                    setImageButtonDrawable(iconButton, R.drawable.ic_bluetooth_disabled_black_36dp);
                                    progressIndicator.setVisibility(View.GONE);
                                    break;
                                default:
                            }
                        }
                    }
                });

                //Set action to do on icon button click
                iconButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(bluetoothConnection.isConnected()) {
                            //Send disconnect intent
                            progressIndicator.setVisibility(View.VISIBLE);
                            LocalBroadcastManager.getInstance(DeviceActivity.this).sendBroadcast(new BluetoothDisconnectIntent(DeviceActivity.this, bluetoothConnection));
                        } else {
                            //Send connect intent
                            progressIndicator.setVisibility(View.VISIBLE);
                            LocalBroadcastManager.getInstance(DeviceActivity.this).sendBroadcast(new BluetoothConnectIntent(DeviceActivity.this, bluetoothConnection));
                        }
                    }
                });

                //Set action to do when list item is clicked
                listClickable.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent terminalIntent = new Intent(DeviceActivity.this, TerminalActivity.class);
                        terminalIntent.putExtra(TECIntent.BLUETOOTH_CONNECTION_DATA, bluetoothConnection);
                        startActivity(terminalIntent);
                    }
                });

                //Set up options menu anchored to optionsButton
                final PopupMenu optionsMenu = new PopupMenu(DeviceActivity.this, optionsButton);
                optionsMenu.inflate(R.menu.menu_connection_options);
                optionsMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_open_terminal:
                                Intent terminalIntent = new Intent(DeviceActivity.this, TerminalActivity.class);
                                terminalIntent.putExtra(TECIntent.BLUETOOTH_CONNECTION_DATA, bluetoothConnection);
                                startActivity(terminalIntent);
                                return true;
                            case R.id.action_open_kudos:
                                Intent kudosIntent = new Intent(DeviceActivity.this, DriveKudosActivity.class);
                                kudosIntent.putExtra(TECIntent.BLUETOOTH_CONNECTION_DATA, bluetoothConnection);
                                startActivity(kudosIntent);
                                return true;
                            case R.id.action_open_controller:
                                Intent controlIntent = new Intent(DeviceActivity.this, ControllerActivity.class);
                                controlIntent.putExtra(TECIntent.BLUETOOTH_CONNECTION_DATA, bluetoothConnection);
                                startActivity(controlIntent);
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

            //If this connection is Wifi.
            } else if(connection instanceof WifiConnection) {
                final WifiConnection wifiConnection = (WifiConnection) connection;

                int wifiIconId = wifiConnection.isConnected() ?
                        R.drawable.ic_signal_wifi_4_bar_black_36dp :
                        R.drawable.ic_signal_wifi_off_black_36dp;
                setImageButtonDrawable(iconButton, wifiIconId);

                wifiConnection.addObserver(new Observer() {
                    @Override
                    public void update(Observable observable, Object data) {
                        if (data instanceof Connection.ObserverCues) {
                            Connection.ObserverCues cue = (Connection.ObserverCues) data;
                            switch (cue) {
                                case Connected:

                                    break;
                                case Disconnected:

                                    break;
                                case ConnectFailed:

                                    break;
                                default:
                            }
                        }
                    }
                });

                //Define what to do when the icon is pressed.
                iconButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (wifiConnection.isConnected()) {

                        } else {

                        }
                    }
                });

                //Define what to do when the list item is clicked.
                listClickable.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                //Define the options menu and what to do on different menu clicks.
                final PopupMenu optionsMenu = new PopupMenu(DeviceActivity.this, optionsButton);
                optionsMenu.inflate(R.menu.menu_connection_options);
                optionsMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_open_terminal:

                                return true;
                            case R.id.action_open_kudos:

                                return true;
                            case R.id.action_open_controller:

                                return true;
                            default:
                                return false;
                        }
                    }
                });

                //Show the options menu when the options button is clicked.
                optionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        optionsMenu.show();
                    }
                });
            }

            //Insert unique inflation of other Connection types in the future

            return root;
        }

        private void setImageButtonDrawable(final ImageButton button, int resourceId) {
            if(Build.VERSION.SDK_INT >= 16) {
                button.setBackground(getResources().getDrawable(resourceId));
            } else {
                button.setImageDrawable(getResources().getDrawable(resourceId));
            }
            button.setColorFilter(0xFFFF0000);
        }
    }
}