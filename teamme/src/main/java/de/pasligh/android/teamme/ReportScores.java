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
import java.util.List;
import java.util.Map;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.Score;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.ScoreRV_Adapter;
import de.pasligh.android.teamme.tools.ScoreRV_Interface;

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
        teamCount = getFacade().getTeamCount(gameId);
        RecyclerView rv = (RecyclerView) findViewById(R.id.RoundResultRV);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);
        List<Score> scoreList = getScores();
        if (scoreList.isEmpty()) {
            scoreList.addAll(createDummyScores(0));
        }
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf");

        adapter = new ScoreRV_Adapter(scoreList, tf, this);
        rv.setAdapter(adapter);

        ((FloatingActionButton) findViewById(R.id.addScoreFAB)).setOnClickListener(this);
        getSupportActionBar().setTitle("Game " + gameId);
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
    public void recieveHolder(ScoreRV_Adapter.RoundResultViewHolder p_holder, final List<Score> lisScore  ) {
        for(final Score s : lisScore){
            Log.i(Flags.LOGTAG, "recieve holder for " + s);
            Button buttonTeam = new Button(getApplicationContext());
            buttonTeam.setText(getString(R.string.team) + " " + (s.getTeamNr()) + ": " + s.getScoreCount() + " " + getString(R.string.points));
            buttonTeam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != s) {
                        final NumberPicker np = new NumberPicker(getApplicationContext());
                        np.setMinValue(0);
                        np.setMaxValue(99);
                        np.setValue(s.getScoreCount());

                        AlertDialog.Builder builder = new AlertDialog.Builder(ReportScores.this).setView(np);
                        builder.setMessage(getString(R.string.team) + " " + s.getTeamNr() + " - " + getString(R.string.points) + ": ").setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
        newScore.setGameId((int) getIntent().getLongExtra(Flags.GAME_ID, -1));
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
            getScores().addAll(createDummyScores(adapter.getRoundResultMap().size()+1));
            reload(lisScores);
        }
    }
}
