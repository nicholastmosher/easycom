package org.tec_hub.tecuniversalcomm.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.tec_hub.tecuniversalcomm.R;

/**
 * Created by nick on 10/10/15.
 */
public class CommandFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View commandView = inflater.inflate(R.layout.fragment_command, container, false);

        //Initialize Recycler
        RecyclerView recyclerView = (RecyclerView) commandView.findViewById(R.id.command_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new CommandAdapter());
        return commandView;
    }

    public class CommandAdapter extends RecyclerView.Adapter<CommandAdapter.CommandViewHolder> {

        public class CommandViewHolder extends RecyclerView.ViewHolder {

            public LinearLayout mCommandLayout;

            public CommandViewHolder(LinearLayout container) {
                super(container);
                mCommandLayout = container;
            }
        }

        @Override
        public CommandViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CommandViewHolder(new LinearLayout(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(CommandViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }
}