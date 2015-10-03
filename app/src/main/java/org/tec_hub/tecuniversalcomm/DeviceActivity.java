package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
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
import org.tec_hub.tecuniversalcomm.data.connection.Connection;
import org.tec_hub.tecuniversalcomm.data.connection.ConnectionList;
import org.tec_hub.tecuniversalcomm.data.connection.ConnectionObserver;
import org.tec_hub.tecuniversalcomm.data.connection.ConnectionService;
import org.tec_hub.tecuniversalcomm.data.connection.TcpIpConnection;
import org.tec_hub.tecuniversalcomm.data.device.Device;
import org.tec_hub.tecuniversalcomm.data.device.DeviceObserver;
import org.tec_hub.tecuniversalcomm.dialogs.DialogNewTcpIp;
import org.tec_hub.tecuniversalcomm.dialogs.DialogRenameConnection;
import org.tec_hub.tecuniversalcomm.intents.BluetoothConnectIntent;
import org.tec_hub.tecuniversalcomm.intents.BluetoothDisconnectIntent;
import org.tec_hub.tecuniversalcomm.intents.TECIntent;
import org.tec_hub.tecuniversalcomm.intents.TcpIpConnectIntent;
import org.tec_hub.tecuniversalcomm.intents.TcpIpDisconnectIntent;

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
        setContentView(R.layout.activity_device);

        //Get the intent that launched this activity
        Intent launchIntent = getIntent();
        Preconditions.checkNotNull(launchIntent);

        Device device = Device.getDevice(launchIntent.getStringExtra(TECIntent.DEVICE_UUID));
        if(device != null) {
            //Parse the device sent to us on the launching intent
            mDevice = device;
            lastDevice = device;
        } else if(lastDevice != null) {
            mDevice = lastDevice;
        } else {
            //If there's no device object to read data from, we shouldn't be at this activity.
            finish();
            return;
        }

        //Set the title of the activity to the device name
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mDevice.getName() + " | Connections");
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);

        //Set up Floating Action Buttons for adding Connections.
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

        final AlertDialog newTcpDialog = DialogNewTcpIp.build(this, mDevice);
        addTcpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newTcpDialog.show();
            }
        });

        //Initialize the ListView and Adapter with the Connection data from the active device
        mListView = (ListView) findViewById(R.id.device_manager_list);
        mConnectionAdapter = new ConnectionListAdapter(mDevice.getConnections());
        mListView.setAdapter(mConnectionAdapter);

        ConnectionService.launch(this);
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
                    Connection connection = Connection.getConnection(data.getStringExtra(TECIntent.CONNECTION_UUID));

                    if(connection != null) {
                        mDevice.addConnection(connection);
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
                //todo rename device
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

    /**
     * Manages taking Connection data for the current device and translating
     * that data into View representations for placement on the ListView.
     */
    private class ConnectionListAdapter extends BaseAdapter implements DeviceObserver {

        private ConnectionList mConnections;

        public ConnectionListAdapter(ConnectionList connections) {
            mConnections = Preconditions.checkNotNull(connections);
            mDevice.addObserver(this);
        }

        /**
         * Since this adapter observes the current device, we need a callback
         * to indicate whenever the data in the device has been changed.
         * @param observable The object we're watching for a change,  In this case, the Device.
         * @param cue An enum value that depicts what kind of update occurred.
         */
        public void onUpdate(Device observable, Device.Status cue) {
            switch(cue) {
                case ConnectionsUpdated:
                    System.out.println("ConnectionListAdapter Observer Device updated");
                    mConnections = observable.getConnections();
                    notifyDataSetChanged();
                    break;
                case ConnectionNameUpdated:
                    mConnections = observable.getConnections();
                    notifyDataSetChanged();
                    break;
            }
        }

        /**
         * Function to delete connections
         * @param connection
         * @param observable
         */
        public void delete(Connection connection, Device observable){
            observable.removeConnection(connection);
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

        /**
         * Generates views to insert into the list based on the active Device's data.
         * @param position The index in the Device's ConnectionList to read data from.
         * @param convertView A previous version of the View for the current element.
         * @param parent
         * @return A View representing the Device's Connection data at this index.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            //Inflate or instantiate the root view
            RelativeLayout root;
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

                //Cast the connection specifically to a BluetoothConnection.
                final BluetoothConnection bluetoothConnection = (BluetoothConnection) connection;

                //Set details TextView to display bluetooth address
                detailsView.setText(bluetoothConnection.getAddress());

                //Define icons that could be used for this list item depending on connection status.
                final Drawable iconConnected = ContextCompat.getDrawable(DeviceActivity.this, R.drawable.ic_bluetooth_connected_black_48dp);
                iconConnected.setColorFilter(ContextCompat.getColor(DeviceActivity.this, R.color.connected), PorterDuff.Mode.SRC_ATOP);
                final Drawable iconDisconnected = ContextCompat.getDrawable(DeviceActivity.this, R.drawable.ic_bluetooth_disabled_black_48dp);
                iconDisconnected.setColorFilter(ContextCompat.getColor(DeviceActivity.this, R.color.disconnected), PorterDuff.Mode.SRC_ATOP);
                //Apply the appropriate initial icon based on current connection status.
                setImageButtonDrawable(iconButton, (connection.getStatus().equals(Connection.Status.Connected)) ?
                        iconConnected : iconDisconnected);

                //Set callback for connection status changed to change icon
                bluetoothConnection.addObserver(new ConnectionObserver() {
                    @Override
                    public void onUpdate(Connection observable, Connection.Status cue) {
                        switch (cue) {
                            case Connected:
                                setImageButtonDrawable(iconButton, iconConnected);
                                progressIndicator.setVisibility(View.GONE);
                                break;
                            case Disconnected:
                                setImageButtonDrawable(iconButton, iconDisconnected);
                                progressIndicator.setVisibility(View.GONE);
                                break;
                            case ConnectFailed:
                                setImageButtonDrawable(iconButton, iconDisconnected);
                                progressIndicator.setVisibility(View.GONE);
                                break;
                            default:
                        }
                    }
                });
                //Set action to do on icon button click
                iconButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(bluetoothConnection.getStatus().equals(Connection.Status.Connected)) {
                            //Send disconnect intent
                            LocalBroadcastManager.getInstance(DeviceActivity.this).sendBroadcast(
                                    new BluetoothDisconnectIntent(DeviceActivity.this, bluetoothConnection.getUUID()));
                            progressIndicator.setVisibility(View.VISIBLE);
                        } else {
                            //Send connect intent
                            LocalBroadcastManager.getInstance(DeviceActivity.this).sendBroadcast(
                                    new BluetoothConnectIntent(DeviceActivity.this, bluetoothConnection.getUUID()));
                            progressIndicator.setVisibility(View.VISIBLE);
                        }
                    }
                });

                //Set action to do when list item is clicked
                listClickable.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent terminalIntent = new Intent(DeviceActivity.this, TerminalActivity.class);
                        terminalIntent.putExtra(TECIntent.CONNECTION_UUID, bluetoothConnection.getUUID());
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
                                terminalIntent.putExtra(TECIntent.CONNECTION_UUID, bluetoothConnection.getUUID());
                                startActivity(terminalIntent);
                                return true;
                            case R.id.action_open_kudos:
                                Intent kudosIntent = new Intent(DeviceActivity.this, KudosActivity.class);
                                kudosIntent.putExtra(TECIntent.CONNECTION_UUID, bluetoothConnection.getUUID());
                                startActivity(kudosIntent);
                                return true;
                            case R.id.action_open_controller:
                                //TODO add functionality.
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

            //Do things unique to TcpIpConnections.
            } else if(connection instanceof TcpIpConnection) {

                //Cast the connection specifically to a TcpIpConnection.
                final TcpIpConnection tcpIpConnection = (TcpIpConnection) connection;

                detailsView.setText(tcpIpConnection.getServerIp() + ":" + tcpIpConnection.getServerPort());

                //Define icons that can be used for this list item based on the connection's status.
                final Drawable iconConnected = ContextCompat.getDrawable(DeviceActivity.this, R.drawable.ic_signal_wifi_4_bar_black_48dp);
                iconConnected.setColorFilter(ContextCompat.getColor(DeviceActivity.this, R.color.connected), PorterDuff.Mode.SRC_ATOP);
                final Drawable iconDisconnected = ContextCompat.getDrawable(DeviceActivity.this, R.drawable.ic_signal_wifi_off_black_48dp);
                iconConnected.setColorFilter(ContextCompat.getColor(DeviceActivity.this, R.color.disconnected), PorterDuff.Mode.SRC_ATOP);

                //Apply initial icon based on the connection's current status.
                setImageButtonDrawable(iconButton, (connection.getStatus().equals(Connection.Status.Connected)) ?
                        iconConnected : iconDisconnected);

                //Define callbacks for when the connection's status changes in order to update the view.
                tcpIpConnection.addObserver(new ConnectionObserver() {
                    @Override
                    public void onUpdate(Connection observable, Connection.Status cue) {
                        switch (cue) {
                            case Connected:
                                setImageButtonDrawable(iconButton, iconConnected);
                                progressIndicator.setVisibility(View.GONE);
                                break;
                            case Disconnected:
                                setImageButtonDrawable(iconButton, iconDisconnected);
                                progressIndicator.setVisibility(View.GONE);
                                break;
                            case ConnectFailed:
                                setImageButtonDrawable(iconButton, iconDisconnected);
                                progressIndicator.setVisibility(View.GONE);
                                break;
                            default:
                        }
                    }
                });

                //Set action to do on icon button click
                iconButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (tcpIpConnection.getStatus().equals(Connection.Status.Connected)) {
                            //Send disconnect intent
                            LocalBroadcastManager.getInstance(DeviceActivity.this).sendBroadcast(
                                    new TcpIpDisconnectIntent(DeviceActivity.this, tcpIpConnection.getUUID()));
                            progressIndicator.setVisibility(View.VISIBLE);
                        } else {
                            //Send connect intent
                            LocalBroadcastManager.getInstance(DeviceActivity.this).sendBroadcast(
                                    new TcpIpConnectIntent(DeviceActivity.this, tcpIpConnection.getUUID()));
                            progressIndicator.setVisibility(View.VISIBLE);
                        }
                    }
                });

                //Define what to do when the list item is clicked.
                listClickable.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent terminalIntent = new Intent(DeviceActivity.this, TerminalActivity.class);
                        terminalIntent.putExtra(TECIntent.CONNECTION_UUID, tcpIpConnection.getUUID());
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
                            case R.id.action_rename_connection:
                                //Renaming class
                                new DialogRenameConnection(DeviceActivity.this, connection, mDevice).rename();
                                return true;
                            case R.id.action_delete_connection:
                                //Deleting function
                                delete(connection, mDevice);
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

        /**
         * Uses a different method to set button icons depending on Android version.
         * @param button The button whose icon to change.
         * @param icon The icon to apply to the button.
         */
        private void setImageButtonDrawable(final ImageButton button, Drawable icon) {
            if(Build.VERSION.SDK_INT >= 16) {
                button.setBackground(icon);
            } else {
                button.setImageDrawable(icon);
            }
        }
    }
}