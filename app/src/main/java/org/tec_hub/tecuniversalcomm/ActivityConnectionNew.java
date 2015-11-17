package org.tec_hub.tecuniversalcomm;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Nick Mosher on 11/2/15.
 * @author Nick Mosher, nicholastmosher@gmail.com, github.com/nicholastmosher
 */
public class ActivityConnectionNew extends AppCompatActivity {

    private ConnectionSelectorAdapter mConnectionSelector;
    private FrameLayout mFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_new);

        //Initialize the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.new_connection_toolbar);
        toolbar.setTitle(getString(R.string.new_connection_activity_title));
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.textLight));
        setSupportActionBar(toolbar);

        //Initialize the frame layout where specific connection details go.
        mFrameLayout = (FrameLayout) findViewById(R.id.new_connection_unique_details);

        //Initialize the connection selection spinner.
        Spinner connectionSpinner = (Spinner) findViewById(R.id.new_connection_selector);
        connectionSpinner.setAdapter(mConnectionSelector = new ConnectionSelectorAdapter(this));
        connectionSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    /**
     * Serves data to the on-screen Spinner that is used to choose the type of Connection to
     * establish.
     */
    public static class ConnectionSelectorAdapter extends BaseAdapter {

        private Menu mConnectionsMenu;

        public ConnectionSelectorAdapter(Context context) {
            mConnectionsMenu = new MenuBuilder(context);
            new MenuInflater(context).inflate(R.menu.connections, mConnectionsMenu);
        }

        /**
         * Returns the number of options for this Spinner.
         * @return The number of options for this Spinner.
         */
        @Override
        public int getCount() {
            return mConnectionsMenu.size();
        }

        /**
         * Returns the data item at the given position.
         * @param position The position to retrieve data.
         * @return The data item at the given position.
         */
        @Override
        public Object getItem(int position) {
            return mConnectionsMenu.getItem(position);
        }

        /**
         * Returns a unique id of the data at the given position.
         * @param position The position to retrieve an ID for.
         * @return A unique id of the data at the given position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Returns a view representation of the data at the given position.
         * @param position The position to generate a view for.
         * @param convertView An existing but outdated view to repopulate.
         * @param parent The parent of the view to generate.
         * @return A view representation of the data at the given position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            MenuItem menuItem = mConnectionsMenu.getItem(position);
            LinearLayout connectionView;
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                connectionView = (LinearLayout) inflater.inflate(R.layout.element_connection, parent, false);
            } else {
                connectionView = (LinearLayout) convertView;
            }

            ((ImageView) connectionView.findViewById(R.id.connection_icon)).setImageDrawable(menuItem.getIcon());
            ((TextView) connectionView.findViewById(R.id.connection_title)).setText(menuItem.getTitle());

            return connectionView;
        }
    }
}
