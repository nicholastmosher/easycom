package org.tec_hub.tecuniversalcomm;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
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

import org.tec_hub.tecuniversalcomm.data.NewStorageAdapter;
import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.data.connection.Connection;
import org.tec_hub.tecuniversalcomm.data.connection.ConnectionList;
import org.tec_hub.tecuniversalcomm.data.connection.TcpIpConnection;
import org.tec_hub.tecuniversalcomm.intents.TECIntent;

import java.util.ArrayList;

/**
 * Created by Nick Mosher on 9/30/15.
 */
public class MainActivity extends AppCompatActivity {

    private Connection mActiveConnection;
    private NewStorageAdapter.DataAdapter<ConnectionList> mConnectionAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize Storage Adapter
        mConnectionAdapter = NewStorageAdapter.getInstance(this)
                .getDataAdapter(ConnectionList.class, "Connections", ConnectionList.getTypeAdapter());

        //Initialize Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.terminal_toolbar);
        toolbar.setTitle("Title");

        //Initialize Recycler
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.drawer_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new DrawerLayoutAdapter());

        //Initialize DrawerLayout
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

        //Initialize Tabbed Views
        ViewPager viewPager = (ViewPager) findViewById(R.id.main_pager);
        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(viewPager, getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * Manages loading views into the RecyclerView.  All Connections
     * are handled through this adapter since this is where they are
     * displayed.
     */
    public class DrawerLayoutAdapter extends RecyclerView.Adapter<DrawerLayoutAdapter.ConnectionViewHolder> {

        private ConnectionList mConnections = new ConnectionList();

        /**
         * Acts as a container for each view in the RecyclerView.
         */
        public class ConnectionViewHolder extends RecyclerView.ViewHolder {

            public LinearLayout mContainer;
            public ImageView mIcon;
            public TextView mTitle;

            public ConnectionViewHolder(LinearLayout container) {
                super(container);
                mContainer = container;
                mIcon = (ImageView) mContainer.findViewById(R.id.list_icon);
                mTitle = (TextView) mContainer.findViewById(R.id.list_title);
            }
        }

        /**
         * Constructs a new DrawerLayoutAdapter using an initial list
         * of Connections.
         */
        public DrawerLayoutAdapter() {
            mConnectionAdapter.read(new NewStorageAdapter.DataEventListener<ConnectionList>() {
                @Override
                public void onDataRead(ConnectionList data) {
                    if(data != null) {
                        mConnections = data;
                    }
                }
            });
        }

        @Override
        public ConnectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout listItem = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.new_list_item_connection, parent, false);

            return new ConnectionViewHolder(listItem);
        }

        @Override
        public void onBindViewHolder(ConnectionViewHolder holder, int position) {

            //Set icon.
            Drawable icon;
            switch(mConnections.get(position).getConnectionType()) {
                case TECIntent.CONNECTION_TYPE_BLUETOOTH:
                    icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_bluetooth_black_48dp);
                    icon.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.neutral), PorterDuff.Mode.SRC_ATOP);
                    break;
                case TECIntent.CONNECTION_TYPE_TCPIP:
                    icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_signal_wifi_4_bar_black_48dp);
                    icon.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.neutral), PorterDuff.Mode.SRC_ATOP);
                    break;
                default:
                    icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_action_new);
            }
            holder.mIcon.setImageDrawable(icon);
            holder.mTitle.setText(mConnections.get(position).getName());
        }

        @Override
        public int getItemCount() {
            if(mConnections != null) {
                return mConnections.size();
            }
            return 0;
        }
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<CustomFragment> fragments = new ArrayList<>();
        ViewPager mViewPager;

        public ViewPagerAdapter(ViewPager pager, FragmentManager manager) {
            super(manager);
            mViewPager = pager;

            fragments.add(new TerminalFragment("Terminal"));
            fragments.add(new CommandFragment("Commands"));
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments.get(position).getName();
        }
    }

    public static class CustomFragment extends Fragment {

        private String mName;

        public CustomFragment(String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }
    }

    public static class TerminalFragment extends CustomFragment {

        public TerminalFragment(String name) {
            super(name);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View terminalView = inflater.inflate(R.layout.fragment_terminal, container, false);
            return terminalView;
        }
    }

    public class CommandFragment extends CustomFragment {

        public CommandFragment(String name) {
            super(name);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            View commandView = inflater.inflate(R.layout.fragment_command, container, false);

            //Initialize Recycler
            RecyclerView recyclerView = (RecyclerView) commandView.findViewById(R.id.command_recycler);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            recyclerView.setAdapter(new CommandAdapter());
            return commandView;
        }

        public class CommandAdapter extends RecyclerView.Adapter<CommandAdapter.CommandViewHolder> {

            public class CommandViewHolder extends RecyclerView.ViewHolder {

                public LinearLayout mCommandLayout;

                public CommandViewHolder(LinearLayout container) {
                    super(container);
                    mCommandLayout = container;
                }
            }

            @Override
            public CommandViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new CommandViewHolder(new LinearLayout(MainActivity.this));
            }

            @Override
            public void onBindViewHolder(CommandViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 0;
            }
        }
    }
}
