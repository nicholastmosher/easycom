package org.tec_hub.tecuniversalcomm.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import org.tec_hub.tecuniversalcomm.data.connection.Connection;
import org.tec_hub.tecuniversalcomm.data.device.Device;

/**
 * Created by jswag on 10/2/15.
 */

/**
 * Dialog that renames connections
 * Almost exactly the same as DialogRenameDevice class
 */
public class DialogRenameConnection {
    private Context mContext;
    private Connection mConnection;
    private Device mDevice;
    public DialogRenameConnection(Context mContext, Connection mConnection, Device mDevice){
        this.mConnection = mConnection;
        this.mContext = mContext;
        this.mDevice = mDevice;
    }
    public AlertDialog.Builder connectionBuilder(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext).setTitle(message);
        return builder;
    }
    public void rename(){
        final EditText renameText = new EditText(mContext);
        renameText.setHint("Enter new name");
        AlertDialog.Builder newConnectionDialog = connectionBuilder("Rename");
        newConnectionDialog.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!renameText.toString().equals("")) {
                    mConnection.setName(renameText.getText().toString());
                    mDevice.notifyObservers(Device.Status.ConnectionNameUpdated);
                }
            }
        });
        newConnectionDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        newConnectionDialog.setView(renameText);
        newConnectionDialog.show();

    }
}
