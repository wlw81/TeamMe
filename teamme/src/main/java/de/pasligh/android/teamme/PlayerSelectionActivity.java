package de.pasligh.android.teamme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.Game;
import de.pasligh.android.teamme.objects.Player;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.objects.Score;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.PlayerSelectionRV_Adapter;
import de.pasligh.android.teamme.tools.TeamReactor;

public class PlayerSelectionActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

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

        List<PlayerAssignment> blankAssignments = new ArrayList<PlayerAssignment>();
        List<Player> allPlayers = getFacade().getPlayers();

        Map<String, Integer> mapStarsPerPlayer = new HashMap<String, Integer>();
        Map<String, Integer> mapPointsPerPlayer = new HashMap<String, Integer>();

        sports = getIntent().getStringExtra(Flags.SPORT);
        teamcount = getIntent().getIntExtra(Flags.TEAMCOUNT, -1);
        playercount = getIntent().getIntExtra(Flags.PLAYERCOUNT, -1);

        List<Game> games = getFacade().getGames(sports);
        int overallScore = 0;


        for (Game g : games) {
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

        adapter = new PlayerSelectionRV_Adapter(getApplicationContext(), blankAssignments, Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf"), dataAdapter, this, mapStarsPerPlayer, playercount);
        rv.setAdapter(adapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.playserSelectionToolbar);
        toolbar.setTitle(getString(R.string.title_activity_player_selection));
        toolbar.setLogo(R.drawable.actionbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(PlayerSelectionActivity.this);
            }
        });

    }

    @Override
    public void onClick(View v) {
        List<PlayerAssignment> assignments = adapter.getAssignmentsDone();
        TeamReactor.decideTeams(teamcount, playercount, assignments);
        if (assignments.size() < TeamReactor.getAssignments().size()) {
            Intent callChooser = new Intent(getApplicationContext(),
                    TeamChooser.class);
            callChooser.putExtra(Flags.SPORT, sports);
            callChooser.putExtra(Flags.TEAMCOUNT, teamcount);
            startActivity(callChooser);
        } else {
            Game saveGame = new Game(TeamReactor.getAssignments());
            saveGame.setSport(sports);
            long id = getFacade().persistGame(saveGame);
            Intent callOverview = new Intent(getApplicationContext(),
                    TeamOverview.class);
            callOverview.putExtra(Flags.GAME_ID, id);
            callOverview.putExtra(Flags.TEAMCOUNT, teamcount);
            startActivity(callOverview);
        }
        // adapter.getAssignments().clear();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (adapter.getAssignmentsDone().size() < playercount) {
                PlayerSelectionRV_Adapter.PlayerViewHolder pvh = (PlayerSelectionRV_Adapter.PlayerViewHolder) buttonView.getTag();
                adapter.getAssignments().get(pvh.getAdapterPosition()).setRevealed(isChecked);

                if (isChecked) {
                    pvh.expandView();
                }
            } else {
                buttonView.setChecked(false);
                Toast.makeText(getApplicationContext(), getString(R.string.maxplayers_reached), Toast.LENGTH_SHORT).show();
            }
        }else{
            PlayerSelectionRV_Adapter.PlayerViewHolder pvh = (PlayerSelectionRV_Adapter.PlayerViewHolder) buttonView.getTag();
            adapter.getAssignments().get(pvh.getAdapterPosition()).setRevealed(isChecked);
            pvh.collapseView();
        }
    }
}
