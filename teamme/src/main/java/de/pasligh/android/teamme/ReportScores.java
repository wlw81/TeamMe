package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;

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
    Map<Button, Score> mapScore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_scores);

        mapScore = new HashMap<>();
        gameId = getIntent().getLongExtra(Flags.GAME_ID, -1);
        teamCount = getFacade().getTeamCount(gameId);
        RecyclerView rv = (RecyclerView) findViewById(R.id.RoundResultRV);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);
        List<Score> scoreList = getScores();
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf");

        adapter = new ScoreRV_Adapter(scoreList, tf, this);
        rv.setAdapter(adapter);

        getSupportActionBar().setTitle("Game "+gameId);
    }

    public void reload() {
        adapter.updateScores(getScores(), true);
    }

    @NonNull
    private List<Score> getScores() {
        Map<Integer, List> roundResultMap = new HashMap<Integer, List>();
        List<Score> scoreList = getFacade().getScores(gameId);
        if (scoreList.isEmpty()) {
            int currentTeam = 1;
            while (currentTeam < teamCount) {
                scoreList.add(createNowScore_toResults(0, currentTeam));
                currentTeam++;
            }
        }
        return scoreList;
    }

    @Override
    public void recieveHolder(ScoreRV_Adapter.RoundResultViewHolder p_holder, Score p_score) {
        for (int i = 0; i < teamCount; i++) {
            Button buttonTeam = new Button(getApplicationContext());
            buttonTeam.setText(getString(R.string.team) + " " + (i + 1) + ": " + p_score.getScoreCount() + " " + getString(R.string.points));
            buttonTeam.setOnClickListener(this);
            mapScore.put(buttonTeam, p_score);
            p_holder.getLayoutButtons().addView(buttonTeam);
        }
    }

    public Score createNowScore_toResults(int p_intRoundNr, int p_intTeamNr) {
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
        final Score score = mapScore.get(v);
        if (null != score) {
            final NumberPicker np = new NumberPicker(getApplicationContext());
            np.setMinValue(0);
            np.setMaxValue(99);
            np.setValue(score.getScoreCount());

            AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(np);
            builder.setMessage(getString(R.string.team) + " " + score.getTeamNr() + " - "+getString(R.string.points)+": ").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    score.setScoreCount(np.getValue());
                    getFacade().mergeScore(score);
                    reload();
                }
            });
            builder.show();
        }
    }
}
