package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.databinding.ActivityGameCreatorBinding;
import de.pasligh.android.teamme.objects.GameRecord;
import de.pasligh.android.teamme.objects.Player;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.objects.Score;
import de.pasligh.android.teamme.tools.AnimationHelper;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.PlayerSelectionRV_Adapter;
import de.pasligh.android.teamme.tools.PlayerSelectionRV_Interface;
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
        GameCreatorFragment.Callbacks, OnCheckedChangeListener, OnClickListener, PlayerSelectionRV_Interface {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private int teamCount = 4;
    private ActionBarDrawerToggle drawerToggle;

    public SharedPreferences.OnSharedPreferenceChangeListener getPreferenceChangeListener() {
        if (preferenceChangeListener == null) {
            preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    switchToDarkMode(sharedPreferences, true);
                }
            };
        }
        return preferenceChangeListener;
    }

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private NumberPicker playerCountNP;
    private NumberPicker teamCountNP;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private Snackbar barDisplayed;
    private PlayerSelectionRV_Adapter playerSelectionRV_adapter;
    private ArrayAdapter<String> teamsAdapter;
    private BackendFacade facade;

    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getApplicationContext());
        }
        return facade;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TTS_Tool.getInstance(this);
        setContentView(R.layout.activity_game_list);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        ActivityGameCreatorBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_game_creator);
        setContentView(R.layout.activity_game_creator);

        final ArrayAdapter<String> adapterAutoComplete = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, getFacade()
                .getSports());

        final AutoCompleteTextView sportTV = (AutoCompleteTextView) findViewById(R.id.SportTextView);
        sportTV.setAdapter(adapterAutoComplete);

        playerCountNP = findViewById(R.id.PlayerCountNumberPicker);
        playerCountNP.setMinValue(1);
        playerCountNP.setMaxValue(Flags.MAXPLAYERS);
        playerCountNP.setWrapSelectorWheel(true);
        playerCountNP.setValue(4);
        playerCountNP.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                validateButtons();
            }
        });


        teamCountNP = findViewById(R.id.TeamCountNumberPicker);
        teamCountNP.setMinValue(2);
        teamCountNP.setMaxValue(Flags.MAXTEAMS);
        teamCountNP.setWrapSelectorWheel(true);
        teamCountNP.setValue(2);
        teamCountNP.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                validateButtons();
                repopulateTeamsSpinner();
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.newGameFAB);
        fab.setOnClickListener(this);

        teamsAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_dropdown_item, getTeamsList());

        final Map<String, Integer> mapStarsPerPlayer = new HashMap<String, Integer>();

        RecyclerView rv = (RecyclerView) findViewById(R.id.playerSelectionRV2);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);

        playerSelectionRV_adapter = new PlayerSelectionRV_Adapter(assemblePlayerAssignments(mapStarsPerPlayer), Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf"), teamsAdapter, this, mapStarsPerPlayer);
        rv.setAdapter(playerSelectionRV_adapter);
        binding.setPlayerAssignments(playerSelectionRV_adapter.getAssignments());
        if (playerSelectionRV_adapter.getAssignments().isEmpty()) {
            findViewById(R.id.playerSelectionBlankTV2).setVisibility(View.VISIBLE);
        }

        initView();

        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.app_name));
            setSupportActionBar(toolbar);
        }

        Log.i(Flags.LOGTAG, "MANUFACTURER: " + Build.MANUFACTURER.toUpperCase());
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Flags.AUTOSHAKE = sharedPref.getBoolean(Flags.AUTOSHAKE_PREFERENCES, Build.MANUFACTURER.toUpperCase().startsWith(Flags.MOTOROLA));
        sharedPref.edit().putBoolean(Flags.AUTOSHAKE_PREFERENCES, Flags.AUTOSHAKE);
        switchToDarkMode(sharedPref, false);

        sharedPref.registerOnSharedPreferenceChangeListener(getPreferenceChangeListener());

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

        sportTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                    long id) {
                sportEntered(mapStarsPerPlayer);
            }
        });

        sportTV.setOnEditorActionListener(new TextView.OnEditorActionListener() {


            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Identifier of the action. This will be either the identifier you supplied,
                // or EditorInfo.IME_NULL if being called due to the enter key being pressed.
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    sportEntered(mapStarsPerPlayer);
                    return false;
                }
                // Return true if you have consumed the action, else false.
                return false;
            }


        });

        //Setting the actionbarToggle to drawer layout
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        displayPreselected();
        TeamReactor.resetReactor();
        if (null != getIntent().getExtras()) {
            sportTV.setText(getIntent().getStringExtra(Flags.SPORT));
        }
    }

    private void switchToDarkMode(SharedPreferences sharedPref, boolean bolRecreate) {
        final boolean before = Flags.DARKMODE_FORCE;
        Flags.DARKMODE_FORCE = sharedPref.getBoolean(Flags.DARKMODE_PREFERENCES, false);

        if (before != Flags.DARKMODE_FORCE) {

            if (Flags.DARKMODE_FORCE) {
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }

            Log.i(Flags.LOGTAG, "Darkmode Force is " + Flags.DARKMODE_FORCE);
            if (bolRecreate) {
                getDelegate().applyDayNight();
                recreate();
            }
        }
    }

    private void sportEntered(Map<String, Integer> mapStarsPerPlayer) {
        if (playerSelectionRV_adapter.getAssignments() != null) {
            mapStarsPerPlayer.clear();
            playerSelectionRV_adapter.getMapStarsPerPlayer().clear();
            assemblePlayerAssignments(mapStarsPerPlayer);
            playerSelectionRV_adapter.getMapStarsPerPlayer().putAll(mapStarsPerPlayer);
            playerSelectionRV_adapter.notifyDataSetChanged();
        }
    }

    @NonNull
    private List<String> getTeamsList() {
        List<String> list = new ArrayList<String>();
        list.add(getString(R.string.random));
        for (int i = 1; i <= teamCountNP.getValue(); i++) {
            list.add(getString(R.string.team) + " " + i);
        }
        return list;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.player_selection, menu);
        MenuItem item = menu.findItem(R.id.PlayerSelectionDeleteSelection);
        item.setVisible(!playerSelectionRV_adapter.getAssignmentsDone().isEmpty());
        return true;
    }


    public void displayPreselected() {
        String display = playerSelectionRV_adapter.getAssignmentsDone().size() + " " + getString(R.string.player) + " " + getString(R.string.preselected);
        ((TextView) findViewById(R.id.PreSelectionTextView)).setText(display.toUpperCase(Locale.getDefault()));
        invalidateOptionsMenu();
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
                                                                          alertDialog.setMessage("created by Thomas Pasligh.\nSoftware tested by Mingo.");
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.PlayerSelectionDeleteSelection:
                for (PlayerAssignment pa : playerSelectionRV_adapter.getAssignmentsDone()) {
                    pa.setRevealed(false);
                }
                playerSelectionRV_adapter.notifyDataSetChanged();
                displayPreselected();
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return true;
    }


    @NonNull
    private List<PlayerAssignment> assemblePlayerAssignments(Map<String, Integer> mapStarsPerPlayer) {
        Map<String, Integer> mapPointsPerPlayer = new HashMap<String, Integer>();

        List<PlayerAssignment> blankAssignments = new ArrayList<PlayerAssignment>();
        List<Player> allPlayers = getFacade().getPlayers();

        String sports = ((TextView) findViewById(R.id.SportTextView)).getText().toString().trim();
        List<GameRecord> gameRecords;
        if (!sports.isEmpty()) {
            gameRecords = getFacade().getGames(sports);
        } else {
            gameRecords = getFacade().getGames();
        }

        Integer maxScore = null;
        Integer minScore = null;
        // let's figure out how the players performed
        for (
                GameRecord g : gameRecords) {
            List<PlayerAssignment> assignemntsForGame = getFacade().getAssignments(g.getId());

            for (Score s : getFacade().getScores(g.getId())) {
                for (PlayerAssignment assignment : assignemntsForGame) {
                    if (s.getTeamNr() == assignment.getTeam()) {
                        Integer pointsPerPlayer = mapPointsPerPlayer.get(assignment.getPlayer().getName());
                        int newScore = s.getScoreCount();
                        if (maxScore == null || newScore > maxScore) {
                            maxScore = newScore;
                        }
                        if (minScore == null || newScore < minScore) {
                            minScore = newScore;
                        }

                        if (null != pointsPerPlayer) {
                            if (newScore > pointsPerPlayer) {
                                mapPointsPerPlayer.put(assignment.getPlayer().getName(), (newScore + pointsPerPlayer) / 2);
                            }
                        } else {
                            mapPointsPerPlayer.put(assignment.getPlayer().getName(), newScore);
                        }
                    }
                }
            }
        }

        //now let's rate the players
        if (minScore != null && maxScore != null) {
            int oneStar = (maxScore - minScore) / Flags.MAXSTARS;
            for (String playerName : mapPointsPerPlayer.keySet()) {
                if (oneStar != 0) {
                    int scored = mapPointsPerPlayer.get(playerName);
                    scored -= minScore;
                    mapStarsPerPlayer.put(playerName, Math.min(Flags.MAXSTARS, scored / oneStar));
                } else {
                    mapStarsPerPlayer.put(playerName, Flags.MAXSTARS / 2);
                }
            }
        }

        for (
                Player p : allPlayers) {
            PlayerAssignment assignBlank = new PlayerAssignment();
            assignBlank.setPlayer(p);
            blankAssignments.add(assignBlank);
        }

        return blankAssignments;
    }

    @Override
    protected void onResume() {
        super.onResume();
        playerSelectionRV_adapter.notifyDataSetChanged();
        validateButtons();
        showSnackbar(); // must be executed at the end of this method
    }

    private void showSnackbar() {
        barDisplayed = null;
        final GameRecord lastGameRecord = getFacade().getLastGamePlayed();
        if (null != lastGameRecord) {
            try {
                ((TextView) findViewById(R.id.SportTextView)).setText(getFacade().getLastGamePlayed().getSport().trim());
                int snackbarShowlenght = Snackbar.LENGTH_INDEFINITE;
                String caption = getString(R.string.log);
                List<Score> scoreList = getFacade().getScores(lastGameRecord.getId());
                if (scoreList != null && !scoreList.isEmpty()) {
                    snackbarShowlenght = Snackbar.LENGTH_LONG;
                    caption = getString(R.string.log_complete);
                }
                java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                barDisplayed = Snackbar
                        .make(findViewById(R.id.gameCreatorCL), lastGameRecord.getSport() + " " + dateFormat.format(lastGameRecord.getStartedAt()), snackbarShowlenght)
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
            } finally {
                ((TextView) findViewById(R.id.SportTextView)).requestFocus();
            }
        }
    }

    private void teamMe() {
        if (validateButtons()) {
            int teamCount = teamCountNP.getValue();
            String sport = ((AutoCompleteTextView) findViewById(R.id.SportTextView)).getText().toString().trim();
            if (sport.isEmpty()) {
                sport = getString(R.string.unknown);
            }

            String conflicts = TeamReactor.decideTeams(teamCount, playerCountNP.getValue(), playerSelectionRV_adapter.getAssignmentsDone());
            if (!conflicts.isEmpty()) {
                Toast.makeText(getApplicationContext(), conflicts + "\n..." + getString(R.string.forceMove), Toast.LENGTH_LONG).show();
            }
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.newGameFAB);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(fab,
                    (int) fab.getX(),
                    (int) fab.getY(),
                    (int) fab.getWidth(),
                    (int) fab.getHeight());


            if (playerSelectionRV_adapter.getAssignmentsDone().size() < TeamReactor.getAssignments().size()) {
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

    @Override
    protected void onDestroy() {
        try {
            TTS_Tool.getInstance(this).shutdown();
        } catch (Exception e) {
            Log.i(Flags.LOGTAG, e.getLocalizedMessage());
        }
        getFacade().getObjDB_API().close();
        super.onDestroy();
    }

    @Override
    public void onItemSelected(String id) {
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (null != teamsAdapter) {
            repopulateTeamsSpinner();
            validateButtons();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * Validate UI
     *
     * @return ready for decisions
     */
    public boolean validateButtons() {
        boolean validTeamMeStart = playerCountNP.getValue() >= teamCountNP.getValue();
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
        }

    }

    public void repopulateTeamsSpinner() {
        teamsAdapter.clear();
        teamsAdapter.addAll(getTeamsList());
    }

    @Override
    public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
        PlayerSelectionRV_Adapter.PlayerViewHolder pvh = (PlayerSelectionRV_Adapter.PlayerViewHolder) buttonView.getTag();
        PlayerAssignment pa = playerSelectionRV_adapter.getAssignments().get(pvh.getAdapterPosition());
        if (isChecked && !pa.isRevealed()) {
            PlayerSelectionRV_Adapter.expandView(pvh);
        } else if (!isChecked && pa.isRevealed()) {
            PlayerSelectionRV_Adapter.collapseView(pvh);
        }
        // otherwise it turns out to be confusing to take over the player count
        if (playerSelectionRV_adapter.getAssignmentsDone().size() > playerCountNP.getValue()) {
            playerCountNP.setValue(playerSelectionRV_adapter.getAssignmentsDone().size());
            validateButtons();
        }

        displayPreselected();
    }

    @Override
    public boolean onLongClick(View v) {
        ((Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE)).vibrate(400);
        PlayerSelectionRV_Adapter.PlayerViewHolder pvh = (PlayerSelectionRV_Adapter.PlayerViewHolder) v.getTag();
        int position = pvh.getAdapterPosition();
        final PlayerAssignment contact = playerSelectionRV_adapter.getAssignments().get(position);
        Intent intent = new Intent(getApplicationContext(), PlayerDetailActivity.class);
        intent.putExtra(PlayerDetailFragment.ARG_ITEM_ID, contact.getPlayer().getName());
        startActivity(intent);
        return true;
    }
}
