package org.tec_hub.tecuniversalcomm.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by jswag on 9/27/15.
 */
public class DeviceRenameDialog {
    private static Context context2;
    public DeviceRenameDialog(Context context){
        context2 = context;
    }
    public static AlertDialog.Builder dialogBuilder(String message){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context2);
        dialogBuilder.setTitle(message);
        return dialogBuilder;
    }
    public static EditText askForName(){
        final EditText deviceName = new EditText(context2);
        deviceName.setHint("Enter Device Name:");
        return deviceName;
    }
    public static void rename(TextView name){
        final EditText newName = askForName();
        final TextView nameView = name;
        final AlertDialog.Builder newNameDialog = dialogBuilder("Rename Device");
        newNameDialog.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!newName.getText().toString().equals(""));
                nameView.setText(newName.getText());
            }
        });
        newNameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        newNameDialog.setView(newName);
        newNameDialog.show();
    }
}
