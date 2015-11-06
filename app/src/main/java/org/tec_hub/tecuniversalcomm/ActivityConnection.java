package org.tec_hub.tecuniversalcomm;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Nick Mosher on 10/19/15.
 * @author Nick Mosher, nicholastmosher@gmail.com, https://github.com/nicholastmosher
 */
public class ActivityConnection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        //Initialize toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.connection_toolbar);
        toolbar.setTitle(getString(R.string.connections));
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.textLight));
        setSupportActionBar(toolbar);

        FloatingActionButton connectionFab = (FloatingActionButton) findViewById(R.id.connection_fab);
        connectionFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityConnection.this, ActivityNewConnection.class));
            }
        });
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public LinearLayout mContainer;
            public ImageView mIcon;
            public TextView mTitle;
            public TextView mSubtitle;
            public ViewHolder(View view) {
                super(view);
                mContainer = (LinearLayout) view.findViewById(R.id.connection_container);
                mIcon = (ImageView) view.findViewById(R.id.connection_icon);
                mTitle = (TextView) view.findViewById(R.id.connection_title);
                mSubtitle = (TextView) view.findViewById(R.id.connection_subtitle);

                mContainer.setOnClickListener(ViewHolder.this);
            }

            @Override
            public void onClick(View v) {

            }
        }

        @Override
        public int getItemCount() {
            return 0;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }
    }
}
