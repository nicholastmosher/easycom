package com.nicholastmosher.easycom;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nicholastmosher.easycom.core.connection.ConnectionService;

/**
 * Created by Nick Mosher on 10/16/15.
 * @author Nick Mosher, nicholastmosher@gmail.com, https://github.com/nicholastmosher
 */
public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the main toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.textLight));
        setSupportActionBar(toolbar);

        //Initialize the recycler.
        RecyclerView recycler = (RecyclerView) findViewById(R.id.main_recycler);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new RecyclerAdapter());

        ConnectionService.launch(getApplicationContext());
    }

    /**
     * Adapter that populates view objects for filling the main screen RecyclerView.
     */
    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        /**
         * Helper class that contains references to all needed view objects for one
         * item in the RecyclerView.
         */
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public LinearLayout mContainer;
            public ImageView mIcon;
            public TextView mTitle;
            public TextView mSubtitle;

            public ViewHolder(CardView card) {
                super(card);
                mContainer = (LinearLayout) card.findViewById(R.id.card_container);
                mIcon = (ImageView) card.findViewById(R.id.card_icon);
                mTitle = (TextView) card.findViewById(R.id.card_title);
                mSubtitle = (TextView) card.findViewById(R.id.card_subtitle);

                mContainer.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                //Perform check for casting to LinearLayout
                if (view instanceof LinearLayout) {
                    LinearLayout container = (LinearLayout) view;
                    String title = ((TextView) container.findViewById(R.id.card_title)).getText().toString();

                    //Launch Activities based on item click.
                    if (title.equals(getString(R.string.connections))) {
                        startActivity(new Intent(MainActivity.this, ConnectionActivity.class));
                    } else if (title.equals(getString(R.string.terminal))) {
                        startActivity(new Intent(MainActivity.this, TerminalActivity.class));
                    } else if (title.equals(getString(R.string.commands))) {
                        startActivity(new Intent(MainActivity.this, CommandActivity.class));
                    } else if (title.equals(getString(R.string.controls))) {
                        startActivity(new Intent(MainActivity.this, ControlActivity.class));
                    } else if (title.equals(getString(R.string.settings))) {
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    }
                }
            }
        }

        private Menu mMenu;

        public RecyclerAdapter() {
            mMenu = new MenuBuilder(MainActivity.this);
            new MenuInflater(MainActivity.this).inflate(R.menu.main, mMenu);
        }

        @Override
        public int getItemCount() {
            return mMenu.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.element_main_card, parent, false);
            return new ViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (position > mMenu.size())
                throw new IllegalArgumentException("Position out of bounds!");

            MenuItem item = mMenu.getItem(position);
            holder.mIcon.setImageDrawable(item.getIcon());
            holder.mTitle.setText(item.getTitle());
            if (item.hasSubMenu()) {
                MenuItem subMenuItem = item.getSubMenu().getItem();
                holder.mSubtitle.setText(subMenuItem.getTitle());
            } else {
                holder.mSubtitle.setText("");
            }
        }
    }
}
