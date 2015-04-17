package org.tec_hub.tecuniversalcomm;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.tec_hub.tecuniversalcomm.Connection.BluetoothConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Nick Mosher on 4/13/15.
 * Opens a terminal-like interface for sending data over an established connection.
 */
public class ConnectionTerminalActivity extends ActionBarActivity {

    public static final String CONNECTION_DATA = "connection_data";

    private BluetoothConnection mConnection;

    //View objects
    private ScrollView mTerminalScroll;
    private TextView mTerminalWindow;
    private EditText mTerminalInput;
    private Button mTerminalSend;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_terminal);

        Bundle extras = getIntent().getExtras();
        mConnection = extras.getParcelable(CONNECTION_DATA);

        getSupportActionBar().setTitle(mConnection.getName() + " - " + mConnection.getAddress());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTerminalScroll = (ScrollView) findViewById(R.id.terminal_scrollview);
        mTerminalWindow = (TextView) findViewById(R.id.terminal_window);
        mTerminalInput = (EditText) findViewById(R.id.terminal_input_text);
        mTerminalSend = (Button) findViewById(R.id.terminal_input_button);

        mTerminalInput.setHint("Type data to send:");
        mTerminalSend.setText("Send");
        mTerminalSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = mTerminalInput.getText().toString();
                if(data != null && !data.equals("")) {
                    sendData(data);
                    mTerminalInput.setText("");
                }
            }
        });

        mConnection.connect();
        new ReceiveInputTask().execute("");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mConnection.disconnect();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_connection_terminal, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.terminal_button_clear) {
            clearTerminal();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean sendData(String data) {
        System.out.println("sendData(" + data + ")");
        if(mConnection.isConnected()) {
            OutputStream outputStream = mConnection.getOutputStream();
            if(outputStream != null) {
                try {
                    outputStream.write(data.getBytes());
                    appendTerminal("Me: " + data);
                    return true;
                } catch(IOException e) {
                    e.printStackTrace();
                    appendTerminal("Error sending: " + data);
                    return false;
                }
            }
            return false;
        } else {
            appendTerminal("Error sending: " + data);
        }
        return false;
    }

    private void receivedData(String data) {
        appendTerminal(mConnection.getName() + ": " + data);
    }

    private void appendTerminal(String data) {
        System.out.println("appendTerminal(" + data + ")");
        if(data != null) {
            mTerminalWindow.setText(mTerminalWindow.getText() + data + "\n");
            mTerminalScroll.fullScroll(View.FOCUS_DOWN); //Make scrollview scroll down
        }
    }

    private void clearTerminal() {
        mTerminalWindow.setText("");
    }

    private class ReceiveInputTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            while(mConnection.isConnected()) {
                byte[] buffer = new byte[256];
                int bytes = 0;
                try {
                    InputStream input = mConnection.getInputStream();
                    bytes = input.read(buffer);
                } catch(IOException e) {
                    e.printStackTrace();
                    return null;
                }
                String message = new String(buffer, 0, bytes);
                System.out.println(message);
                publishProgress(message);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            receivedData(values[0]);
        }
    }
}