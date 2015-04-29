package org.tec_hub.tecuniversalcomm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.tec_hub.tecuniversalcomm.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.connection.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Nick Mosher on 3/3/2015.
 */
public class DiscoveryActivity extends ActionBarActivity {

    public static final int REQUEST_ENABLE_BT = 1;

    /**
     * This BroadcastReceiver is triggered when the Bluetooth module discovers a new Bluetooth
     * device.  When this happens, make a new Connection representing that bluetooth device
     * and put it to the discovered devices adapter.
     */
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                System.out.println("Bluetooth device discovered!");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (connectionAdapter != null) {
                    BluetoothConnection btConnection = new BluetoothConnection(
                            device.getName(),
                            device.getAddress());
                    connectionAdapter.add(btConnection);
                }
            }
        }
    };

    private BluetoothAdapter bluetoothAdapter;
    private ConnectionListAdapter connectionAdapter;
    private ListView discoveredList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Discovered Devices");
        setContentView(R.layout.discovered_devices_list);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            System.out.println("Bluetooth Adapter is null; Device does not support Bluetooth.");
        }
        connectionAdapter = new ConnectionListAdapter();
        int dummy = new Random().nextInt();
        connectionAdapter.add(new BluetoothConnection("Dummy", String.valueOf(dummy > 0 ? dummy : -dummy)));

        discoveredList = (ListView) findViewById(R.id.discovered_devices_list);
        discoveredList.setAdapter(connectionAdapter);
        discoveredList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra(TECIntent.BLUETOOTH_CONNECTION_DATA, (Parcelable) connectionAdapter.getItem(position));
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        registerReceiver(btReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        discoveredList.deferNotifyDataSetChanged();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    protected void onStart() {
        super.onStart();
        bluetoothAdapter.startDiscovery();
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btReceiver);
    }

    private class ConnectionListAdapter extends BaseAdapter {
        private List<Connection> discoveredConnections;

        public ConnectionListAdapter() {
            discoveredConnections = new ArrayList<>();
        }

        public void add(Connection connection) {
            if (connection != null) {
                boolean flagDuplicate = false;
                for (Connection c : discoveredConnections) {
                    if (c.equals(connection)) {
                        flagDuplicate = true;
                        break;
                    }
                }
                if (!flagDuplicate) {
                    discoveredConnections.add(connection);
                    notifyDataSetChanged();
                }
            }
        }

        public int getCount() {
            if (discoveredConnections != null) {
                return discoveredConnections.size();
            }
            return 0;
        }

        public Object getItem(int position) {
            return discoveredConnections.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        /**
         * Returns a view that represents a discovered device.
         * @param position    The position of this discovered device in it's parent ListView.
         * @param convertView The previous instance of this view, can be used to refresh view.
         * @param parent      The parent of the view to return.
         * @return
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout view;

            if (convertView == null) {
                view = new LinearLayout(DiscoveryActivity.this);
                LayoutInflater inflater = (LayoutInflater) DiscoveryActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.inflate(R.layout.discovered_bt_device_list_item, view, true);
            } else {
                view = (LinearLayout) convertView;
            }
            TextView nameTextView = (TextView) view.findViewById(R.id.discovered_bt_name);
            TextView addressTextView = (TextView) view.findViewById(R.id.discovered_bt_address);

            Connection connection = discoveredConnections.get(position);
            if (connection instanceof BluetoothConnection) {
                BluetoothConnection btConnection = (BluetoothConnection) connection;
                String name = btConnection.getName();
                String address = btConnection.getAddress();

                nameTextView.setText((name == null) ? "No name" : name);
                addressTextView.setText(address);
            }

            return view;
        }
    }
}
