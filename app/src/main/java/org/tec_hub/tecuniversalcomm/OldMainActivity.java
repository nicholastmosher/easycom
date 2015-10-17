package org.tec_hub.tecuniversalcomm;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import org.tec_hub.tecuniversalcomm.data.StorageAdapter;
import org.tec_hub.tecuniversalcomm.data.connection.Connection;
import org.tec_hub.tecuniversalcomm.data.connection.ConnectionList;
import org.tec_hub.tecuniversalcomm.data.connection.ConnectionService;
import org.tec_hub.tecuniversalcomm.data.connection.intents.ConnectionIntent;
import org.tec_hub.tecuniversalcomm.fragments.CommandFragment;
import org.tec_hub.tecuniversalcomm.fragments.TerminalFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick Mosher on 9/30/15.
 */
public class OldMainActivity extends AppCompatActivity {

    private Connection mActiveConnection;
    private StorageAdapter.DataAdapter<ConnectionList> mConnectionAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.old_activity_main);

        ConnectionService.launch(this);

        //Initialize Storage Adapter
        mConnectionAdapter = StorageAdapter.getInstance(this)
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
            mConnectionAdapter.read(new StorageAdapter.DataEventListener<ConnectionList>() {
                @Override
                public void onDataRead(ConnectionList data) {
                    if (data != null) {
                        mConnections = data;
                    }
                }
            });
        }

        @Override
        public ConnectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout listItem = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_connection, parent, false);

            return new ConnectionViewHolder(listItem);
        }

        @Override
        public void onBindViewHolder(ConnectionViewHolder holder, int position) {

            //Set icon.
            Drawable icon;
            switch(mConnections.get(position).getConnectionType()) {
                case ConnectionIntent.CONNECTION_TYPE_BLUETOOTH:
                    icon = ContextCompat.getDrawable(OldMainActivity.this, R.drawable.ic_bluetooth_black_48dp);
                    icon.setColorFilter(ContextCompat.getColor(OldMainActivity.this, R.color.neutral), PorterDuff.Mode.SRC_ATOP);
                    break;
                case ConnectionIntent.CONNECTION_TYPE_TCPIP:
                    icon = ContextCompat.getDrawable(OldMainActivity.this, R.drawable.ic_wifi_black_48dp);
                    icon.setColorFilter(ContextCompat.getColor(OldMainActivity.this, R.color.neutral), PorterDuff.Mode.SRC_ATOP);
                    break;
                default:
                    icon = ContextCompat.getDrawable(OldMainActivity.this, R.drawable.ic_action_new);
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

    /**
     * Handles exchanging of fragment views in the ViewPager.
     */
    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<>();
        private List<String> names = new ArrayList<>();
        ViewPager mViewPager;

        public ViewPagerAdapter(ViewPager pager, FragmentManager manager) {
            super(manager);
            mViewPager = pager;

            fragments.add(new TerminalFragment());
            names.add("Terminal");
            fragments.add(new CommandFragment());
            names.add("Commands");
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
            return names.get(position);
        }
    }
}
