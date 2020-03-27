package de.pasligh.android.teamme;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.GameRecord;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.tools.TTS_Tool;
import de.pasligh.android.teamme.tools.TeamReactor;

/**
 * An activity representing a single GameRecord detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link GameRecordListActivity}.
 */
public class GameRecordDetailActivity extends AppCompatActivity {

    private BackendFacade facade;
    private static String currentId = null;
    private SoundPool soundPool;
    private int spFanfare;
    private int DURATION_MEDIA_FILE = 3000;

    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getApplicationContext());
        }
        return facade;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamerecord_detail);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        soundPool = new SoundPool.Builder().build();
        spFanfare = soundPool.load(this, R.raw.beep, 1);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            String id = getIntent().getStringExtra(GameRecordDetailFragment.ARG_ITEM_ID);
            if (id != null) {
                currentId = id;
            }
            if (currentId != null) {
                arguments.putString(GameRecordDetailFragment.ARG_ITEM_ID,
                        currentId);
            }
            GameRecordDetailFragment fragment = new GameRecordDetailFragment();
            fragment.setApplicationContext(getApplicationContext());
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.gamerecord_detail_container, fragment)
                    .commit();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentId == null || getFacade().getGame(Integer.parseInt(currentId)) == null) {
            finish();
        }
    }

    public StringBuilder createTeamDecided_AnnouncementText() {
        StringBuilder shareText = new StringBuilder();
        shareText.append(getString(R.string.shareIntent)).append(" ");
        int teamNr = 1;
        List<PlayerAssignment> assignments;
        while (!(assignments = TeamReactor.getAssignmentsByTeam(teamNr))
                .isEmpty()) {
            shareText.append(getString(R.string.team)).append(" ")
                    .append(assignments.get(0).getPlayer().getName())
                    .append("... ");
            for (int i = 0; i < assignments.size(); i++) {
                shareText.append(getString(R.string.player)).append(" ").append(i + 1).append(":");
                shareText.append(assignments.get(i).getPlayer().getName())
                        .append(", ");
            }
            shareText.append(". ");

            teamNr++;
        }
        shareText.append(getString(R.string.luck));
        return shareText;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, GameRecordListActivity.class));
            return true;
        } else if (id == R.id.TeamOverviewAnnounceMenuItem) {
            soundPool.play(spFanfare, 1, 1, 1, 0, 1);
            final String announcement = createTeamDecided_AnnouncementText().toString();

            Timer myMediaTimer = new Timer();
            myMediaTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    TTS_Tool.getInstance(GameRecordDetailActivity.this)
                            .speak(announcement, TextToSpeech.QUEUE_FLUSH);
                }

            }, DURATION_MEDIA_FILE);
        } else if (id == R.id.TeamOverviewDeleteMenuItem) {
            GameRecord gr = getFacade().getGame(Integer.parseInt(currentId));
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.playerDeleteDialog_question).replace("$1", gr.getSport())).setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (getFacade().deleteAssignments(Integer.parseInt(currentId))) {
                                getFacade().deleteGame(Integer.parseInt(currentId));
                            }

                            onBackPressed();
                        }
                    }

            );
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateUpTo(new Intent(this, GameCreatorActivity.class));
    }


}
