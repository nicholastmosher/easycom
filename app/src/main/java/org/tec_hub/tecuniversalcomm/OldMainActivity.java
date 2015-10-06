package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.device.DeviceList;
import org.tec_hub.tecuniversalcomm.data.connection.ConnectionService;
import org.tec_hub.tecuniversalcomm.data.device.Device;
import org.tec_hub.tecuniversalcomm.data.StorageAdapter;
import org.tec_hub.tecuniversalcomm.dialogs.DialogRenameDevice;
import org.tec_hub.tecuniversalcomm.intents.TECIntent;

public class OldMainActivity extends AppCompatActivity {

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
        setContentView(R.layout.old_activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("TEC COMM | Devices");

        //Creates a dialog to make a new Device.
        final EditText deviceName = new EditText(this);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Create New Device");
        dialogBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!deviceName.getText().toString().equals("")) {
                    Device newDevice = Device.build(deviceName.getText().toString());
                    mDeviceAdapter.put(newDevice);
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialogBuilder.setView(deviceName);
        final AlertDialog newDeviceDialog = dialogBuilder.create();

        FloatingActionButton actionButton = (FloatingActionButton) findViewById(R.id.action_button);
        actionButton.setImageResource(R.drawable.ic_add_white_48dp);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceName.setText("");
                newDeviceDialog.show();
            }
        });
        setSupportActionBar(toolbar);

        StorageAdapter.init(this);
        mDeviceAdapter = new DeviceListAdapter();
        mDeviceListView = (ListView) findViewById(R.id.tec_activity_listview);
        mDeviceListView.setAdapter(mDeviceAdapter);
        mDeviceListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        ConnectionService.launch(this); //FIXME does this need to launch here?
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
        private DeviceList mDevices;

        public DeviceListAdapter() {
            mDevices = StorageAdapter.getDevices();
        }

        /**
         * Inserts the given device into storage and notifies the mDeviceListView of a data update.
         * @param newDevice The device to add to memory.
         */
        public void put(Device newDevice) {
            Preconditions.checkNotNull(newDevice);
            mDevices.add(newDevice);
            notifyDataSetChanged();
        }

        /**
         * If the given device exists in storage, delete it and remove it from the mDeviceListView.
         * @param device
         */
        public void delete(Device device) {
            mDevices.remove(device);
            notifyDataSetChanged();
        }

        @Override
        public void notifyDataSetChanged() {
            StorageAdapter.setDevices(mDevices);
            super.notifyDataSetChanged();
        }

        public int getCount() {
            if (mDevices != null) {
                return mDevices.size();
            }
            return 0;
        }

        public Object getItem(int position) {
            return mDevices.get(position);
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
                root = new LinearLayout(OldMainActivity.this);
                LayoutInflater inflater = (LayoutInflater) OldMainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.inflate(R.layout.list_item_device, root, true);
            }

            //Retrieve current Device object
            final Device device = (Device) getItem(position);
            Preconditions.checkNotNull(device);

            //Load views from xml
            final TextView nameView = (TextView) root.findViewById(R.id.device_name);
            TextView detailsView = (TextView) root.findViewById(R.id.device_details);
            ImageButton deviceImageButton = (ImageButton) root.findViewById(R.id.device_image_button);
            RelativeLayout listClickable = (RelativeLayout) root.findViewById(R.id.list_clickable);
            ImageButton optionButton = (ImageButton) root.findViewById(R.id.device_options);

            //Set the title to the device name
            nameView.setText(device.getName());
            detailsView.setText("Connections: " + device.getConnections().size());

            //Set the image resource of the device icon button based on SDK version
            if (Build.VERSION.SDK_INT >= 16) {
                deviceImageButton.setBackground(ContextCompat.getDrawable(OldMainActivity.this, R.drawable.ic_memory_black_48dp));
            } else {
                deviceImageButton.setImageDrawable(ContextCompat.getDrawable(OldMainActivity.this, R.drawable.ic_memory_black_48dp));
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
                    Intent intent = new Intent(OldMainActivity.this, DeviceActivity.class);
                    intent.putExtra(TECIntent.DEVICE_UUID, ((Device) mDeviceAdapter.getItem(position)).getUUID());
                    startActivity(intent);
                }
            });

            //Create a popup menu to launch when the options button is pressed
            final PopupMenu optionMenu = new PopupMenu(OldMainActivity.this, optionButton);
            optionMenu.inflate(R.menu.menu_device_options);
            optionMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    //Switch action based on clicked item
                    switch(item.getItemId()) {
                        case R.id.action_rename_device:
                            new DialogRenameDevice(OldMainActivity.this, device).rename();
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