package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import de.pasligh.android.teamme.tools.HoloCircleSeekBar;
import de.pasligh.android.teamme.tools.HoloCircleSeekBar.OnCircleSeekBarChangeListener;
import de.pasligh.android.teamme.tools.TTS_Tool;
import de.pasligh.android.teamme.tools.TeamReactor;

/**
 * An activity representing a list of games. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link GameStatisticsActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link GameCreatorFragment} and the item details (if present) is a
 * {@link GameStatisticsFragment}.
 * <p/>
 * This activity also implements the required
 * {@link GameCreatorFragment.Callbacks} interface to listen for item
 * selections.
 */
public class GameCreatorActivity extends FragmentActivity implements
        GameCreatorFragment.Callbacks, OnCircleSeekBarChangeListener, OnCheckedChangeListener, OnClickListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private int playerCount;
    private int teamCount = 4;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TTS_Tool.getInstance(this);
        setContentView(R.layout.activity_game_list);
        HoloCircleSeekBar seekbar = ((HoloCircleSeekBar) findViewById(R.id.PlayerPicker));
        seekbar.setOnSeekBarChangeListener(this);
        ((RadioGroup) findViewById(R.id.TeamsRadioGroup))
                .setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.MoreTeamRadioButton))
                .setOnClickListener(this);

        playerCount = 4;

        if (findViewById(R.id.game_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            GameStatisticsFragment fragment = new GameStatisticsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.game_detail_container, fragment).commit();
            invalidateOptionsMenu();
        }

        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Condensed.ttf");
        ((TextView) findViewById(R.id.NewGameLabelTextView)).setTypeface(tf);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_creator, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.StartItem) {
            int teamCount = getTeamCount();
            TeamReactor.decideTeams(teamCount, playerCount);
            Intent callChooser = new Intent(getApplicationContext(),
                    TeamChooser.class);
            startActivity(callChooser);
        } else if (item.getItemId() == R.id.AboutItem) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(R.string.app_name));
            alertDialog.setMessage("by Thomas Pasligh");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            alertDialog.setIcon(R.drawable.ic_launcher);
            alertDialog.show();
        } else if (item.getItemId() == R.id.StatisticsItem) {
            Intent statisticsActivity = new Intent(getApplicationContext(),
                    GameStatisticsActivity.class);
            startActivity(statisticsActivity);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean valid = playerCount >= getTeamCount();
        MenuItem startItemView = menu.findItem(R.id.StartItem);
        startItemView.setVisible(valid);
        menu.findItem(R.id.StatisticsItem).setVisible(!mTwoPane);
        return super.onPrepareOptionsMenu(menu);
    }

    private int getTeamCount() {

        if (((RadioButton) findViewById(R.id.TwoTeamRadioButton)).isChecked()) {
            teamCount = 2;
        } else if (((RadioButton) findViewById(R.id.ThreeTeamRadioButton))
                .isChecked()) {
            teamCount = 3;
        }
        return teamCount;
    }

    @Override
    protected void onDestroy() {
        try {
            TTS_Tool.getInstance(this).shutdown();
        } catch (Exception e) {
            // TODO: handle exception
        }
        super.onDestroy();
    }

    @Override
    public void onProgressChanged(HoloCircleSeekBar seekBar, int progress,
                                  boolean fromUser) {
        playerCount = progress;
        invalidateOptionsMenu();
    }

    @Override
    public void onItemSelected(String id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        invalidateOptionsMenu();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GameCreator Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://de.pasligh.android.teamme/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GameCreator Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://de.pasligh.android.teamme/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == (R.id.MoreTeamRadioButton)){

            if(teamCount <= 4){
                // more players? if selected start with 4 - getting everything prepared nice and smoothly
                teamCount = 4;
                ((RadioButton) findViewById(R.id.MoreTeamRadioButton)).setText(String.valueOf(teamCount));
                invalidateOptionsMenu();
            }

            final NumberPicker np = new NumberPicker(getApplicationContext());
            np.setMinValue(4);
            //Specify the maximum value/number of NumberPicker
            np.setMaxValue(10);

            AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(np);
            builder.setMessage(getString(R.string.title_dialog_teamcount)).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    teamCount = np.getValue();
                    ((RadioButton) findViewById(R.id.MoreTeamRadioButton)).setText(String.valueOf(np.getValue()));
                    invalidateOptionsMenu();
                }
            });
            builder.show();
        }
    }
}
