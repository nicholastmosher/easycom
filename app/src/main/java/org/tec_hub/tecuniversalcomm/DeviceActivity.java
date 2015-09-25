package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import org.tec_hub.tecuniversalcomm.data.StorageAdapter;
import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnectionService;
import org.tec_hub.tecuniversalcomm.data.connection.Connection;
import org.tec_hub.tecuniversalcomm.data.Device;
import org.tec_hub.tecuniversalcomm.data.connection.ConnectionList;
import org.tec_hub.tecuniversalcomm.data.connection.TcpIpConnection;
import org.tec_hub.tecuniversalcomm.intents.BluetoothConnectIntent;
import org.tec_hub.tecuniversalcomm.intents.BluetoothDisconnectIntent;
import org.tec_hub.tecuniversalcomm.intents.TECIntent;
import org.tec_hub.tecuniversalcomm.intents.TcpIpConnectIntent;
import org.tec_hub.tecuniversalcomm.intents.TcpIpDisconnectIntent;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Nick Mosher on 3/18/2015.
 */
public class DeviceActivity extends AppCompatActivity {

    public static final int REQUEST_DISCOVERY = 2;

    private static Device lastDevice;

    private ListView mListView;
    private ConnectionListAdapter mConnectionAdapter;
    private Device mDevice;
    private boolean mActionsVisible;

