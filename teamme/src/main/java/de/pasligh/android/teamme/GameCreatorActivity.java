package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.GameRecord;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.objects.Score;
import de.pasligh.android.teamme.tools.AnimationHelper;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.HoloCircleSeekBar;
import de.pasligh.android.teamme.tools.HoloCircleSeekBar.OnCircleSeekBarChangeListener;
import de.pasligh.android.teamme.tools.TTS_Tool;
import de.pasligh.android.teamme.tools.TeamReactor;

import static de.pasligh.android.teamme.R.string.player;
import static de.pasligh.android.teamme.R.string.playercount;

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
        GameCreatorFragment.Callbacks, OnCircleSeekBarChangeListener, OnCheckedChangeListener, OnClickListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private int playerCount;
    private int teamCount = 4;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private Snackbar barDisplayed;
    private final List<PlayerAssignment> assignmentsDone = new ArrayList<>();

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

        if (findViewById(R.id.game_statistics_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            GameStatisticsFragment fragment = new GameStatisticsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.game_statistics_container, fragment).commit();
        } else {
            setContentView(R.layout.activity_game_creator);
        }


        TeamReactor.resetReactor();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, getFacade()
                .getSports());

        HoloCircleSeekBar seekbar = ((HoloCircleSeekBar) findViewById(R.id.PlayerPicker));
        seekbar.setOnSeekBarChangeListener(this);
        ((RadioGroup) findViewById(R.id.TeamsRadioGroup))
                .setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.MoreTeamRadioButton))
                .setOnClickListener(this);

        final AutoCompleteTextView playerNameTextView = (AutoCompleteTextView) findViewById(R.id.SportTextView);
        playerNameTextView.setAdapter(adapter);
        TextWatcher tw = new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // you can check for enter key here
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateButtons(false);
            }
        };

        playerNameTextView.addTextChangedListener(tw);
        playerNameTextView.setOnKeyListener(new View.OnKeyListener() {

                                                @Override
                                                public boolean onKey(View v, int keyCode, KeyEvent event) {
                                                    if ((event.getAction() == KeyEvent.ACTION_DOWN)
                                                            && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                                        validateButtons(true);
                                                        playerNameTextView.clearFocus();
                                                    }
                                                    return false;
                                                }
                                            }

        );


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.newGameFAB);
        fab.setOnClickListener(this);

        ToggleButton btn = (ToggleButton) findViewById(R.id.preSelectionButton);
        btn.setOnClickListener(this);

        RadioButton rab = (RadioButton) findViewById(R.id.TwoTeamRadioButton);
        rab.setChecked(true);

        playerCount = 4;

        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Condensed.ttf");
        ((TextView)

                findViewById(R.id.NewGameLabelTextView)

        ).

                setTypeface(tf);

        initView();

        if (toolbar != null)

        {
            toolbar.setTitle(getString(R.string.app_name));
            setSupportActionBar(toolbar);
        }

        // Initializing Drawer Layout and ActionBarToggle
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.about, R.string.action_settings) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).

                addApi(AppIndex.API)

                .

                        build();

    }


    private void initView() {

        toolbar = (Toolbar) findViewById(R.id.gameCreatorTB);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.gameCreatorDL);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.getMenu().getItem(0).setVisible(!mTwoPane);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                                                              @Override
                                                              public boolean onNavigationItemSelected(MenuItem menuItem) {
                                                                  menuItem.setChecked(true);
                                                                  mDrawerLayout.closeDrawers();
                                                                  switch (menuItem.getItemId()) {

                                                                      case R.id.AboutItem:
                                                                          AlertDialog alertDialog = new AlertDialog.Builder(GameCreatorActivity.this).create();

                                                                          String version = "";
                                                                          try {
                                                                              PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                                                              version = pInfo.versionName;
                                                                          } catch (PackageManager.NameNotFoundException e) {
                                                                              version = "unknown";
                                                                          }

                                                                          alertDialog.setTitle(getString(R.string.app_name) + " " + version);
                                                                          alertDialog.setMessage("by Thomas Pasligh");
                                                                          alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                                                                  new DialogInterface.OnClickListener() {
                                                                                      public void onClick(DialogInterface dialog, int which) {
                                                                                      }
                                                                                  });
                                                                          alertDialog.setIcon(R.drawable.ic_launcher);
                                                                          alertDialog.show();
                                                                          return true;
                                                                      case R.id.Jump2GameItem:
                                                                          Intent recordsActivity = new Intent(getApplicationContext(),
                                                                                  GameRecordListActivity.class);
                                                                          startActivity(recordsActivity);
                                                                          return true;
                                                                      case R.id.PlayerItem:
                                                                          Intent playerActivity = new Intent(getApplicationContext(),
                                                                                  PlayerListActivity.class);
                                                                          startActivity(playerActivity);
                                                                          return true;
                                                                      case R.id.SettingsItem:

                                                                          Intent settingsActivity = new Intent(getApplicationContext(),
                                                                                  SettingsActivity.class);
                                                                          startActivity(settingsActivity);
                                                                          return true;

                                                                      case R.id.StatisticsItem:
                                                                          Intent statisticsActivity = new Intent(getApplicationContext(),
                                                                                  GameStatisticsActivity.class);
                                                                          startActivity(statisticsActivity);
                                                                          return true;

                                                                      default:
                                                                          return true;
                                                                  }
                                                              }
                                                          }

        );

    }

    @Override
    protected void onResume() {
        super.onResume();
        showSnackbar();
        validateButtons(false);
        ToggleButton btn = (ToggleButton) findViewById(R.id.preSelectionButton);
        btn.setChecked(!assignmentsDone.isEmpty());
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                if (findViewById(R.id.TwoTeamRadioButton).isFocused()) {
                    findViewById(R.id.TwoTeamRadioButton).setSelected(true);
                } else if (findViewById(R.id.ThreeTeamRadioButton).isFocused()) {
                    findViewById(R.id.ThreeTeamRadioButton).setSelected(true);
                }
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void showSnackbar() {
        barDisplayed = null;
        final GameRecord lastGameRecord = getFacade().getLastGamePlayed();
        if (null != lastGameRecord) {
            try {
                int snackbarShowlenght = Snackbar.LENGTH_INDEFINITE;
                String caption = getString(R.string.log);
                List<Score> scoreList = getFacade().getScores(lastGameRecord.getId());
                if (scoreList != null && !scoreList.isEmpty()) {
                    snackbarShowlenght = Snackbar.LENGTH_LONG;
                    caption = getString(R.string.log_complete);
                }
                java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                barDisplayed = Snackbar
                        .make(findViewById(R.id.GameCreatorIncludeLayout), lastGameRecord.getSport() + " " + dateFormat.format(lastGameRecord.getStartedAt()), snackbarShowlenght)
                        .setAction(caption, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent reportScores = new Intent(getApplicationContext(),
                                        ReportScoresActivity.class);
                                reportScores.putExtra(Flags.GAME_ID, lastGameRecord.getId());
                                startActivity(reportScores);
                            }
                        });
                barDisplayed.show();
            } catch (Exception e) {
                Log.e(Flags.LOGTAG, "Snackbar error: " + e.getMessage());
            }
        }
    }

    private void teamMe() {
        if (validateButtons(true)) {
            int teamCount = getTeamCount();
            String sport = ((AutoCompleteTextView) findViewById(R.id.SportTextView)).getText().toString().trim();
            String conflicts = TeamReactor.decideTeams(teamCount, playerCount, assignmentsDone);
            if (!conflicts.isEmpty()) {
                Toast.makeText(getApplicationContext(), conflicts + "\n..." + getString(R.string.forceMove), Toast.LENGTH_LONG).show();
            }
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.newGameFAB);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(fab,
                    (int) fab.getX(),
                    (int) fab.getY(),
                    (int) fab.getWidth(),
                    (int) fab.getHeight());


            if (assignmentsDone.size() < TeamReactor.getAssignments().size()) {
                Intent callChooser = new Intent(getApplicationContext(),
                        TeamChooserActivity.class);
                callChooser.putExtra(Flags.SPORT, sport);
                callChooser.putExtra(Flags.TEAMCOUNT, teamCount);
                ActivityCompat.startActivity(this, callChooser, options.toBundle());
            } else {
                GameRecord saveGameRecord = new GameRecord(TeamReactor.getAssignments());
                saveGameRecord.setSport(sport);
                long id = getFacade().persistGame(saveGameRecord);
                Intent callOverview = new Intent(getApplicationContext(),
                        GameRecordListActivity.class);
                callOverview.putExtra(Flags.GAME_ID, id);
                callOverview.putExtra(Flags.TEAMCOUNT, teamCount);
                ActivityCompat.startActivity(this, callOverview, options.toBundle());
            }
        }
    }

    private void preselection() {
        // we already know some player names, let's offer a preselection!
        Intent playerPreselection = new Intent(getApplicationContext(),
                PlayerSelectionActivity.class);
        String sport = ((AutoCompleteTextView) findViewById(R.id.SportTextView)).getText().toString().trim();
        playerPreselection.putExtra(Flags.TEAMCOUNT, teamCount);
        playerPreselection.putExtra(Flags.PLAYERCOUNT, playerCount);
        playerPreselection.putExtra(Flags.SPORT, sport);
        startActivityForResult(playerPreselection, Flags.PRESELECTION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == Flags.PRESELECTION_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                assignmentsDone.clear();

                if (null != data.getExtras()) {
                    for (String key : data.getExtras().keySet()) {
                        Object value = data.getExtras().get(key);
                        Log.d(Flags.LOGTAG, String.format("%s %s (%s)", key,
                                value.toString(), value.getClass().getName()));
                        assignmentsDone.add((PlayerAssignment) value);
                    }
                }

                // otherwise it turns out to be confusing to take over the player count
                if(assignmentsDone.size() > playerCount){
                    playerCount = assignmentsDone.size();

                    ToggleButton btn = (ToggleButton) findViewById(R.id.preSelectionButton);
                    btn.setTextOn(String.valueOf(playerCount));
                    btn.setChecked(playerCount > 0);

                    HoloCircleSeekBar holoCircleSeekBar = (HoloCircleSeekBar) findViewById(R.id.PlayerPicker);
                    holoCircleSeekBar.setProgress(playerCount);
                    validateButtons(true);
                }
            }
        }
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
        validateButtons(true);
    }

    @Override
    public void onItemSelected(String id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        validateButtons(true);
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

    /**
     * Validate UI
     *
     * @return ready for decisions
     */
    public boolean validateButtons(boolean p_hideKeyboard) {
        AutoCompleteTextView sportTextView = ((AutoCompleteTextView) findViewById(R.id.SportTextView));
        if (p_hideKeyboard) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(sportTextView.getWindowToken(), 0);
        }
        RadioGroup group = ((RadioGroup) findViewById(R.id.TeamsRadioGroup));
        boolean validPreSelection = !sportTextView.getText().toString().trim().isEmpty() && group.getCheckedRadioButtonId() >= 0;
        findViewById(R.id.preSelectionButton).setEnabled(validPreSelection);

        boolean validTeamMeStart = playerCount >= getTeamCount() && validPreSelection;
        if (validTeamMeStart) {

            if (null != barDisplayed) {
                barDisplayed.dismiss();
            }

            // previously invisible view
            View myView = findViewById(R.id.newGameFAB);
            if (myView.getVisibility() != View.VISIBLE) {
                if (AnimationHelper.reveal(myView) == null) {
                    myView.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (findViewById(R.id.newGameFAB).getVisibility() == View.VISIBLE) {
                if (!AnimationHelper.hide(findViewById(R.id.newGameFAB))) {
                    findViewById(R.id.newGameFAB).setVisibility(View.INVISIBLE);
                }
            }
        }

        return validTeamMeStart;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == (R.id.newGameFAB)) {
            teamMe();
        } else if (v.getId() == (R.id.preSelectionButton)) {
            preselection();
        } else if (v.getId() == (R.id.MoreTeamRadioButton)) {

            if (teamCount <= 4) {
                // more players? if selected start with 4 - getting everything prepared nice and smoothly
                teamCount = 4;
                ((RadioButton) findViewById(R.id.MoreTeamRadioButton)).setText(String.valueOf(teamCount));
                validateButtons(true);
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
                    validateButtons(true);
                }
            });
            builder.show();
        }
    }

}
