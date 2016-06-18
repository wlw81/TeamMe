package de.pasligh.android.teamme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.GameRecord;
import de.pasligh.android.teamme.objects.Player;
import de.pasligh.android.teamme.tools.GameRecordRV_Adapter;

/**
 * A fragment representing a single Player detail screen.
 * This fragment is either contained in a {@link PlayerListActivity}
 * in two-pane mode (on tablets) or a {@link PlayerDetailActivity}
 * on handsets.
 */
public class PlayerDetailFragment extends android.support.v4.app.Fragment implements View.OnClickListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Player mItem;

    private BackendFacade facade;


    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getActivity().getApplicationContext());
        }
        return facade;
    }


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayerDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = new Player(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.player_detail, container, false);
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Roboto-Thin.ttf");
        TextView tv = ((TextView) rootView.findViewById(R.id.player_detail_nameTV));
        tv.setTypeface(tf);
        rootView.findViewById(R.id.player_detail_FAB).setOnClickListener(this);


        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            tv.setText(mItem.getName());
            GameRecordRV_Adapter adapter = new GameRecordRV_Adapter(getContext(), null, getFacade().getGamesByPlayer(mItem.getName()));
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            RecyclerView rv = ((RecyclerView) rootView.findViewById(R.id.player_detail_RV));
            rv.setLayoutManager(llm);
            rv.setAdapter(adapter);
        }

        return rootView;
    }

    @Override
    public void onClick(View v) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.playerDeleteDialog_question).replace("$1", mItem.getName()))
                .setPositiveButton(R.string.delete,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                if (getFacade().deletePlayer(mItem.getName())) {
                                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.deleted) + ": " + mItem.getName(), Toast.LENGTH_SHORT).show();
                                    restartListActivity();
                                }
                            }
                        }).setNeutralButton(R.string.merge, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                builderSingle.setTitle(getString(R.string.mergeTo));

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        getActivity(),
                        android.R.layout.simple_selectable_list_item);

                List<Player> allPlayers = getFacade().getPlayers();
                final List<Player> players = new ArrayList<Player>();

                for (Player p : allPlayers) {
                    if (!p.getName().equals(mItem.getName())) {
                        arrayAdapter.add(p.getName());
                        players.add(p);
                    }
                }

                builderSingle.setNegativeButton(
                        getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builderSingle.setAdapter(
                        arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getFacade().mergePlayer(mItem.getName(), players.get(which).getName());
                                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.deleted) + ": " + mItem.getName(), Toast.LENGTH_SHORT).show();
                                restartListActivity();
                            }
                        });
                builderSingle.show();

            }
        });
        // Create the AlertDialog object and return it
        builder.create().show();
    }

    private void restartListActivity() {
        Intent playerActivity = new Intent(getActivity().getApplicationContext(),
                PlayerListActivity.class);
        startActivity(playerActivity);
    }
}
