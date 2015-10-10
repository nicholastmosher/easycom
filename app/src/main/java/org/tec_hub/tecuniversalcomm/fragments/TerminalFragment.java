package org.tec_hub.tecuniversalcomm.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.tec_hub.tecuniversalcomm.R;
import org.tec_hub.tecuniversalcomm.data.inputs.InputHex;

/**
 * Created by Nick Mosher on 10/10/15.
 */
public class TerminalFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Inflate root views
        View terminalView = inflater.inflate(R.layout.fragment_terminal, container, false);

        ((FrameLayout) terminalView.findViewById(R.id.input)).addView(new HexFragment().onCreateView(inflater, container, savedInstanceState));

        return terminalView;
    }

    public static class HexFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            InputHex inputHex = (InputHex) inflater.inflate(R.layout.input_hex, container, false);
            return inputHex;
        }
    }

    public static class BinaryFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    public static class StringFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }
}