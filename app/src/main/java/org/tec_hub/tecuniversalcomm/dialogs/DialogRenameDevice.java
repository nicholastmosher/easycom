package org.tec_hub.tecuniversalcomm.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import org.tec_hub.tecuniversalcomm.data.device.Device;

/**
 * Created by jswag on 9/27/15.
 */
public class DialogRenameDevice {

    private Context mContext;
    private Device mDevice;

    /**
     * Builds a new dialog.
     * @param context The context to build the dialog in.
     * @param device The device to rename.
     */
    public DialogRenameDevice(Context context, Device device) {
        mContext = context;
        mDevice = device;
    }

    /**
     * A helper to construct the dialog.
     * @param message The title of the Dialog.
     * @return Returns a constructed Dialog.
     */
    public AlertDialog.Builder dialogBuilder(String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setTitle(message);
        return dialogBuilder;
    }

    /**
     * Renames the Device to the value set by the user in the Dialog.
     */
    public  void rename() {

        final EditText newName = new EditText(mContext);
        newName.setHint("Enter Device Name:");
        final AlertDialog.Builder newNameDialog = dialogBuilder("Rename Device");

        newNameDialog.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!newName.getText().toString().equals("")) {
                    mDevice.setName(newName.getText().toString());
                }
            }
        });

        newNameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        newNameDialog.setView(newName);
        newNameDialog.show();
    }
}
