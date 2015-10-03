package org.tec_hub.tecuniversalcomm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Nick Mosher on 9/30/15.
 */
public class NewTerminalActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_terminal);

        Toolbar toolbar = (Toolbar) findViewById(R.id.terminal_toolbar);
        toolbar.setTitle("New Terminal Toolbar");
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.terminal_recycler_drawer);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        RecyclerView.Adapter adapter = new TerminalDrawerAdapter();
        recyclerView.setAdapter(adapter);
    }

    public class TerminalDrawerAdapter extends RecyclerView.Adapter<TerminalDrawerAdapter.DrawerViewHolder> {

        public class DrawerViewHolder extends RecyclerView.ViewHolder {

            public LinearLayout mConnectionItem;
            public DrawerViewHolder(LinearLayout connectionItem) {
                super(connectionItem);
                mConnectionItem = connectionItem;
            }
        }

        public TerminalDrawerAdapter() {

        }

        @Override
        public DrawerViewHolder onCreateViewHolder(ViewGroup parent, int i) {

            LinearLayout connectionListItem = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_connection, parent, false);

            return new DrawerViewHolder(connectionListItem);
        }

        @Override
        public void onBindViewHolder(DrawerViewHolder drawerViewHolder, int i) {
            ((TextView) drawerViewHolder.mConnectionItem.findViewById(R.id.connection_name)).setText("Connection Name!");
        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }
}
