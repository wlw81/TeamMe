package de.pasligh.android.teamme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.tools.Flags;
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
public class GameCreatorActivity extends AppCompatActivity implements
        GameCreatorFragment.Callbacks, OnCircleSeekBarChangeListener, OnCheckedChangeListener, OnClickListener, TextView.OnEditorActionListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private int playerCount;
    private int teamCount = 4;

    private BackendFacade facade;

    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getApplicationContext());
        }
        return facade;
    }

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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, getFacade()
                .getSports());

        AutoCompleteTextView playerNameTextView = (AutoCompleteTextView) findViewById(R.id.SportTextView);
        playerNameTextView.setOnEditorActionListener(this);
        playerNameTextView.setAdapter(adapter);

        HoloCircleSeekBar seekbar = ((HoloCircleSeekBar) findViewById(R.id.PlayerPicker));
        seekbar.setOnSeekBarChangeListener(this);
        ((RadioGroup) findViewById(R.id.TeamsRadioGroup))
                .setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.MoreTeamRadioButton))
                .setOnClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.newGameFAB);
        fab.setOnClickListener(this);

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
            validateTeamMe_Start();
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
        if (item.getItemId() == R.id.AboutItem) {
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

    private void teamMe() {
        int teamCount = getTeamCount();
        String sport = ((AutoCompleteTextView) findViewById(R.id.SportTextView)).getText().toString().trim();
        if (getFacade().getPlayers().isEmpty()) {
            // get things started - because we know no players
            TeamReactor.decideTeams(teamCount, playerCount);
            Intent callChooser = new Intent(getApplicationContext(),
                    TeamChooser.class);
            callChooser.putExtra(Flags.SPORT, sport);
            startActivity(callChooser);
        } else {
            // we already know some player names, let's offer a preselection!
            Intent playerPreselection = new Intent(getApplicationContext(),
                    PlayerSelectionActivity.class);
            playerPreselection.putExtra(Flags.TEAMCOUNT, teamCount);
            playerPreselection.putExtra(Flags.PLAYERCOUNT, playerCount);
            playerPreselection.putExtra(Flags.SPORT, sport);
            startActivity(playerPreselection);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
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
        getFacade().getObjDB_API().close();
        super.onDestroy();
    }

    @Override
    public void onProgressChanged(HoloCircleSeekBar seekBar, int progress,
                                  boolean fromUser) {
        playerCount = progress;
        validateTeamMe_Start();
    }

    @Override
    public void onItemSelected(String id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        validateTeamMe_Start();
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

    public void validateTeamMe_Start() {
        AutoCompleteTextView sportTextView = ((AutoCompleteTextView) findViewById(R.id.SportTextView));
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(sportTextView.getWindowToken(), 0);

        boolean valid = playerCount >= getTeamCount() && !sportTextView.getText().toString().trim().isEmpty();
        if (valid) {

            // previously invisible view
            View myView = findViewById(R.id.newGameFAB);
            if (myView.getVisibility() != View.VISIBLE) {
                // create the animator for this view (the start radius is zero)
                Animator anim = null;
                anim = reveal(myView);
                // make the view visible and start the animation
                myView.setVisibility(View.VISIBLE);
                if (anim != null) {
                    anim.start();
                }
            }
        } else {
            if (findViewById(R.id.newGameFAB).getVisibility() == View.VISIBLE) {
                if (!hide()) {
                    findViewById(R.id.newGameFAB).setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean hide() {
        // previously visible view
        final View myView = findViewById(R.id.newGameFAB);

        // get the center for the clipping circle
        int cx = myView.getWidth() / 2;
        int cy = myView.getHeight() / 2;

        // get the initial radius for the clipping circle
        float initialRadius = (float) Math.hypot(cx, cy);

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                myView.setVisibility(View.INVISIBLE);
            }
        });

        // start the animation
        anim.start();
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Animator reveal(View myView) {
        Animator anim;// get the center for the clipping circle
        int cx = myView.getWidth() / 2;
        int cy = myView.getHeight() / 2;

        // get the final radius for the clipping circle
        float finalRadius = (float) Math.hypot(cx, cy);
        anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
        return anim;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == (R.id.newGameFAB)) {
            teamMe();
        } else if (v.getId() == (R.id.MoreTeamRadioButton)) {

            if (teamCount <= 4) {
                // more players? if selected start with 4 - getting everything prepared nice and smoothly
                teamCount = 4;
                ((RadioButton) findViewById(R.id.MoreTeamRadioButton)).setText(String.valueOf(teamCount));
                validateTeamMe_Start();
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
                    validateTeamMe_Start();
                }
            });
            builder.show();
        }
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE
                || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            validateTeamMe_Start();
            return true;
        }
        return false;
    }
}
