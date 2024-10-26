package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ShareCompat;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
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
import de.pasligh.android.teamme.tools.TeamReactor;
import de.pasligh.android.teamme.tools.TextHelper;

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

        adapter = new ScoreRV_Adapter(getApplicationContext(), scoreList, this);
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
        TextHelper.appendFooter_Signature(shareText, getString(R.string.shareFooter));
        return ShareCompat.IntentBuilder.from(this).setType("text/plain").setText(shareText.toString().trim()).setSubject(((TextView) (findViewById(R.id.ScoreWinnerTV))).getText().toString()).getIntent();
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

    public void publishWinningTeam(List<Score> p_scoreList) {
        int winningTeam = TextHelper.getWinnerTeam_by_RoundsOrScore(p_scoreList, adapter.getItemCount(), teamCount);
        if (winningTeam != Flags.DRAW_TEAM) {
            ((TextView) findViewById(R.id.ScoreWinnerTV)).setText(getString(R.string.team) + " " + TeamReactor.getAssignmentsByTeam(winningTeam).get(0).getPlayer().getName() + " " + getString(R.string.wins) + "!");
        } else {
            ((TextView) findViewById(R.id.ScoreWinnerTV)).setText(getString(R.string.draw).toUpperCase() + "!");
        }
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
            final Button buttonTeam = new Button(getApplicationContext());
            buttonTeam.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            buttonTeam.setText(getString(R.string.team) + " " + (TeamReactor.getAssignmentsByTeam(s.getTeamNr()).get(0).getPlayer().getName()) + ": " + s.getScoreCount() + " " + getString(R.string.points));
            buttonTeam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != s) {
                        View viewInflated = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_score_input, (ViewGroup) findViewById(R.id.gameRecordCV), false);
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
