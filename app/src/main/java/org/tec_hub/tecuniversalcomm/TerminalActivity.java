package org.tec_hub.tecuniversalcomm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.connection.BluetoothConnectionService;
import org.tec_hub.tecuniversalcomm.connection.Connection;
import org.tec_hub.tecuniversalcomm.data.Packet;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Nick Mosher on 4/13/15.
 * Opens a terminal-like interface for sending data over an established connection.
 */
public class TerminalActivity extends ActionBarActivity {

    private BluetoothConnection mConnection;

    //View objects
    private ScrollView mTerminalScroll;
    private TextView mTerminalWindow;
    private EditText mTerminalInput;
    private Button mTerminalSend;
    private MenuItem mConnectedIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_terminal);

        if(!BluetoothConnectionService.isLaunched()) {
            startService(new Intent(this, BluetoothConnectionService.class));
        }

        Bundle extras = getIntent().getExtras();
        BluetoothConnection tempConnection = extras.getParcelable(TECIntent.BLUETOOTH_CONNECTION_DATA);
        mConnection = Preconditions.checkNotNull(tempConnection);

        getSupportActionBar().setTitle(mConnection.getName());
        getSupportActionBar().setSubtitle(mConnection.getAddress());
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
                    mTerminalInput.setText("{\"mData\":,\"mName\":\"\"}");
                }
            }
        });

        //Set a listener to accordingly change the status of the connection indicator
        mConnection.setOnStatusChangedListener(this, new Connection.OnStatusChangedListener() {
            @Override
            public void onConnect() {
                if (mConnectedIndicator != null) {
                    mConnectedIndicator.setIcon(getResources().getDrawable(R.drawable.ic_connected));
                }
            }

            @Override
            public void onDisconnect() {
                if (mConnectedIndicator != null) {
                    mConnectedIndicator.setIcon(getResources().getDrawable(R.drawable.ic_disconnected));
                }
            }
        });
        mConnection.connect(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TECIntent.ACTION_BLUETOOTH_UPDATE_INPUT);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(intent.getAction()) {
                    case TECIntent.ACTION_BLUETOOTH_UPDATE_INPUT:
                        receivedData(intent.getStringExtra(TECIntent.BLUETOOTH_RECEIVED_DATA));
                        break;
                    default:
                }
            }
        }, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mConnectedIndicator != null) {
            initIndicator();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_connection_terminal, menu);

        //Isolate the connected indicator and set it to a member variable for dynamic icon
        mConnectedIndicator = menu.findItem(R.id.connected_indicator);
        initIndicator();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.terminal_button_clear) {

            //Erase all text in the terminal window
            clearTerminal();
            return true;
        } else if(id == R.id.connected_indicator) {

            //Toggle between connected and disconnected
            if(mConnection.isConnected()) {
                mConnection.disconnect(this);
            } else {
                mConnection.connect(this);
            }
        } else if(id == R.id.Kudos) {
            Intent kudosIntent = new Intent(this, DriveKudosActivity.class);
            kudosIntent.putExtra(TECIntent.BLUETOOTH_CONNECTION_DATA, mConnection);
            startActivity(kudosIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initIndicator() {
        mConnectedIndicator.setIcon(getResources().getDrawable(
                (mConnection.isConnected() ? R.drawable.ic_connected : R.drawable.ic_disconnected)));
    }

    private boolean sendData(String data) {
        System.out.println("sendData(" + data + ")");

        if(mConnection.isConnected()) {
            OutputStream outputStream = mConnection.getOutputStream();
            if(outputStream != null) {
                try {
                    String packet = Packet.asString("Terminal", data).toJson();
                    outputStream.write(data.getBytes());
                    appendTerminal("Me: " + packet);
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
}