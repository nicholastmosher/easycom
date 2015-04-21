package org.tec_hub.tecuniversalcomm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
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

import org.tec_hub.tecuniversalcomm.Connection.Connection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TECActivity extends ActionBarActivity {

    public static final int REQUEST_DISCOVERY = 2;

    /**
     * The View item that displays all device items in a list.
     */
    private ListView mDeviceListView;

    /**
     * The custom data adapter that ports device metadata to the mDeviceListView.
     */
    private DeviceListAdapter mDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tec);

        TECDataAdapter.init(this);
        mDeviceAdapter = new DeviceListAdapter(this);
        mDeviceListView = (ListView) findViewById(R.id.tec_activity_listview);
        mDeviceListView.setAdapter(mDeviceAdapter);
        mDeviceListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_settings) {
            System.out.println("Settings button!");
            return true;
        } else if(id == R.id.action_add_device) {
            //Open the ConnectionDiscoveryActivity when the Add button is pressed.
            startActivityForResult(new Intent(TECActivity.this, ConnectionDiscoveryActivity.class), REQUEST_DISCOVERY);
            return true;
        } else if(id == R.id.action_delete_all_devices) {
            TECDataAdapter.wipeDevicesFile();
            mDeviceAdapter.populateFromStorage();
            return true;
        } else if(id == R.id.action_refresh_devices) {
            mDeviceAdapter.populateFromStorage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * System callback that executes when some activity called with startActivityForResult() returns
     * with a response.  The requestCode is the code we gave when launching the activity for result,
     * the resultCode is a code given by the launched activity that indicates the status of the
     * result given.
     *
     * @param requestCode The code given during startActivityForResult().
     * @param resultCode  A code indicating the status of the return (e.g. RESULT_OK).
     * @param data        An intent with data relevant to the activity that was launched.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_DISCOVERY:
                if (resultCode == RESULT_OK) {
                    Connection connection = data.getParcelableExtra(ConnectionDiscoveryActivity.EXTRA_CONNECTION);
                    //TODO put option to put connection in existing devices
                    ArrayList<Connection> connections = new ArrayList<>();
                    connections.add(connection);
                    Device device = Device.build("Dummy", connections);
                    mDeviceAdapter.put(device);
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
        private Context mContext;

        public DeviceListAdapter(Context context) {
            this.mContext = context;
            this.mDeviceEntries = new ArrayList<>();
            populateFromStorage();
        }

        /**
         * Inserts the given device into storage and notifies the mDeviceListView of a data update.
         *
         * @param device The device to add to memory.
         */
        public void put(Device device) {
            if (device != null) {
                boolean flagDuplicate = false;
                for (Device d : mDeviceEntries) {
                    if (device.hashCode() == d.hashCode()) {
                        flagDuplicate = true;
                        break;
                    }
                }
                if (!flagDuplicate) {
                    mDeviceEntries.add(device);
                    notifyDataSetChanged();
                    TECDataAdapter.putDevice(device);
                }
            }
        }

        /**
         * If the given device exists in storage, delete it and remove it from the mDeviceListView.
         *
         * @param device
         */
        public void delete(Device device) {
            if (device != null) {
                //Remove device from mDeviceEntries
                for (Iterator<Device> i = mDeviceEntries.iterator(); i.hasNext(); ) {
                    Device d = i.next();
                    if (device.equals(d)) {
                        i.remove(); //Removes currently indexed item
                        notifyDataSetChanged();
                        TECDataAdapter.deleteDevice(device);
                    }
                }
            }
        }

        /**
         * Retrieves Device entries from persistent storage and loads them into the dynamic
         * array responsible for displaying the entries in the listView.
         */
        public void populateFromStorage() {
            mDeviceEntries = Preconditions.checkNotNull(TECDataAdapter.readDevicesFromFile());
            notifyDataSetChanged();
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
            LinearLayout view;

            if (convertView == null) //Regenerate the view
            {
                final Device device = mDeviceEntries.get(position);
                view = new LinearLayout(mContext);
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                /*
                 * Below, inflate a different layout of list item depending on what
                 * type of connections each device has.
                 */
                inflater.inflate(R.layout.device_list_item, view, true);
                ((TextView) view.findViewById(R.id.bt_name)).setText(device.getName());
                if(device.getBluetoothConnection() != null) {
                    ((TextView) view.findViewById(R.id.bt_address)).setText(device.getBluetoothConnection().getAddress());
                }

                //Set the image resource of the bluetooth icon button based on SDK version
                ImageButton btButton = (ImageButton) view.findViewById(R.id.list_bt_btn);
                if (Build.VERSION.SDK_INT >= 16) {
                    btButton.setBackground(TECActivity.this.getResources().getDrawable(R.drawable.bt_icon_live));
                } else {
                    btButton.setImageDrawable(TECActivity.this.getResources().getDrawable(R.drawable.bt_icon_live));
                }

                btButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO obviously disabling a button will make this unreachable, find another icon changing method
                        ImageButton button = (ImageButton) v;
                        button.setEnabled(!button.isEnabled());
                    }
                });

                //Set the RelativeLayout of the list item (a sizeable chunk) as clickable
                RelativeLayout listClickable = (RelativeLayout) view.findViewById(R.id.list_clickable);
                listClickable.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(TECActivity.this, ConnectionTerminalActivity.class);
                        intent.putExtra(TECIntent.BLUETOOTH_CONNECTION_DATA, (Parcelable) device.getBluetoothConnection());
                        startActivity(intent);
                    }
                });

                //Set an option button that opens a popup menu with a delete option.
                ImageButton optionButton = (ImageButton) view.findViewById(R.id.device_options);
                final PopupMenu optionMenu = new PopupMenu(TECActivity.this, optionButton);
                optionMenu.inflate(R.menu.menu_device_options);
                optionMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if(id == R.id.action_rename_device) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(TECActivity.this);

                            alert.setTitle("Title");
                            alert.setMessage("Message");

                            // Set an EditText view to get user input
                            final EditText input = new EditText(TECActivity.this);
                            alert.setView(input);

                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String value = input.getText().toString();
                                    // Do something with value!
                                }
                            });

                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Canceled.
                                }
                            });

                            alert.show();
                        } else if(id == R.id.action_delete_device) {
                            delete(device);
                            return true;
                        }
                        return false;
                    }
                });

                optionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Show the menu for the selected device
                        optionMenu.show();
                    }
                });
            } else //Reuse the view
            {
                view = (LinearLayout) convertView;
            }
            return view;
        }
    }
}
