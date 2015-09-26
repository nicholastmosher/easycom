package org.tec_hub.tecuniversalcomm.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.tec_hub.tecuniversalcomm.R;
import org.tec_hub.tecuniversalcomm.data.connection.TcpIpConnection;
import org.tec_hub.tecuniversalcomm.intents.TcpIpDiscoveredIntent;

/**
 * Created by Nick Mosher on 9/24/15.
 */
public class DialogNewTcpIp {

    private static LinearLayout address;

    public static AlertDialog build(final Context context, final Class<?> cls) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Create New Device");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        address = (LinearLayout) inflater.inflate(R.layout.dialog_new_tcpip, null, false);
        builder.setView(address);

        final EditText name = (EditText) address.findViewById(R.id.name);
        final EditText ip = (EditText) address.findViewById(R.id.ip);
        final EditText port = (EditText) address.findViewById(R.id.port);

        ip.setRawInputType(Configuration.KEYBOARD_12KEY);
        port.setRawInputType(Configuration.KEYBOARD_12KEY);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!name.toString().equals("") && !ip.toString().equals("")) {
                    try {
                        int portNum = Integer.parseInt(port.getText().toString());
                        TcpIpConnection connection = new TcpIpConnection(name.getText().toString(), ip.getText().toString(), portNum);
                        TcpIpDiscoveredIntent intent = new TcpIpDiscoveredIntent(context, cls, connection);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    } catch(NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        ip.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.disabled), PorterDuff.Mode.DST_ATOP);

        return builder.create();
    }
}
