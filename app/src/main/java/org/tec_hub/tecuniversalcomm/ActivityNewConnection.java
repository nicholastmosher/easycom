package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Nick Mosher on 11/2/15.
 * @author Nick Mosher, nicholastmosher@gmail.com, github.com/nicholastmosher
 */
public class ActivityNewConnection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_connection);

        Toolbar toolbar = (Toolbar) findViewById(R.id.new_connection_toolbar);
        toolbar.setTitle(getString(R.string.new_connection_activity_title));
        setSupportActionBar(toolbar);

        Spinner connectionSpinner = (Spinner) findViewById(R.id.new_connection_selector);
        connectionSpinner.setAdapter(new ConnectionSelectorAdapter(this));
    }

    public static class ConnectionSelectorAdapter extends BaseAdapter {

        private Menu mConnectionsMenu;

        public ConnectionSelectorAdapter(Context context) {
            mConnectionsMenu = new MenuBuilder(context);
            new MenuInflater(context).inflate(R.menu.connections, mConnectionsMenu);
        }

        @Override
        public int getCount() {
            return mConnectionsMenu.size();
        }

        @Override
        public Object getItem(int position) {
            return mConnectionsMenu.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            MenuItem menuItem = mConnectionsMenu.getItem(position);
            LinearLayout connectionView;
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                connectionView = (LinearLayout) inflater.inflate(R.layout.element_connection, null, false);
            } else {
                connectionView = (LinearLayout) convertView;
            }

            ((ImageView) connectionView.findViewById(R.id.connection_icon)).setImageDrawable(menuItem.getIcon());
            ((TextView) connectionView.findViewById(R.id.connection_title)).setText(menuItem.getTitle());

            return connectionView;
        }
    }
}
