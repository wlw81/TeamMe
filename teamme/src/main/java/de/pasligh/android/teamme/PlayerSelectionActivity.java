package de.pasligh.android.teamme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.Game;
import de.pasligh.android.teamme.objects.Player;
import de.pasligh.android.teamme.objects.PlayerAssignemnt;
import de.pasligh.android.teamme.objects.Score;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.PlayerSelectionRV_Adapter;
import de.pasligh.android.teamme.tools.TeamReactor;

public class PlayerSelectionActivity extends Activity implements View.OnClickListener {

    private BackendFacade facade;
    PlayerSelectionRV_Adapter adapter;

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
                (this, android.R.layout.simple_spinner_item, list);

        List<PlayerAssignemnt> blankAssignments = new ArrayList<PlayerAssignemnt>();
        List<Player> allPlayers = getFacade().getPlayers();

        Map<String, Integer> mapStarsPerPlayer = new HashMap<String, Integer>();
        Map<String, Integer> mapPointsPerPlayer = new HashMap<String, Integer>();

        List<Game> games = getFacade().getGames(getIntent().getStringExtra(Flags.SPORT));
        int overallScore = 0;

        for (Game g : games) {
            List<PlayerAssignemnt> assignemntsForGame = getFacade().getAssignments(g.getId());

            for (Score s : getFacade().getScores(g.getId())) {
                for (PlayerAssignemnt assignment : assignemntsForGame) {
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
            PlayerAssignemnt assignBlank = new PlayerAssignemnt();
            assignBlank.setPlayer(p);
            blankAssignments.add(assignBlank);
        }

        adapter = new PlayerSelectionRV_Adapter(getApplicationContext(), blankAssignments, Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf"), dataAdapter, mapStarsPerPlayer, getIntent().getIntExtra(Flags.PLAYERCOUNT, -1));
        rv.setAdapter(adapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.playserSelectionToolbar);
        toolbar.setTitle(getString(R.string.title_activity_player_selection));
        toolbar.setLogo(R.drawable.actionbar);
    }

    @Override
    public void onClick(View v) {
        List<PlayerAssignemnt> assignments = adapter.getAssignmentsDone();
        int teamcount = getIntent().getIntExtra(Flags.TEAMCOUNT, -1);
        TeamReactor.decideTeams(teamcount, getIntent().getIntExtra(Flags.PLAYERCOUNT, -1), assignments);
        if (assignments.size() < TeamReactor.getAssignments().size()) {
            Intent callChooser = new Intent(getApplicationContext(),
                    TeamChooser.class);
            callChooser.putExtra(Flags.SPORT, getIntent().getStringExtra(Flags.SPORT));
            callChooser.putExtra(Flags.TEAMCOUNT, teamcount);
            startActivity(callChooser);
        } else {
            Game saveGame = new Game(TeamReactor.getAssignments());
            saveGame.setSport(getIntent().getStringExtra(Flags.SPORT));
            long id = getFacade().persistGame(saveGame);
            Intent callOverview = new Intent(getApplicationContext(),
                    TeamOverview.class);
            callOverview.putExtra(Flags.GAME_ID, id);
            callOverview.putExtra(Flags.TEAMCOUNT, teamcount);
            startActivity(callOverview);
        }

    }

}
