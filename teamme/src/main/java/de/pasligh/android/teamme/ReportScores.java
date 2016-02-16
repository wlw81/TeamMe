package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.Game;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.objects.Score;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.ScoreRV_Adapter;
import de.pasligh.android.teamme.tools.ScoreRV_Interface;
import de.pasligh.android.teamme.tools.TeamReactor;

public class ReportScores extends AppCompatActivity implements ScoreRV_Interface, View.OnClickListener {
    private BackendFacade facade;
    ScoreRV_Adapter adapter;
    int teamCount;
    long gameId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_scores);

        gameId = getIntent().getLongExtra(Flags.GAME_ID, -1);
        if (gameId < 0) {
            gameId = getIntent().getIntExtra
                    (Flags.GAME_ID, -1);
        }
        teamCount = getFacade().getTeamCount(gameId);

        if (TeamReactor.getAssignments() == null || TeamReactor.getAssignments().isEmpty()) {
            Set<PlayerAssignment> readAssignments = new HashSet<>();
            for (PlayerAssignment p : getFacade().getAssignments((int) gameId)) {
                p.setRevealed(true);
                readAssignments.add(p);
            }
            TeamReactor.overwriteAssignments(readAssignments);
        }

        RecyclerView rv = (RecyclerView) findViewById(R.id.RoundResultRV);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);
        List<Score> scoreList = getScores();
        if (scoreList.isEmpty()) {
            scoreList.addAll(createDummyScores(0));
        }
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf");

        adapter = new ScoreRV_Adapter(getApplicationContext(), scoreList, tf, this);
        rv.setAdapter(adapter);
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        ((FloatingActionButton) findViewById(R.id.addScoreFAB)).setOnClickListener(this);
        Game game = getFacade().getGame((int) gameId);
        getSupportActionBar().setTitle(game.getSport() + " " + dateFormat.format(game.getStartedAt()));
    }

    private List<Score> createDummyScores(int p_intRoundNr) {
        List<Score> scoreList = new ArrayList<>();
        int currentTeam = 1;
        while (currentTeam <= teamCount) {
            scoreList.add(createNewScore_toResults(p_intRoundNr, currentTeam));
            currentTeam++;
        }
        return scoreList;
    }

    public void reload(List<Score> p_lisScores) {
        adapter.updateScores(p_lisScores, true);
    }

    @NonNull
    private List<Score> getScores() {
        Map<Integer, List> roundResultMap = new HashMap<Integer, List>();
        List<Score> scoreList = getFacade().getScores(gameId);
        return scoreList;
    }

    @Override
    public void recieveHolder(ScoreRV_Adapter.RoundResultViewHolder p_holder, final List<Score> lisScore) {
        for (final Score s : lisScore) {
            Log.i(Flags.LOGTAG, "recieve holder for " + s);
            Button buttonTeam = new Button(getApplicationContext());
            buttonTeam.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            buttonTeam.setText(getString(R.string.team) + " " + (TeamReactor.getAssignmentsByTeam(s.getTeamNr()).get(0).getPlayer().getName()) + ": " + s.getScoreCount() + " " + getString(R.string.points));
            buttonTeam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != s) {
                        final NumberPicker np = new NumberPicker(getApplicationContext());
                        np.setMinValue(0);
                        np.setMaxValue(99);
                        np.setValue(s.getScoreCount());

                        AlertDialog.Builder builder = new AlertDialog.Builder(ReportScores.this).setView(np);
                        builder.setMessage(getString(R.string.team) + " " + TeamReactor.getAssignmentsByTeam(s.getTeamNr()).get(0).getPlayer().getName() + " - " + getString(R.string.points) + ": ").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (Score sSave : lisScore) {
                                    if (!sSave.equals(s)) {
                                        getFacade().mergeScore(sSave);
                                    }
                                }
                                s.setScoreCount(np.getValue());
                                getFacade().mergeScore(s);
                                reload(getScores());
                            }
                        });
                        builder.show();
                    }
                }
            });
            p_holder.getLayoutButtons().addView(buttonTeam);
        }
    }

    public Score createNewScore_toResults(int p_intRoundNr, int p_intTeamNr) {
        Score newScore = new Score();
        newScore.setGameId((int) gameId);
        newScore.setRoundNr(p_intRoundNr);
        newScore.setTeamNr(p_intTeamNr);
        return newScore;
    }

    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getApplicationContext());
        }
        return facade;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addScoreFAB) {
            List<Score> lisScores = getScores();
            lisScores.addAll(createDummyScores(adapter.getRoundResultMap().size()));
            reload(lisScores);
        }
    }
}
