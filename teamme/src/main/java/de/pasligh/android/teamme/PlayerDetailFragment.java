package de.pasligh.android.teamme;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.Player;
import de.pasligh.android.teamme.tools.GameRecordRV_Adapter;
import de.pasligh.android.teamme.tools.TeamReactor;

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
    private Player player;

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
            player = new Player(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.player_detail, container, false);
        rootView.findViewById(R.id.player_detail_FAB).setOnClickListener(this);
        rootView.findViewById(R.id.player_detail_rename_FAB).setOnClickListener(this);

        // Show the dummy content as text in a TextView.
        if (player != null) {
            ((TextView) rootView.findViewById(R.id.player_detail_nameTV)).setText(player.getName());
            GameRecordRV_Adapter adapter = new GameRecordRV_Adapter(getContext(), this, getFacade().getGamesByPlayer(player.getName()));
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            RecyclerView rv = ((RecyclerView) rootView.findViewById(R.id.player_detail_RV));
            rv.setLayoutManager(llm);
            rv.setAdapter(adapter);
        }

        return rootView;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.player_detail_FAB) {

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.playerDeleteDialog_question).replace("$1", player.getName()))
                    .setPositiveButton(R.string.delete,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    if (getFacade().deletePlayer(player.getName())) {
                                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.deleted) + ": " + player.getName(), Toast.LENGTH_SHORT).show();
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
                        if (!p.getName().equals(player.getName())) {
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
                                    getFacade().mergePlayer(player.getName(), players.get(which).getName());
                                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.deleted) + ": " + player.getName(), Toast.LENGTH_SHORT).show();
                                    restartListActivity();
                                }
                            });
                    builderSingle.show();

                }
            });
            // Create the AlertDialog object and return it
            builder.create().show();


        } else if (v.getId() == R.id.player_detail_rename_FAB) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),  R.style.AlertDialogCustom);
            builder.setTitle(R.string.playerinputtext);

            // Set up the input
            final EditText input = new EditText(getContext());
            input.setText(player.getName(), TextView.BufferType.EDITABLE);
            input.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            input.setSingleLine();
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newPlayerName = input.getText().toString().trim();
                    if (getFacade().persistPlayer(new Player(newPlayerName)) >= 0) {
                        getFacade().mergePlayer(player.getName(), newPlayerName);
                        restartListActivity();
                    }
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        } else {
            GameRecordRV_Adapter.GameRecordRV_Holder holder = (GameRecordRV_Adapter.GameRecordRV_Holder) v.getTag();
            String id = String.valueOf(holder.getId());
            Intent intent = new Intent(getActivity().getApplicationContext(), GameRecordDetailActivity.class);
            intent.putExtra(GameRecordDetailFragment.ARG_GAME_ID, id);
            startActivity(intent);
        }

    }

    private void restartListActivity() {
        Intent playerActivity = new Intent(getActivity().getApplicationContext(),
                PlayerListActivity.class);
        startActivity(playerActivity);
    }
}
