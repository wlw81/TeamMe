package de.pasligh.android.teamme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.databinding.ActivityPlayerListBinding;
import de.pasligh.android.teamme.databinding.ActivityPlayerSelectionBinding;
import de.pasligh.android.teamme.objects.GameRecord;
import de.pasligh.android.teamme.objects.Player;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.objects.Score;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.PlayerSelectionRV_Adapter;
import de.pasligh.android.teamme.tools.PlayerSelectionRV_Interface;
import de.pasligh.android.teamme.tools.TeamReactor;

public class PlayerSelectionActivity extends AppCompatActivity implements View.OnClickListener, PlayerSelectionRV_Interface {

    private BackendFacade facade;
    PlayerSelectionRV_Adapter adapter;

    private int teamcount;
    private int playercount;
    private String sports;

    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getApplicationContext());
        }
        return facade;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_selection);
        ActivityPlayerSelectionBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_player_selection);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.playersAssignedFAB);
        fab.setOnClickListener(this);

        RecyclerView rv = (RecyclerView) findViewById(R.id.playerSelectionRV);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);

        List<String> list = new ArrayList<String>();
        list.add(getString(R.string.random));
        for (int i = 1; i <= getIntent().getIntExtra(Flags.TEAMCOUNT, -1); i++) {
            list.add(getString(R.string.team) + " " + i);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_dropdown_item, list);

        Map<String, Integer> mapStarsPerPlayer = new HashMap<String, Integer>();

        adapter = new PlayerSelectionRV_Adapter(getApplicationContext(), assemblePlayerAssignments(mapStarsPerPlayer), Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf"), dataAdapter, this, mapStarsPerPlayer);
        rv.setAdapter(adapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.gameCreatorTB);
        toolbar.setTitle(getString(R.string.title_activity_player_selection));
        toolbar.setLogo(R.drawable.actionbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(PlayerSelectionActivity.this);
            }
        });


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.setAssignments(adapter.getAssignments());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.player_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.PlayerSelectionAddContext:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.addPlayer));

                // Set up the input
                ArrayAdapter<String> playerAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line, getFacade()
                        .getPlayersAsStringArray());
                final AutoCompleteTextView input = new AutoCompleteTextView(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setAdapter(playerAdapter);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = input.getText().toString().trim();
                        if (!m_Text.isEmpty()) {
                            PlayerAssignment assignmentNew = new PlayerAssignment();
                            assignmentNew.setPlayer(new Player(m_Text));
                            try {
                                if (getFacade().persistPlayer(assignmentNew.getPlayer()) <= 0) {
                                    adapter.getAssignments().add(assignmentNew);
                                    adapter.notifyDataSetChanged();
                                    findViewById(R.id.playerSelectionBlankTV).setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                Log.d(Flags.LOGTAG, assignmentNew.getPlayer() + " already known.");
                            }

                        }
                    }
                });

                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                final AlertDialog alertToShow = builder.create();
                alertToShow.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                alertToShow.show();
                input.requestFocus();
                input.selectAll();

                break;
        }
        return true;
    }

    @NonNull
    private List<PlayerAssignment> assemblePlayerAssignments(Map<String, Integer> mapStarsPerPlayer) {
        Map<String, Integer> mapPointsPerPlayer = new HashMap<String, Integer>();

        List<PlayerAssignment> blankAssignments = new ArrayList<PlayerAssignment>();
        List<Player> allPlayers = getFacade().getPlayers();

        sports = getIntent().getStringExtra(Flags.SPORT);
        teamcount = getIntent().getIntExtra(Flags.TEAMCOUNT, -1);
        playercount = getIntent().getIntExtra(Flags.PLAYERCOUNT, -1);

        // if too many player are listed, we try to suggest only player for this sport type
        List<GameRecord> gameRecords = getFacade().getGames(sports);
        try {
            if (allPlayers.size() > Flags.MAXPLAYERS_PRESELECTION) {
                Set<Player> playersPerGame = new HashSet<Player>();
                for (GameRecord gr : gameRecords) {
                    for (PlayerAssignment assignment : gr.getAssignments()) {
                        playersPerGame.add(assignment.getPlayer());
                    }
                }

                if (!playersPerGame.isEmpty()) {
                    allPlayers.clear();
                    allPlayers.addAll(playersPerGame);
                }
            }
        } catch (Exception e) {
            Log.w(Flags.LOGTAG, "Could not optimize player list: " + e.getMessage());
        }

        Integer maxScore = null;
        Integer minScore = null;

        // let's figure out how the players performed
        for (GameRecord g : gameRecords) {
            List<PlayerAssignment> assignemntsForGame = getFacade().getAssignments(g.getId());

            for (Score s : getFacade().getScores(g.getId())) {
                for (PlayerAssignment assignment : assignemntsForGame) {
                    if (s.getTeamNr() == assignment.getTeam()) {
                        Integer pointsPerPlayer = mapPointsPerPlayer.get(assignment.getPlayer().getName());
                        int newScore = s.getScoreCount();
                        if (maxScore == null || newScore > maxScore) {
                            maxScore = newScore;
                        }
                        if (minScore == null || newScore < minScore) {
                            minScore = newScore;
                        }

                        if (null != pointsPerPlayer) {
                            if (newScore > pointsPerPlayer) {
                                mapPointsPerPlayer.put(assignment.getPlayer().getName(), (newScore + pointsPerPlayer) / 2);
                            }
                        } else {
                            mapPointsPerPlayer.put(assignment.getPlayer().getName(), newScore);
                        }
                    }
                }
            }
        }

        //now let's rate the players
        if (minScore != null && maxScore != null) {
            int oneStar = (maxScore - minScore) / Flags.MAXSTARS;
            for (String playerName : mapPointsPerPlayer.keySet()) {
                if (oneStar != 0) {
                    int scored = mapPointsPerPlayer.get(playerName);
                    scored -= minScore;
                    mapStarsPerPlayer.put(playerName, Math.min(Flags.MAXSTARS, scored / oneStar));
                } else {
                    mapStarsPerPlayer.put(playerName, Flags.MAXSTARS / 2);
                }
            }
        }

        for (Player p : allPlayers) {
            PlayerAssignment assignBlank = new PlayerAssignment();
            assignBlank.setPlayer(p);
            blankAssignments.add(assignBlank);
        }

        return blankAssignments;
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        Intent data = new Intent();
        for (PlayerAssignment p : adapter.getAssignmentsDone()) {
            data.putExtra(p.getPlayer().getName(), p);
        }

        if (getParent() == null) {
            setResult(Activity.RESULT_OK, data);
        } else {
            getParent().setResult(Activity.RESULT_OK, data);
        }
        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        PlayerSelectionRV_Adapter.PlayerViewHolder pvh = (PlayerSelectionRV_Adapter.PlayerViewHolder) buttonView.getTag();
        PlayerAssignment pa = adapter.getAssignments().get(pvh.getAdapterPosition());
        if (isChecked && !pa.isRevealed()) {
            adapter.getAssignments().get(pvh.getAdapterPosition()).setRevealed(isChecked);
            pvh.expandView();
        } else if (!isChecked && pa.isRevealed()) {
            adapter.getAssignments().get(pvh.getAdapterPosition()).setRevealed(isChecked);
            pvh.collapseView();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        ((Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE)).vibrate(400);
        PlayerSelectionRV_Adapter.PlayerViewHolder pvh = (PlayerSelectionRV_Adapter.PlayerViewHolder) v.getTag();
        int position = pvh.getAdapterPosition();
        final PlayerAssignment contact = adapter.getAssignments().get(position);
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(
                PlayerSelectionActivity.this);
        builder.setMessage(getString(R.string.playerDeleteDialog_question).replace("$1", contact.getPlayer().getName()))
                .setPositiveButton(R.string.delete,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                if (getFacade().deletePlayer(contact.getPlayer().getName())) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.deleted) + ": " + contact.getPlayer().getName(), Toast.LENGTH_SHORT).show();
                                    adapter.getAssignments().remove(contact);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }).setNeutralButton(R.string.merge, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(PlayerSelectionActivity.this);
                builderSingle.setTitle(getString(R.string.mergeTo));

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        PlayerSelectionActivity.this,
                        android.R.layout.simple_selectable_list_item);

                List<Player> allPlayers = getFacade().getPlayers();
                final List<Player> players = new ArrayList<Player>();

                for (Player p : allPlayers) {
                    if (!p.getName().equals(contact.getPlayer().getName())) {
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
                                getFacade().mergePlayer(contact.getPlayer().getName(), players.get(which).getName());
                                Toast.makeText(getApplicationContext(), getString(R.string.deleted) + ": " + contact.getPlayer().getName(), Toast.LENGTH_SHORT).show();
                                adapter.getAssignments().remove(contact);
                                Map<String, Integer> mapStarsPerPlayer = new HashMap<String, Integer>();
                                assemblePlayerAssignments(mapStarsPerPlayer);
                                adapter.getMapStarsPerPlayer().clear();
                                adapter.getMapStarsPerPlayer().putAll(mapStarsPerPlayer);
                                adapter.notifyDataSetChanged();
                            }
                        });
                builderSingle.show();

            }
        });
        // Create the AlertDialog object and return it
        builder.create().show();
        return false;
    }
}
