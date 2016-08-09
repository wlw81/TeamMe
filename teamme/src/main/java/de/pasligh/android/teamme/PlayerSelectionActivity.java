package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.pasligh.android.teamme.backend.BackendFacade;
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

        int overallScore = 0;

        // let's see how the players are
        for (GameRecord g : gameRecords) {
            List<PlayerAssignment> assignemntsForGame = getFacade().getAssignments(g.getId());

            for (Score s : getFacade().getScores(g.getId())) {
                for (PlayerAssignment assignment : assignemntsForGame) {
                    if (s.getTeamNr() == assignment.getTeam()) {
                        Integer pointsPerPlayer = mapPointsPerPlayer.get(assignment.getPlayer().getName());
                        int newscore = s.getScoreCount() * 100;
                        if (null != pointsPerPlayer) {
                            mapPointsPerPlayer.put(assignment.getPlayer().getName(), pointsPerPlayer + newscore);
                        } else {
                            mapPointsPerPlayer.put(assignment.getPlayer().getName(), newscore);
                        }

                        overallScore += newscore;
                    }
                }
            }
        }

        // we found some scores, let's calculate the strength
        if (overallScore > 0) {
            int maxScore = overallScore / mapPointsPerPlayer.size();
            int oneStar = maxScore / Flags.MAXSTARS;
            for (String playername : mapPointsPerPlayer.keySet()) {
                int scored = mapPointsPerPlayer.get(playername);
                mapStarsPerPlayer.put(playername, Math.min(Flags.MAXSTARS, scored / oneStar));
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
        List<PlayerAssignment> assignments = adapter.getAssignmentsDone();
        String conflicts = TeamReactor.decideTeams(teamcount, playercount, assignments);
        if (!conflicts.isEmpty()) {
            Toast.makeText(getApplicationContext(), conflicts + "\n..." + getString(R.string.forceMove), Toast.LENGTH_LONG).show();
        }
        if (assignments.size() < TeamReactor.getAssignments().size()) {
            Intent callChooser = new Intent(getApplicationContext(),
                    TeamChooserActivity.class);
            callChooser.putExtra(Flags.SPORT, sports);
            callChooser.putExtra(Flags.TEAMCOUNT, teamcount);
            startActivity(callChooser);
        } else {
            GameRecord saveGameRecord = new GameRecord(TeamReactor.getAssignments());
            saveGameRecord.setSport(sports);
            long id = getFacade().persistGame(saveGameRecord);
            Intent callOverview = new Intent(getApplicationContext(),
                    GameRecordListActivity.class);
            callOverview.putExtra(Flags.GAME_ID, id);
            callOverview.putExtra(Flags.TEAMCOUNT, teamcount);
            startActivity(callOverview);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        PlayerSelectionRV_Adapter.PlayerViewHolder pvh = (PlayerSelectionRV_Adapter.PlayerViewHolder) buttonView.getTag();
        PlayerAssignment pa = adapter.getAssignments().get(pvh.getAdapterPosition());
        if (isChecked && !pa.isRevealed()) {
            if (adapter.getAssignmentsDone().size() < playercount) {
                adapter.getAssignments().get(pvh.getAdapterPosition()).setRevealed(isChecked);
                pvh.expandView();
            } else {
                buttonView.setChecked(false);
                Toast.makeText(getApplicationContext(), getString(R.string.maxplayers_reached), Toast.LENGTH_SHORT).show();
            }
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
