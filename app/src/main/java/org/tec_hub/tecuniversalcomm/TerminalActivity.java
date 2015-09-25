package org.tec_hub.tecuniversalcomm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.data.connection.ConnectionService;
import org.tec_hub.tecuniversalcomm.data.connection.Connection;
import org.tec_hub.tecuniversalcomm.data.connection.TcpIpConnection;
import org.tec_hub.tecuniversalcomm.intents.BluetoothSendIntent;
import org.tec_hub.tecuniversalcomm.intents.TECIntent;
import org.tec_hub.tecuniversalcomm.intents.TcpIpSendIntent;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Nick Mosher on 4/13/15.
 * Opens a terminal-like interface for sending data over an established connection.
 */
public class TerminalActivity extends AppCompatActivity {

    private Connection mConnection;
    private int mConnectedIcon;
    private int mDisconnectedIcon;

    //View objects
    private ScrollView mTerminalScroll;
    private TextView mTerminalWindow;
    private EditText mTerminalInput;
    private Button mTerminalSend;
    private MenuItem mConnectedIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

        ConnectionService.launch(this);
        Intent intent = getIntent();
        switch(intent.getStringExtra(TECIntent.CONNECTION_TYPE)) {
            case TECIntent.CONNECTION_TYPE_BLUETOOTH:

                mConnection = intent.getParcelableExtra(TECIntent.BLUETOOTH_CONNECTION_DATA);
                toolbar.setSubtitle(((BluetoothConnection) mConnection).getAddress());
                mConnectedIcon = R.drawable.ic_bluetooth_connected_black_48dp;
                mDisconnectedIcon = R.drawable.ic_bluetooth_disabled_black_48dp;
                break;
            case TECIntent.CONNECTION_TYPE_TCPIP:

                mConnection = intent.getParcelableExtra(TECIntent.TCPIP_CONNECTION_DATA);
                toolbar.setSubtitle(((TcpIpConnection) mConnection).getServerIp() + ":" + ((TcpIpConnection) mConnection).getServerPort());
                mConnectedIcon = R.drawable.ic_signal_wifi_4_bar_black_48dp;
                mDisconnectedIcon = R.drawable.ic_signal_wifi_off_black_48dp;
                break;
            default:
        }

        toolbar.setTitle(mConnection.getName());
        setSupportActionBar(toolbar);

        mTerminalScroll = (ScrollView) findViewById(R.id.terminal_scrollview);
        mTerminalWindow = (TextView) findViewById(R.id.terminal_window);
        mTerminalInput = (EditText) findViewById(R.id.terminal_input_text);
        mTerminalSend = (Button) findViewById(R.id.terminal_input_button);

        mTerminalWindow.setTextColor(ContextCompat.getColor(this, R.color.material_grey_900));

        mTerminalInput.setHint("Type data to send:");
        mTerminalSend.setText("Send");
        mTerminalSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = mTerminalInput.getText().toString();
                if (!data.equals("")) {
                    sendData(data);
                    mTerminalInput.setText("");
                    //mTerminalInput.setText("{\"mData\":,\"mName\":\"\"}");
                }
            }
        });

        mTerminalSend.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));

        //Set a listener to accordingly change the status of the connection indicator
        mConnection.addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                if(data instanceof Connection.Cues) {
                    Connection.Cues cue = (Connection.Cues) data;
                    switch(cue) {
                        case Connected:
                            if (mConnectedIndicator != null) {
                                mConnectedIndicator.setIcon(ContextCompat.getDrawable(TerminalActivity.this, mConnectedIcon));
                            }
                            break;
                        case Disconnected:
                            if (mConnectedIndicator != null) {
                                mConnectedIndicator.setIcon(ContextCompat.getDrawable(TerminalActivity.this, mDisconnectedIcon));
                            }
                            break;
                        case ConnectFailed:
                            if(mConnectedIndicator != null) {
                                mConnectedIndicator.setIcon(ContextCompat.getDrawable(TerminalActivity.this, mDisconnectedIcon));
                            }
                            break;
                        default:
                    }
                }
            }
        });
        mConnection.connect(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TECIntent.ACTION_RECEIVED_DATA);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(intent.getAction()) {
                    case TECIntent.ACTION_RECEIVED_DATA:
                        receivedData(intent.getStringExtra(TECIntent.RECEIVED_DATA));
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

    /**
     * Refreshes the connection indicator at the top of the screen when the status changes.
     */
    private void updateIndicator() {
        mConnectedIndicator.setIcon(ContextCompat.getDrawable(TerminalActivity.this,
                (mConnection.isConnected() ? mConnectedIcon : mDisconnectedIcon)));
    }

    private boolean sendData(String data) {
        System.out.println("sendData(" + data + ")");

        Intent sendIntent = null;
        if(mConnection instanceof BluetoothConnection) {
            sendIntent = new BluetoothSendIntent(this, (BluetoothConnection) mConnection, data);

        } else if(mConnection instanceof TcpIpConnection) {
            sendIntent = new TcpIpSendIntent(this, (TcpIpConnection) mConnection, data);

        }

        if(sendIntent != null) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(sendIntent);
        }
        return false;
    }

    /**
     * Invoked whenever we receive valid data from a connection.
     * @param data The data we received from the connection.
     */
    private void receivedData(String data) {
        appendTerminal(mConnection.getName() + ": " + data);
    }

    /**
     * Adds text to the TextView display on the screen.
     * @param data The text to append.
     */
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