package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

public class TECActivity extends ActionBarActivity
{
    public static final int REQUEST_DISCOVERY = 2;

    /**
     * The View item that displays all device items in a list.
     */
    private ListView deviceListView;

    /**
     * The custom data adapter that ports device metadata to the deviceListView.
     */
    private DeviceListAdapter deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tec);

        TECDataAdapter.init(this);
        deviceAdapter = new DeviceListAdapter(this);
        deviceListView = (ListView) findViewById(R.id.tec_activity_listview);
        deviceListView.setAdapter(deviceAdapter);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tec, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            System.out.println("Settings button!");
            return true;
        }
        else if(id == R.id.action_add_device)
        {
            //Open the ConnectionDiscoveryActivity when the Add button is pressed.
            startActivityForResult(new Intent(TECActivity.this, ConnectionDiscoveryActivity.class), REQUEST_DISCOVERY);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * System callback that executes when some activity called with startActivityForResult() returns
     * with a response.  The requestCode is the code we gave when launching the activity for result,
     * the resultCode is a code given by the launched activity that indicates the status of the
     * result given.
     * @param requestCode The code given during startActivityForResult().
     * @param resultCode A code indicating the status of the return (e.g. RESULT_OK).
     * @param data An intent with data relevant to the activity that was launched.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case REQUEST_DISCOVERY:
                if(resultCode == RESULT_OK)
                {
                    Connection connection = data.getParcelableExtra(ConnectionDiscoveryActivity.EXTRA_CONNECTION);
                    //TODO put option to put connection in existing devices
                    Device device = new Device(connection);
                    deviceAdapter.put(device);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Serves data about current Device data to the deviceListView.  Manages the dynamic and
     * persistent storage of the configured Devices and constructs views of each individual
     * list item for placement in the list.
     */
    private class DeviceListAdapter extends BaseAdapter
    {
        /**
         * Dynamic array that keeps track of all devices currently being managed.
         * This is held in memory and is readily accessible so that system calls
         * requesting View updates can be satisfied quickly.
         */
        private ArrayList<Device> deviceEntries;
        private Context context;

        public DeviceListAdapter(Context context)
        {
            this.context = context;
            this.deviceEntries = new ArrayList<Device>();
            populateFromStorage();
        }

        /**
         * Inserts the given device into storage and notifies the deviceListView of a data update.
         * @param device The device to add to memory.
         */
        public void put(Device device)
        {
            if(device != null)
            {
                boolean flagDuplicate = false;
                for (Device d : deviceEntries)
                {
                    if (device.hashCode() == d.hashCode())
                    {
                        flagDuplicate = true;
                        break;
                    }
                }
                if (!flagDuplicate)
                {
                    deviceEntries.add(device);
                    notifyDataSetChanged();
                    TECDataAdapter.putDevice(device);
                }
            }
        }

        /**
         * If the given device exists in storage, delete it and remove it from the deviceListView.
         * @param device
         */
        public void delete(Device device)
        {
            if(device != null)
            {
                //Remove device from deviceEntries
                for (Iterator<Device> i = deviceEntries.iterator(); i.hasNext(); )
                {
                    Device d = i.next();
                    if (device.equals(d))
                    {
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
        public void populateFromStorage()
        {
            ArrayList<Device> tempFileDevices = TECDataAdapter.readDevicesFromFile();
            if(tempFileDevices != null)
            {
                deviceEntries = tempFileDevices;
                notifyDataSetChanged();
            }
        }

        public int getCount()
        {
            if(deviceEntries != null)
            {
                return deviceEntries.size();
            }
            return 0;
        }

        public Object getItem(int position)
        {
            return deviceEntries.get(position);
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            LinearLayout view = null;

            if(convertView == null) //Regenerate the view
            {
                final Device device = deviceEntries.get(position);
                view = new LinearLayout(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                /*
                 * Below, inflate a different layout of list item depending on what
                 * type of connections each device has.
                 * TODO move drawable responsibilities to device object
                 */
                 inflater.inflate(R.layout.device_list_item, view, true);
                ((TextView) view.findViewById(R.id.bt_name)).setText(device.getName());
                ((TextView) view.findViewById(R.id.bt_address)).setText(device.getConnections().get(0).getBluetoothAddress());

                final ImageButton btButton = (ImageButton) view.findViewById(R.id.list_bt_btn);
                btButton.setBackground(TECActivity.this.getResources().getDrawable(R.drawable.bt_icon_live));
                btButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int[] state = btButton.getDrawable().getState();
                        boolean enabledExists = false;
                        for(int s : state)
                        {
                            if(s == android.R.attr.state_enabled || s == -android.R.attr.state_enabled)
                            {
                                s = -s;
                                enabledExists = true;
                            }
                        }

                        if(!enabledExists)
                        {
                            int[] newState = new int[state.length + 1];
                            for(int i = 0; i < state.length; i++)
                            {
                                newState[i] = state[i];
                            }
                            state[state.length - 1] = android.R.attr.state_enabled;
                        }
                        btButton.getDrawable().setState(state);
                    }
                });

                ImageButton optionButton = (ImageButton) view.findViewById(R.id.device_options);
                final PopupMenu optionMenu = new PopupMenu(TECActivity.this, optionButton);
                optionMenu.inflate(R.menu.menu_device_options);
                optionMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        int id = item.getItemId();
                        if(id == R.id.action_delete_device)
                        {
                            delete(device);
                            return true;
                        }

                        return false;
                    }
                });

                optionButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //Show the menu for the selected device
                        optionMenu.show();
                    }
                });
            }
            else //Reuse the view
            {
                view = (LinearLayout) convertView;
            }
            return view;
        }
    }
}
