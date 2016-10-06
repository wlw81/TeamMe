package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.GameRecord;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.objects.Score;
import de.pasligh.android.teamme.tools.AnimationHelper;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.ScoreRV_Adapter;
import de.pasligh.android.teamme.tools.ScoreRV_Interface;
import de.pasligh.android.teamme.tools.ShareHelper;
import de.pasligh.android.teamme.tools.TeamReactor;

public class ReportScoresActivity extends AppCompatActivity implements ScoreRV_Interface, View.OnClickListener {
    private BackendFacade facade;
    ScoreRV_Adapter adapter;
    int teamCount;
    long gameId;
    private ShareActionProvider mShareActionProvider;
    private StaggeredGridLayoutManager gridLayoutManager;

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

        Set<PlayerAssignment> readAssignments = new HashSet<>();
        for (PlayerAssignment p : getFacade().getAssignments((int) gameId)) {
            p.setRevealed(true);
            readAssignments.add(p);
        }
        TeamReactor.overwriteAssignments(readAssignments);

        RecyclerView rv = (RecyclerView) findViewById(R.id.RoundResultRV);

        float scalefactor = getResources().getDisplayMetrics().density * 100;
        int number = getWindowManager().getDefaultDisplay().getWidth();
        int columns = 1;
        try {
            columns = (int) Math.max(1, ((float) number / (float) scalefactor) / 4);
        } catch (Exception e) {
            Log.w(Flags.LOGTAG, "Can't determine column count (" + e.getMessage() + ")!");
        }


        // First param is number of columns and second param is orientation i.e Vertical or Horizontal
        gridLayoutManager =
                new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
        // Attach the layout manager to the recycler view
        rv.setLayoutManager(gridLayoutManager);

        List<Score> scoreList = getScores();
        if (scoreList.isEmpty()) {
            scoreList.addAll(createDummyScores(0));
            findViewById(R.id.addScoreFAB).setVisibility(View.INVISIBLE);
        }
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf");

        ((TextView) findViewById(R.id.ScoreWinnerTV)).setTypeface(tf);

