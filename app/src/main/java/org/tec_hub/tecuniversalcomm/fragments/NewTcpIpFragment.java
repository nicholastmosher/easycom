package org.tec_hub.tecuniversalcomm.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.tec_hub.tecuniversalcomm.R;

/**
 * Created by Nick Mosher on 12/4/15.
 * @author Nick Mosher
 */
public class NewTcpIpFragment extends Fragment {

    /**
     * Generates a view that will be used to create a new TCP/IP connection.
     * @param inflater An inflater for using xml layouts.
     * @param container A reference to the view's new parent.
     * @param savedInstanceState A bundle to save the instance's state.
     * @return A view for creating TCP/IP connections.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LinearLayout tcpipView = (LinearLayout) inflater.inflate(R.layout.fragment_new_tcpip, container, false);

        EditText ip = (EditText) tcpipView.findViewById(R.id.connection_new_tcpip_ip);

        EditText port = (EditText) tcpipView.findViewById(R.id.connection_new_tcpip_port);

        return tcpipView;
    }
}
