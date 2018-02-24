package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
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
import java.util.HashMap;
import java.util.List;
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
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        ActivityGameCreatorBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_game_creator);
        setContentView(R.layout.activity_game_creator);

        TeamReactor.resetReactor();

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

        playerSelectionRV_adapter = new PlayerSelectionRV_Adapter(getApplicationContext(), assemblePlayerAssignments(mapStarsPerPlayer), Typeface.createFromAsset(getAssets(),
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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).

                addApi(AppIndex.API)

                .

                        build();
        displayPreselected();
    }

    private void sportEntered(Map<String, Integer> mapStarsPerPlayer) {
        if (playerSelectionRV_adapter.getAssignments() != null) {
            playerSelectionRV_adapter.getAssignments().clear();
            mapStarsPerPlayer.clear();
            playerSelectionRV_adapter.getAssignments().addAll(assemblePlayerAssignments(mapStarsPerPlayer));
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
        return true;
    }


    public void displayPreselected() {
        String display = playerSelectionRV_adapter.getAssignmentsDone().size() + " " + getString(R.string.player) + " " + getString(R.string.preselected);
        ((TextView) findViewById(R.id.PreSelectionTextView)).setText(display);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.PlayerSelectionAddContext:
                View viewInflated = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_player_input, (ViewGroup) findViewById(android.R.id.content), false);
                // Set up the input
                final AutoCompleteTextView input = (AutoCompleteTextView) viewInflated.findViewById(R.id.PlayerInputTV);

                AlertDialog.Builder builder = new AlertDialog.Builder(GameCreatorActivity.this).setView(viewInflated);
                builder.setTitle(getString(R.string.addPlayer));

                // Set up the input
                ArrayAdapter<String> playerAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line, getFacade()
                        .getPlayersAsStringArray());
                input.setAdapter(playerAdapter);

                // Set up the buttons
                builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = input.getText().toString().trim();
                        if (!m_Text.isEmpty()) {
                            PlayerAssignment assignmentNew = new PlayerAssignment();
                            assignmentNew.setPlayer(new Player(m_Text));
                            try {
                                getFacade().persistPlayer(assignmentNew.getPlayer());
                            } catch (Exception e) {
                                Log.d(Flags.LOGTAG, assignmentNew.getPlayer() + " already known.");
                            }

                            boolean playerAlreadyListed = false;
                            for (PlayerAssignment pa : playerSelectionRV_adapter.getAssignments()) {
                                if (pa.getPlayer().getName().equals(assignmentNew.getPlayer().getName())) {
                                    playerAlreadyListed = true;
                                    return;
                                }
                            }

                            if (!playerAlreadyListed) {
                                playerSelectionRV_adapter.getAssignments().add(assignmentNew);
                                playerSelectionRV_adapter.notifyDataSetChanged();
                                findViewById(R.id.playerSelectionBlankTV2).setVisibility(View.GONE);
                            }
                        }
                    }
                });

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                final AlertDialog alertToShow = builder.create();
                alertToShow.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                alertToShow.show();
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
        showSnackbar();
        validateButtons();
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
            // TODO: handle exception
        }
        getFacade().getObjDB_API().close();
        super.onDestroy();
    }

    @Override
    public void onItemSelected(String id) {
        // TODO Auto-generated method stub

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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        PlayerSelectionRV_Adapter.PlayerViewHolder pvh = (PlayerSelectionRV_Adapter.PlayerViewHolder) buttonView.getTag();
        PlayerAssignment pa = playerSelectionRV_adapter.getAssignments().get(pvh.getAdapterPosition());
        if (isChecked && !pa.isRevealed()) {
            playerSelectionRV_adapter.getAssignments().get(pvh.getAdapterPosition()).setRevealed(isChecked);
            pvh.expandView();
        } else if (!isChecked && pa.isRevealed()) {
            playerSelectionRV_adapter.getAssignments().get(pvh.getAdapterPosition()).setRevealed(isChecked);
            pvh.collapseView();
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
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(
                GameCreatorActivity.this);
        builder.setMessage(getString(R.string.playerDeleteDialog_question).replace("$1", contact.getPlayer().getName()))
                .setPositiveButton(R.string.delete,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                if (getFacade().deletePlayer(contact.getPlayer().getName())) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.deleted) + ": " + contact.getPlayer().getName(), Toast.LENGTH_SHORT).show();
                                    playerSelectionRV_adapter.getAssignments().remove(contact);
                                    playerSelectionRV_adapter.notifyDataSetChanged();
                                }
                            }
                        }).setNeutralButton(R.string.merge, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(GameCreatorActivity.this);
                builderSingle.setTitle(getString(R.string.mergeTo));

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        GameCreatorActivity.this,
                        android.R.layout.simple_selectable_list_item);

                List<Player> allPlayers = getFacade().getPlayers();
                final List<Player> players = new ArrayList<Player>();

                for (Player p : allPlayers) {
                    if (!p.getName().equals(contact.getPlayer().getName())) {
                        arrayAdapter.add(p.getName());
                        players.add(p);
                    }
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
                                getFacade().mergePlayer(contact.getPlayer().getName(), players.get(which).getName());
                                Toast.makeText(getApplicationContext(), getString(R.string.deleted) + ": " + contact.getPlayer().getName(), Toast.LENGTH_SHORT).show();
                                playerSelectionRV_adapter.getAssignments().remove(contact);
                                Map<String, Integer> mapStarsPerPlayer = new HashMap<String, Integer>();
                                assemblePlayerAssignments(mapStarsPerPlayer);
                                playerSelectionRV_adapter.getMapStarsPerPlayer().clear();
                                playerSelectionRV_adapter.getMapStarsPerPlayer().putAll(mapStarsPerPlayer);
                                playerSelectionRV_adapter.notifyDataSetChanged();
                            }
                        });
                builderSingle.show();

            }
        });
        // Create the AlertDialog object and return it
        builder.create().show();
        return false;
    }
}