    FloatingActionButton addButton;
    FloatingActionButton addBtButton;
    FloatingActionButton addTcpButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manager);

        //Get the intent that launched this activity
        Intent launchIntent = getIntent();
        Preconditions.checkNotNull(launchIntent);

        Device device;
        if((device = launchIntent.getParcelableExtra(TECIntent.DEVICE_DATA)) != null) {
            //Parse the device sent to us on the launching intent
            mDevice = device;
            lastDevice = device;
        } else {
            mDevice = lastDevice;
        }

        //Set the title of the activity to the device name
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mDevice.getName() + " | Connections");
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);

        addButton = (FloatingActionButton) findViewById(R.id.action_add);
        addBtButton = (FloatingActionButton) findViewById(R.id.action_add_bluetooth);
        addTcpButton = (FloatingActionButton) findViewById(R.id.action_add_tcp);

        addButton.setImageResource(R.drawable.ic_add_white_48dp);
        addBtButton.setImageResource(R.drawable.ic_bluetooth_white_48dp);
        addTcpButton.setImageResource(R.drawable.ic_signal_wifi_4_bar_white_48dp);

        addBtButton.setVisibility(View.GONE);
        addTcpButton.setVisibility(View.GONE);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionsVisible = !mActionsVisible;
                updateActionButtonVisibility();
            }
        });

        addBtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(DeviceActivity.this, DiscoveryActivity.class), REQUEST_DISCOVERY);
            }
        });

        //Initialize the ListView and Adapter with the Connection data from the active device
        mListView = (ListView) findViewById(R.id.device_manager_list);
        mConnectionAdapter = new ConnectionListAdapter(mDevice.getConnections());
        mListView.setAdapter(mConnectionAdapter);

        BluetoothConnectionService.launch(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mActionsVisible = false;
        updateActionButtonVisibility();
    }

    /**
     * System callback that executes when some activity called with startActivityForResult() returns
     * with a response.  The requestCode is the code we gave when launching the activity for result,
     * the resultCode is a code given by the launched activity that indicates the status of the
     * result given.
     * @param requestCode The code given during startActivityForResult().
     * @param resultCode  A code indicating the status of the return (e.g. RESULT_OK).
     * @param data        An intent with data relevant to the activity that was launched.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null) return;
        switch (requestCode) {
            case REQUEST_DISCOVERY:
                if (resultCode == RESULT_OK) {

                    System.out.println("onActivityResult");
                    Connection connection = null;

                    switch(data.getStringExtra(TECIntent.CONNECTION_TYPE)) {
                        case TECIntent.CONNECTION_TYPE_BLUETOOTH:
                            connection = data.getParcelableExtra(TECIntent.BLUETOOTH_CONNECTION_DATA);
                            break;
                        case TECIntent.CONNECTION_TYPE_TCPIP:
                            connection = data.getParcelableExtra(TECIntent.TCPIP_CONNECTION_DATA);
                            break;
                        default:
                    }

                    if(connection != null) {
                        mDevice.addConnection(connection);
                        mDevice.notifyObservers(Device.Cues.ConnectionsUpdated);
                        mConnectionAdapter.notifyDataSetChanged();
                    }
                }
                break;
            default:
                break;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_options, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.action_rename_device:
                //TODO implement renaming
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

    private void updateActionButtonVisibility() {
        if (mActionsVisible) {
            addButton.setImageResource(R.drawable.ic_clear_white_48dp);
            addBtButton.setVisibility(View.VISIBLE);
            addTcpButton.setVisibility(View.VISIBLE);
        } else {
            addButton.setImageResource(R.drawable.ic_add_white_48dp);
            addBtButton.setVisibility(View.GONE);
            addTcpButton.setVisibility(View.GONE);
        }
    }

    private class ConnectionListAdapter extends BaseAdapter implements Observer {

        private ConnectionList mConnections;

        public ConnectionListAdapter(ConnectionList connections) {
            mConnections = Preconditions.checkNotNull(connections);
            mDevice.addObserver(this);
        }

        @Override
        public void update(Observable observable, Object data) {
            if(observable instanceof Device && data instanceof Device.Cues) {
                Device device = (Device) observable;
                Device.Cues cue = (Device.Cues) data;

                if(cue == Device.Cues.ConnectionsUpdated) {
                    mConnections = device.getConnections();
                }
            }
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
                        R.drawable.ic_bluetooth_connected_black_48dp :
                        R.drawable.ic_bluetooth_disabled_black_48dp;
                setImageButtonDrawable(iconButton, bluetoothIconId);
                iconButton.setColorFilter(ContextCompat.getColor(DeviceActivity.this, R.color.colorAccent));

                //Set callback for connection status changed to change icon
                bluetoothConnection.addObserver(new Observer() {
                    @Override
                    public void update(Observable observable, Object data) {
                        if (data instanceof Connection.Cues) {
                            Connection.Cues cue = (Connection.Cues) data;
                            switch (cue) {
                                case Connected:
                                    setImageButtonDrawable(iconButton, R.drawable.ic_bluetooth_connected_black_48dp);
                                    progressIndicator.setVisibility(View.GONE);
                                    break;
                                case Disconnected:
                                    setImageButtonDrawable(iconButton, R.drawable.ic_bluetooth_disabled_black_48dp);
                                    progressIndicator.setVisibility(View.GONE);
                                    break;
                                case ConnectFailed:
                                    setImageButtonDrawable(iconButton, R.drawable.ic_bluetooth_disabled_black_48dp);
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
                        terminalIntent.putExtra(TECIntent.CONNECTION_TYPE, TECIntent.CONNECTION_TYPE_BLUETOOTH);
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
            } else if(connection instanceof TcpIpConnection) {
                final TcpIpConnection tcpIpConnection = (TcpIpConnection) connection;

                detailsView.setText(tcpIpConnection.getServerIp() + ":" + tcpIpConnection.getServerPort());

                int wifiIconId = tcpIpConnection.isConnected() ?
                        R.drawable.ic_signal_wifi_4_bar_black_48dp :
                        R.drawable.ic_signal_wifi_off_black_48dp;
                setImageButtonDrawable(iconButton, wifiIconId);

                tcpIpConnection.addObserver(new Observer() {
                    @Override
                    public void update(Observable observable, Object data) {
                        if (data instanceof Connection.Cues) {
                            Connection.Cues cue = (Connection.Cues) data;
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

                //Set action to do on icon button click
                iconButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(tcpIpConnection.isConnected()) {
                            //Send disconnect intent
                            progressIndicator.setVisibility(View.VISIBLE);
                            LocalBroadcastManager.getInstance(DeviceActivity.this).sendBroadcast(new TcpIpDisconnectIntent(DeviceActivity.this, tcpIpConnection));
                        } else {
                            //Send connect intent
                            progressIndicator.setVisibility(View.VISIBLE);
                            LocalBroadcastManager.getInstance(DeviceActivity.this).sendBroadcast(new TcpIpConnectIntent(DeviceActivity.this, tcpIpConnection));
                        }
                    }
                });

                //Define what to do when the list item is clicked.
                listClickable.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent terminalIntent = new Intent(DeviceActivity.this, TerminalActivity.class);
                        terminalIntent.putExtra(TECIntent.TCPIP_CONNECTION_DATA, tcpIpConnection);
                        terminalIntent.putExtra(TECIntent.CONNECTION_TYPE, TECIntent.CONNECTION_TYPE_TCPIP);
                        startActivity(terminalIntent);
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
                button.setBackground(ContextCompat.getDrawable(DeviceActivity.this, resourceId));
            } else {
                button.setImageDrawable(ContextCompat.getDrawable(DeviceActivity.this, resourceId));
            }
            button.setColorFilter(0xFFFF0000);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            StorageAdapter.putDevice(mDevice);
        }
    }
}