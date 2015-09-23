package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnectionService;
import org.tec_hub.tecuniversalcomm.data.connection.Connection;
import org.tec_hub.tecuniversalcomm.data.Device;
import org.tec_hub.tecuniversalcomm.data.StorageAdapter;
import org.tec_hub.tecuniversalcomm.data.connection.TcpIpConnection;
import org.tec_hub.tecuniversalcomm.intents.BluetoothDiscoveredIntent;
import org.tec_hub.tecuniversalcomm.intents.TECIntent;
import org.tec_hub.tecuniversalcomm.intents.TcpIpDiscoveredIntent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_DISCOVERY = 2;

    /**
     * The View item that displays all device items in a list.
     */
    private ListView mDeviceListView;

    /**
     * The custom data adapter that ports device data to the mDeviceListView.
     */
    private DeviceListAdapter mDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("TEC COMM | Devices");
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.textColor));
        FloatingActionButton actionButton = (FloatingActionButton) findViewById(R.id.action_button);
        actionButton.setImageResource(R.drawable.ic_action_new);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, DiscoveryActivity.class), REQUEST_DISCOVERY);
            }
        });
        setSupportActionBar(toolbar);

        StorageAdapter.init(this);
        mDeviceAdapter = new DeviceListAdapter();
        mDeviceListView = (ListView) findViewById(R.id.tec_activity_listview);
        mDeviceListView.setAdapter(mDeviceAdapter);
        mDeviceListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        BluetoothConnectionService.launch(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tec, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                System.out.println("Settings button!");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        switch (requestCode) {
            case REQUEST_DISCOVERY:
                if (resultCode == RESULT_OK) {

                    System.out.println("onActivityResult");
                    Connection connection = null;
                    if(data instanceof BluetoothDiscoveredIntent) {
                        connection = data.getParcelableExtra(TECIntent.BLUETOOTH_CONNECTION_DATA);
                    } else if(data instanceof TcpIpDiscoveredIntent) {
                        System.out.println("Got TCPIPDiscoveredIntent");
                        connection = data.getParcelableExtra(TECIntent.TCPIP_CONNECTION_DATA);
                    }

                    if(connection != null) {
                        //TODO put option to put connection in existing devices
                        Device device = Device.build(connection.getName(), connection);
                        mDeviceAdapter.put(device);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * Serves data about current Device data to the mDeviceListView.  Manages the dynamic and
     * persistent storage of the configured Devices and constructs views of each individual
     * list item for placement in the list.
     */
    private class DeviceListAdapter extends BaseAdapter {

        /**
         * Dynamic array that keeps track of all devices currently being managed.
         * This is held in memory and is readily accessible so that system calls
         * requesting View updates can be satisfied quickly.
         */
        private List<Device> mDeviceEntries;

        public DeviceListAdapter() {
            this.mDeviceEntries = new ArrayList<>();
            populateFromStorage();
        }

        /**
         * Inserts the given device into storage and notifies the mDeviceListView of a data update.
         * @param newDevice The device to add to memory.
         */
        public void put(Device newDevice) {
            Preconditions.checkNotNull(newDevice);
            boolean flagUpdatedExisting = false;
            for (Device device : mDeviceEntries) {
                if (newDevice.isVersionOf(device)) {
                    int index = mDeviceEntries.indexOf(device);
                    if(index != -1) {
                        mDeviceEntries.set(index, newDevice);
                        flagUpdatedExisting = true;
                        break;
                    } else {
                        throw new IllegalStateException("Cannot find device index!");
                    }
                }
            }
            //If an existing device was not updated, then this is a new device, add it to the list
            if (!flagUpdatedExisting) {
                mDeviceEntries.add(newDevice);
            }
            notifyDataSetChanged();
        }

        /**
         * If the given device exists in storage, delete it and remove it from the mDeviceListView.
         * @param device
         */
        public void delete(Device device) {
            Preconditions.checkNotNull(device);
            //Remove device from mDeviceEntries
            Iterator iterator = mDeviceEntries.iterator();
            while(iterator.hasNext()) {
                Device d = (Device) iterator.next();
                if(device.isVersionOf(d)) {
                    iterator.remove();
                }
            }
            notifyDataSetChanged();
        }

        /**
         * Retrieves Device entries from persistent storage and loads them into the dynamic
         * array responsible for displaying the entries in the listView.
         */
        public void populateFromStorage() {
            List<Device> temp = Preconditions.checkNotNull(StorageAdapter.getDevices());
            mDeviceEntries = temp;
        }

        @Override
        public void notifyDataSetChanged() {
            StorageAdapter.setDevices(mDeviceEntries);
            super.notifyDataSetChanged();
        }

        public int getCount() {
            if (mDeviceEntries != null) {
                return mDeviceEntries.size();
            }
            return 0;
        }

        public Object getItem(int position) {
            return mDeviceEntries.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            LinearLayout root;

            //Inflate or instantiate the root view
            if (convertView != null) {
                root = (LinearLayout) convertView;
            } else {
                root = new LinearLayout(MainActivity.this);
                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.inflate(R.layout.list_item_device, root, true);
            }

            //Retrieve current Device object
            final Device device = (Device) getItem(position);
            Preconditions.checkNotNull(device);

            //Load views from xml
            TextView nameView = (TextView) root.findViewById(R.id.device_name);
            TextView detailsView = (TextView) root.findViewById(R.id.device_details);
            ImageButton deviceImageButton = (ImageButton) root.findViewById(R.id.device_image_button);
            RelativeLayout listClickable = (RelativeLayout) root.findViewById(R.id.list_clickable);
            ImageButton optionButton = (ImageButton) root.findViewById(R.id.device_options);

            //Set the title to the device name
            nameView.setText(device.getName());
            detailsView.setText("Connections: " + device.getConnections().size());

            //Set the image resource of the device icon button based on SDK version
            if (Build.VERSION.SDK_INT >= 16) {
                deviceImageButton.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_memory_black_36dp));
            } else {
                deviceImageButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_memory_black_36dp));
            }

            //Set action to do on device icon button pressed
            deviceImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            //Set the clickable area of the list item
            listClickable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
                    intent.putExtra(TECIntent.DEVICE_DATA, (Device) mDeviceAdapter.getItem(position));
                    startActivity(intent);
                }
            });

            //Create a popup menu to launch when the options button is pressed
            final PopupMenu optionMenu = new PopupMenu(MainActivity.this, optionButton);
            optionMenu.inflate(R.menu.menu_device_options);
            optionMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    //Switch action based on clicked item
                    switch(item.getItemId()) {
                        case R.id.action_rename_device:
                            Device.promptRename(device, MainActivity.this); //FIXME does not change persistently
                            notifyDataSetChanged();
                            return true;
                        case R.id.action_delete_device:
                            delete(device);
                            return true;
                    }
                    return false;
                }
            });

            //Show options menu on option button clicked
            optionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    optionMenu.show();
                }
            });

            return root;
        }
    }
}