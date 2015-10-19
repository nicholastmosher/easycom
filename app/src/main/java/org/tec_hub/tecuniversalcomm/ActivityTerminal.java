package org.tec_hub.tecuniversalcomm;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by Nick Mosher on 10/19/15.
 * @author Nick Mosher, nicholastmosher@gmail.com, https://github.com/nicholastmosher
 */
public class ActivityTerminal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        //Initialize toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.terminal_toolbar);
        toolbar.setTitle(getString(R.string.terminal));
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.textLight));
        setSupportActionBar(toolbar);
    }
}
