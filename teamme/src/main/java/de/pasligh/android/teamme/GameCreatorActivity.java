package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.Game;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.tools.AnimationHelper;
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
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;

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
        setContentView(R.layout.activity_game_creator);

        TeamReactor.resetReactor();

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
        }

        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Condensed.ttf");
        ((TextView) findViewById(R.id.NewGameLabelTextView)).setTypeface(tf);
        initView();
        if (toolbar != null) {
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
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

                                                                          if (getFacade().getLastGamePlayed() != null) {
                                                                              AlertDialog.Builder builderSingle = new AlertDialog.Builder(GameCreatorActivity.this);
                                                                              builderSingle.setIcon(R.drawable.write);
                                                                              builderSingle.setTitle(getString(R.string.selectGame));

                                                                              final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                                                                      GameCreatorActivity.this,
                                                                                      android.R.layout.simple_selectable_list_item);
                                                                              java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                                                                              final List<Game> games = new ArrayList<>(); // so we don't have to read that later again
                                                                              for (Game g : getFacade().getGames()) {
                                                                                  List<PlayerAssignment> assignments = getFacade().getAssignments(g.getId());

                                                                                  String caption = g.getSport() + " " + dateFormat.format(g.getStartedAt());
                                                                                  if (assignments != null) {
                                                                                      caption += " (" + getFacade().getAssignments(g.getId()).size() + " " + getString(R.string.player) + ")";
                                                                                  }
                                                                                  arrayAdapter.add(caption);
                                                                                  games.add(g);
                                                                              }

                                                                              builderSingle.setNegativeButton(
                                                                                      getString(R.string.cancel),
                                                                                      new DialogInterface.OnClickListener() {
                                                                                          @Override
                                                                                          public void onClick(DialogInterface dialog, int which) {
                                                                                              dialog.dismiss();
                                                                                          }
                                                                                      });

                                                                              builderSingle.setAdapter(
                                                                                      arrayAdapter,
                                                                                      new DialogInterface.OnClickListener() {
                                                                                          @Override
                                                                                          public void onClick(DialogInterface dialog, int which) {
                                                                                              Intent reportScores = new Intent(getApplicationContext(),
                                                                                                      ReportScoresActivity.class);
                                                                                              reportScores.putExtra(Flags.GAME_ID, games.get(which).getId());
                                                                                              startActivity(reportScores);
                                                                                          }
                                                                                      });
                                                                              builderSingle.show();
                                                                          } else {
                                                                              Toast.makeText(getApplicationContext(), getString(R.string.cancel), Toast.LENGTH_SHORT).show();
                                                                          }
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

    private void teamMe() {
        if (validateTeamMe_Start()) {
            int teamCount = getTeamCount();
            String sport = ((AutoCompleteTextView) findViewById(R.id.SportTextView)).getText().toString().trim();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

            boolean preselection = sharedPref.getBoolean("preselection", true);
            if (getFacade().getPlayers().isEmpty() || !preselection) {
                // get things started - because we know no players
                TeamReactor.decideTeams(teamCount, playerCount);
                Intent callChooser = new Intent(getApplicationContext(),
                        TeamChooserActivity.class);
                callChooser.putExtra(Flags.SPORT, sport);
                callChooser.putExtra(Flags.TEAMCOUNT, teamCount);
                callChooser.putExtra(Flags.PLAYERCOUNT, playerCount);
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

    public boolean validateTeamMe_Start() {
        AutoCompleteTextView sportTextView = ((AutoCompleteTextView) findViewById(R.id.SportTextView));
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(sportTextView.getWindowToken(), 0);
        RadioGroup group = ((RadioGroup) findViewById(R.id.TeamsRadioGroup));
        boolean valid = playerCount >= getTeamCount() && !sportTextView.getText().toString().trim().isEmpty() && group.getCheckedRadioButtonId() >= 0;
        if (valid) {

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

        return valid;
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
