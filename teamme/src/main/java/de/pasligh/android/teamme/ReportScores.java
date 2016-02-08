package de.pasligh.android.teamme;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.Score;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.ScoreRV_Adapter;

public class ReportScores extends Activity {
    private BackendFacade facade;
    ScoreRV_Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_scores);

        RecyclerView rv = (RecyclerView) findViewById(R.id.RoundResultRV);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);
        Map<Integer, Score> roundResultMap = new HashMap<Integer, Score>();
        long gameId = getIntent().getLongExtra(Flags.GAME_ID, -1);
        List<Score> scoreList = getFacade().getScores(gameId);
        if (scoreList.isEmpty()) {
            int currentTeam = 1;
            int teamCount = getFacade().getTeamCount(gameId);
            while (currentTeam < teamCount) {
                scoreList.add(createNowScore_toResults(1, currentTeam));
                currentTeam++;
            }
        }

        for (Score s : scoreList) {
            roundResultMap.put(s.getRoundNr(), s);
        }

        adapter = new ScoreRV_Adapter(roundResultMap);
        rv.setAdapter(adapter);
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


}
