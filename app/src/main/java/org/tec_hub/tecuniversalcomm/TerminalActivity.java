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

import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnectionService;
import org.tec_hub.tecuniversalcomm.data.connection.Connection;
import org.tec_hub.tecuniversalcomm.intents.TECIntent;

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
        mConnection.putOnStatusChangedListener(this, new Connection.OnStatusChangedListener() {
            @Override
            public void onConnect() {
                System.out.println("TerminalActivity -> onConnect");
                if (mConnectedIndicator != null) {
                    mConnectedIndicator.setIcon(getResources().getDrawable(R.drawable.ic_connected));
                }
            }

            @Override
            public void onDisconnect() {
                System.out.println("TerminalActivity -> onDisconnect");
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
        updateIndicator();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            //Clicked the clear button
            case R.id.terminal_button_clear:
                //Erase all text in the terminal window
                clearTerminal();
                return true;

            //Clicked the connection indicator
            case R.id.connected_indicator:
                //Toggle between connected and disconnected
                if(mConnection.isConnected()) {
                    mConnection.disconnect(this);
                } else {
                    mConnection.connect(this);
                }
                return true;

            //Pressed Kudos button
            case R.id.Kudos:
                Intent kudosIntent = new Intent(this, DriveKudosActivity.class);
                kudosIntent.putExtra(TECIntent.BLUETOOTH_CONNECTION_DATA, mConnection);
                startActivity(kudosIntent);
                return true;

            //If the up button is pressed, act like the back button
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateIndicator() {
        if(mConnection.isConnected()) {
            mConnectedIndicator.setIcon(getResources().getDrawable(R.drawable.ic_connected));
        } else {
            mConnectedIndicator.setIcon(getResources().getDrawable(R.drawable.ic_disconnected));
        }
    }

    private boolean sendData(String data) {
        System.out.println("sendData(" + data + ")");

        Intent sendDataIntent = new Intent(this, BluetoothConnectionService.class);
        sendDataIntent.setAction(TECIntent.ACTION_BLUETOOTH_SEND_DATA);
        sendDataIntent.putExtra(TECIntent.BLUETOOTH_CONNECTION_DATA, mConnection);
        sendDataIntent.putExtra(TECIntent.BLUETOOTH_SEND_DATA, data);

        LocalBroadcastManager.getInstance(this).sendBroadcast(sendDataIntent);
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