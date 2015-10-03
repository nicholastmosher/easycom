package org.tec_hub.tecuniversalcomm;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.data.connection.ConnectionList;
import org.tec_hub.tecuniversalcomm.data.connection.TcpIpConnection;
import org.tec_hub.tecuniversalcomm.intents.TECIntent;

/**
 * Created by Nick Mosher on 9/30/15.
 */
public class NewTerminalActivity extends AppCompatActivity {

    private ConnectionList mConnections;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_terminal);

        mConnections = new ConnectionList();
        mConnections.add(new BluetoothConnection("Name", "Address"));
        mConnections.add(new BluetoothConnection("Name", "Address"));
        mConnections.add(new BluetoothConnection("Name", "Address"));
        mConnections.add(new BluetoothConnection("Name", "Address"));
        mConnections.add(new BluetoothConnection("Name", "Address"));
        mConnections.add(new BluetoothConnection("Name", "Address"));
        mConnections.add(new BluetoothConnection("Name", "Address"));
        mConnections.add(new BluetoothConnection("Name", "Address"));
        mConnections.add(new TcpIpConnection("Name", "Address", 7777));
        mConnections.add(new BluetoothConnection("Name", "Address"));
        mConnections.add(new BluetoothConnection("Name", "Address"));

        //Initialize toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.terminal_toolbar);
        toolbar.setTitle("Title");
        Drawable navIcon = ContextCompat.getDrawable(this, R.drawable.ic_menu_black_48dp);
//        navIcon.setColorFilter(ContextCompat.getColor(this, R.color.neutral), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationIcon(navIcon);

        //Initialize Recycler
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.terminal_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new DrawerLayoutAdapter(mConnections));

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerListener(new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_closed) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        });
    }

    public class DrawerLayoutAdapter extends RecyclerView.Adapter<DrawerLayoutAdapter.DrawerViewHolder> {

        private ConnectionList mConnections;

        public class DrawerViewHolder extends RecyclerView.ViewHolder {

            public LinearLayout mContainer;
            public ImageView mIcon;
            public TextView mTitle;

            public DrawerViewHolder(LinearLayout container) {
                super(container);
                mContainer = container;
                mIcon = (ImageView) mContainer.findViewById(R.id.list_icon);
                mTitle = (TextView) mContainer.findViewById(R.id.list_title);
            }
        }

        public DrawerLayoutAdapter(ConnectionList connections) {
            mConnections = connections;
        }

        @Override
        public DrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout listItem = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.new_list_item_connection, parent, false);

            return new DrawerViewHolder(listItem);
        }

        @Override
        public void onBindViewHolder(DrawerViewHolder holder, int position) {

            //Set icon.
            Drawable icon;
            switch(mConnections.get(position).getConnectionType()) {
                case TECIntent.CONNECTION_TYPE_BLUETOOTH:
                    icon = ContextCompat.getDrawable(NewTerminalActivity.this, R.drawable.ic_bluetooth_black_48dp);
                    icon.setColorFilter(ContextCompat.getColor(NewTerminalActivity.this, R.color.neutral), PorterDuff.Mode.SRC_ATOP);
                    break;
                case TECIntent.CONNECTION_TYPE_TCPIP:
                    icon = ContextCompat.getDrawable(NewTerminalActivity.this, R.drawable.ic_signal_wifi_4_bar_black_48dp);
                    icon.setColorFilter(ContextCompat.getColor(NewTerminalActivity.this, R.color.neutral), PorterDuff.Mode.SRC_ATOP);
                    break;
                default:
                    icon = ContextCompat.getDrawable(NewTerminalActivity.this, R.drawable.ic_action_new);
            }
            holder.mIcon.setImageDrawable(icon);
            holder.mTitle.setText(mConnections.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return mConnections.size();
        }
    }
}