        adapter = new ScoreRV_Adapter(getApplicationContext(), scoreList, tf, this);
        rv.setAdapter(adapter);
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        ((FloatingActionButton) findViewById(R.id.addScoreFAB)).setOnClickListener(this);
        GameRecord gameRecord = getFacade().getGame((int) gameId);
        getSupportActionBar().setTitle(gameRecord.getSport() + " " + dateFormat.format(gameRecord.getStartedAt()));
        publishWinningTeam(scoreList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.report_scores, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.ScoreShareContext);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setShareIntent(createShareIntent());
        return true;
    }

    private Intent createShareIntent() {
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        GameRecord gameRecord = getFacade().getGame((int) gameId);
        String title = gameRecord.getSport() + " " + dateFormat.format(gameRecord.getStartedAt());
        StringBuilder shareText = new StringBuilder();
        shareText.append(title).append(": ");
        shareText.append(((TextView) findViewById(R.id.ScoreWinnerTV)).getText());

        for (int i = 0; i < adapter.getItemCount(); i++) {
            int roundNr = 1 + i;
            shareText.append(" - ");
            shareText.append(getString(R.string.round).toUpperCase() + " #" + roundNr + " -");

            for (Score p_score : getScores()) {
                if (p_score.getRoundNr() == i) {

                    String teamname = TeamReactor.getAssignmentsByTeam(p_score.getTeamNr()).get(0).getPlayer().getName();
                    shareText.append(" " + teamname + ": " + p_score.getScoreCount());
                }
            }
        }

        // create app footer
        shareText.append(" ");
        ShareHelper.appendFooter_Signature(shareText, getString(R.string.shareFooter));
        return ShareCompat.IntentBuilder.from(this).setType("text/plain").setText(shareText.toString().trim()).setSubject(((TextView) (findViewById(R.id.ScoreWinnerTV))).getText().toString()).getIntent();
    }

    public void publishWinningTeam(List<Score> p_scoreList) {
        Integer[][] wonRounds = new Integer[adapter.getItemCount()][teamCount + 1];
        Integer[][] pointsEachRound = new Integer[adapter.getItemCount()][teamCount + 1];

        for (int currentRoundNr = 0; currentRoundNr < pointsEachRound.length; currentRoundNr++) {
            int highestScore = -1;
            for (int currentTeam = 1; currentTeam < pointsEachRound[currentRoundNr].length; currentTeam++) {
                // we're adding everything up to an 2dim array, so we can scroll through nicely
                for (Score s : p_scoreList) {
                    if (s.getRoundNr() == currentRoundNr && s.getTeamNr() == currentTeam) {
                        if (pointsEachRound[currentRoundNr][currentTeam] == null) {
                            pointsEachRound[currentRoundNr][currentTeam] = new Integer(0);
                        }
                        pointsEachRound[currentRoundNr][currentTeam] += s.getScoreCount();

                        // let's measure the highest score!
                        if (s.getScoreCount() >= highestScore) {
                            highestScore = s.getScoreCount();
                        }
                    }
                }
            }


            // let's see wich team gained the most points this round
            for (int currentTeam = 1; currentTeam < pointsEachRound[currentRoundNr].length; currentTeam++) {
                if (pointsEachRound[currentRoundNr][currentTeam] == highestScore) {
                    if (wonRounds[currentRoundNr][currentTeam] == null) {
                        wonRounds[currentRoundNr][currentTeam] = new Integer(0);
                    }
                    wonRounds[currentRoundNr][currentTeam] += 1; // team scored a round point!
                }
            }


        }

        Integer[] overAllScoreEachTeam = new Integer[teamCount + 1];
        Integer[] roundPointsEachTeam = new Integer[teamCount + 1];

        for (int round = 0; round < wonRounds.length; round++) {
            for (int team = 1; team < wonRounds[round].length; team++) {

                if (roundPointsEachTeam[team] == null) {
                    roundPointsEachTeam[team] = new Integer(0);
                }

                if (overAllScoreEachTeam[team] == null) {
                    overAllScoreEachTeam[team] = new Integer(0);
                }

                if (null != wonRounds[round][team]) {
                    roundPointsEachTeam[team] += wonRounds[round][team];
                }

                if (null != pointsEachRound[round][team]) {
                    overAllScoreEachTeam[team] += pointsEachRound[round][team];
                }
            }
        }


        int maxRoundPoints = 0;
        int winningTeam = -1;
        for (int team = 1; team < roundPointsEachTeam.length; team++) {
            if (winningTeam == -1 || roundPointsEachTeam[team] > maxRoundPoints) {
                maxRoundPoints = roundPointsEachTeam[team];
                winningTeam = team;
                Log.i(Flags.LOGTAG, "Wins through roundpoints - team " + team + " round points:  " + roundPointsEachTeam[team]);
            } else if (roundPointsEachTeam[team] == maxRoundPoints) {
                winningTeam = Flags.DRAW_TEAM;
                Log.i(Flags.LOGTAG, "Even through roundpoints - team " + team + " round points:  " + roundPointsEachTeam[team]);
            }
        }

        if (winningTeam == Flags.DRAW_TEAM) {
            winningTeam = -1;
            int maxOverAllScore = 0;
            for (int team = 1; team < overAllScoreEachTeam.length; team++) {
                int compareScore = overAllScoreEachTeam[team];
                if (winningTeam == -1 || compareScore > maxOverAllScore) {
                    maxOverAllScore = compareScore;
                    winningTeam = team;
                    Log.i(Flags.LOGTAG, "Wins through overall score - team " + team + " score:  " + compareScore);
                } else if (compareScore == maxOverAllScore) {
                    winningTeam = Flags.DRAW_TEAM;
                    Log.i(Flags.LOGTAG, "Even through overall score - team " + team + " score:  " + compareScore);
                }
            }
        }


        if (winningTeam != Flags.DRAW_TEAM) {
            ((TextView) findViewById(R.id.ScoreWinnerTV)).setText(getString(R.string.team) + " " + TeamReactor.getAssignmentsByTeam(winningTeam).get(0).getPlayer().getName() + " " + getString(R.string.wins) + "!");
        } else {
            ((TextView) findViewById(R.id.ScoreWinnerTV)).setText(getString(R.string.draw).toUpperCase() + "!");
        }


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

    public void reload(List<Score> p_lisScores, boolean p_hideFAB) {
        adapter.updateScores(p_lisScores, true);
        publishWinningTeam(p_lisScores);
        mShareActionProvider.setShareIntent(createShareIntent());
        if (p_hideFAB) {
            if (findViewById(R.id.addScoreFAB).getVisibility() == View.VISIBLE) {
                if (!AnimationHelper.hide(findViewById(R.id.newGameFAB))) {
                    findViewById(R.id.addScoreFAB).setVisibility(View.INVISIBLE);
                }
            }
        } else {
            if (findViewById(R.id.addScoreFAB).getVisibility() == View.INVISIBLE) {
                if (AnimationHelper.reveal(findViewById(R.id.addScoreFAB)) == null) {
                    findViewById(R.id.addScoreFAB).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @NonNull
    private List<Score> getScores() {
        Map<Integer, List> roundResultMap = new HashMap<Integer, List>();
        List<Score> scoreList = getFacade().getScores(gameId);
        return scoreList;
    }

    @Override
    public void recieveHolder(final ScoreRV_Adapter.RoundResultViewHolder p_holder, final List<Score> lisScore) {
        for (final Score s : lisScore) {
            Log.i(Flags.LOGTAG, "recieve holder for " + s);
            Button buttonTeam = new Button(new ContextThemeWrapper(getApplication(), R.style.AppTheme));
            buttonTeam.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            buttonTeam.setText(getString(R.string.team) + " " + (TeamReactor.getAssignmentsByTeam(s.getTeamNr()).get(0).getPlayer().getName()) + ": " + s.getScoreCount() + " " + getString(R.string.points));
            buttonTeam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != s) {
                        View viewInflated = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_score_input, (ViewGroup) findViewById(android.R.id.content), false);
                        // Set up the input
                        final EditText input = (EditText) viewInflated.findViewById(R.id.ScoreInputTV);
                        if (s.getScoreCount() != 0) {
                            input.setText(String.valueOf(s.getScoreCount()));
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(ReportScoresActivity.this).setView(viewInflated);
                        builder.setMessage(getString(R.string.team) + " " + TeamReactor.getAssignmentsByTeam(s.getTeamNr()).get(0).getPlayer().getName() + " - " + getString(R.string.points) + ": ").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!input.getText().toString().trim().isEmpty()) {
                                    for (Score sSave : lisScore) {
                                        if (!sSave.equals(s)) {
                                            getFacade().mergeScore(sSave);
                                        }
                                    }

                                    try {
                                        s.setScoreCount(Integer.parseInt(input.getText().toString()));
                                    } catch (NumberFormatException e) {
                                        Log.e(Flags.LOGTAG, e.getMessage());
                                        s.setScoreCount(0);
                                    }

                                    getFacade().mergeScore(s);
                                    reload(getScores(), false);
                                    // previously invisible view
                                    View myView = findViewById(R.id.addScoreFAB);
                                    if (myView.getVisibility() != View.VISIBLE) {
                                        if (AnimationHelper.reveal(myView) == null) {
                                            myView.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            }
                        });
                        final AlertDialog alertToShow = builder.create();
                        alertToShow.getWindow().setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        alertToShow.show();
                        input.requestFocus();
                        input.selectAll();
                        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_DONE
                                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                                    alertToShow.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                                    return true;
                                }
                                return false;
                            }
                        });
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
            reload(lisScores, true);
        }
    }
}
