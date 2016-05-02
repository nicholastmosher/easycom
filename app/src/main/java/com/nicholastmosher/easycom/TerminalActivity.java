package com.nicholastmosher.easycom;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nicholastmosher.easycom.core.connection.BluetoothConnection;
import com.nicholastmosher.easycom.core.connection.Connection;
import com.nicholastmosher.easycom.core.connection.TcpIpConnection;

/**
 * Created by Nick Mosher on 10/19/15.
 * @author Nick Mosher, nicholastmosher@gmail.com, https://github.com/nicholastmosher
 */
public class TerminalActivity extends AppCompatActivity {

    private String mDisplayText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        //Initialize toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.terminal_toolbar);
        toolbar.setTitle(getString(R.string.terminal));
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.textLight));
        setSupportActionBar(toolbar);

        final EditText input = (EditText) findViewById(R.id.terminal_command);
        Button send = (Button) findViewById(R.id.terminal_send);
        final TextView display = (TextView) findViewById(R.id.terminal_text);

        send.setText("Send");
        display.setText(mDisplayText);

//        final Connection connection = new BluetoothConnection("TECBot", "00:13:12:25:72:72");
//        final Connection connection = new TcpIpConnection("Obsidyn", "obsidyn.student.rit.edu", 1111);
        final Connection connection = new TcpIpConnection("Obsidyn", "129.21.105.144", 1111);

        connection.connect();
        connection.addOnDataReceivedListener(new Connection.OnDataReceivedListener() {
            @Override
            public void onDataReceived(Connection connection, byte[] data) {
                String in = new String(data);
                System.out.println("Received " + in);
                mDisplayText += in;
                display.setText(mDisplayText);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSend = input.getText().toString();
                mDisplayText += ("\nAndroid> " + toSend + "\n");
                if(toSend != null && !toSend.equals("")) connection.send(toSend.getBytes());
            }
        });
    }
}
