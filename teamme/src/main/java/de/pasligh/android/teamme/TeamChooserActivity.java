package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.GameRecord;
import de.pasligh.android.teamme.objects.Player;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.tools.AnimationHelper;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.TTS_Tool;
import de.pasligh.android.teamme.tools.TeamReactor;

public class TeamChooserActivity extends AppCompatActivity implements SensorEventListener,
        OnEditorActionListener, AnimationListener, OnClickListener {

    private SensorManager sensorManager;
    private long lastUpdate;
    private Animation animationShake1;
    private Animation animationShake2;
    private Animation animationSlideOutLeft;
    private Animation animationSlideOutRight;
    private Animation animationGlow;
    private MediaPlayer mPlayerLeft;
    private MediaPlayer mPlayerRight;
    private PlayerAssignment myAssignment;
    private BackendFacade facade;
    private boolean autoShake = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_team_chooser);
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf");
        ((TextView) findViewById(R.id.TeamCaptionTextView)).setTypeface(tf);
        ((TextView) findViewById(R.id.TeamNumberTextView)).setTypeface(tf);
        ((TextView) findViewById(R.id.TeamIntroductionTextView))
                .setTypeface(tf);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        animationShake1 = AnimationUtils.loadAnimation(this, R.anim.shake1);
        animationShake2 = AnimationUtils.loadAnimation(this, R.anim.shake2);
        animationSlideOutLeft = AnimationUtils.loadAnimation(this, R.anim.left);
        animationSlideOutRight = AnimationUtils.loadAnimation(this,
                R.anim.right);

        animationShake2.setAnimationListener(this);
        mPlayerLeft = MediaPlayer.create(getApplicationContext(), R.raw.left);
        mPlayerRight = MediaPlayer.create(getApplicationContext(), R.raw.right);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, getFacade()
                .getPlayersAsStringArray());

        AutoCompleteTextView playerNameTextView = (AutoCompleteTextView) findViewById(R.id.PlayerNameAutoCompleteTextView);
        playerNameTextView.setHint(getString(R.string.player) + " #"
                + (TeamReactor.getAssignmentsRevealed() + 1));
        playerNameTextView.setOnEditorActionListener(this);
        playerNameTextView.setAdapter(adapter);

        findViewById(R.id.NextPlayerButton).setOnClickListener(this);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        autoShake = sharedPref.getBoolean("autoshake", true);
        playerNameTextView.requestFocus();
    }


    @Override
    public void onBackPressed() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(
                TeamChooserActivity.this);
        builder.setMessage(R.string.cancelDialog_question)
                .setPositiveButton(R.string.cancelDialog_positive,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent backHomne = new Intent(getApplicationContext(),
                                        GameCreatorActivity.class);
                                startActivity(backHomne);
                            }
                        })
                .setNegativeButton(R.string.cancelDialog_negative,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                showSoftkeyboard_if_needed();
                            }
                        });
        // Create the AlertDialog object and return it
        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSensorListener();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        showSoftkeyboard_if_needed();
    }

    private void registerSensorListener() {
        if (myAssignment == null) {
            AutoCompleteTextView playerNameTextview = ((AutoCompleteTextView) findViewById(R.id.PlayerNameAutoCompleteTextView));
            if (!playerNameTextview.getText().toString().isEmpty()
                    && !checkIfAlreadyAssigned()) {
                startShakeCall();
                sensorManager.registerListener(this, sensorManager
                                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = System.currentTimeMillis();
        if (accelationSquareRoot >= 2) //
        {
            if (x < 0) {
                findViewById(R.id.BumperLeftDrawable).startAnimation(
                        animationShake1);
                mPlayerLeft.start();
            } else {
                findViewById(R.id.BumperRightDrawable).startAnimation(
                        animationShake2);
                mPlayerRight.start();
                vibrate();
            }
            if (actualTime - lastUpdate < 200) {
                return;
            }

            Log.d(Flags.LOGTAG, "Bewegung nach " + x + "-" + y + "-" + z);
            acceptPlayername();
            lastUpdate = actualTime;
            findViewById(R.id.PlayerNameAutoCompleteTextView).setEnabled(false);
            stopShakeCall();
        }
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(50);
    }

    private void stopShakeCall() {
        findViewById(R.id.ShakeTextView).clearAnimation();
        findViewById(R.id.ShakeTextView).setVisibility(View.GONE);
    }

    private void acceptPlayername() {
        AutoCompleteTextView playerNameTextview = ((AutoCompleteTextView) findViewById(R.id.PlayerNameAutoCompleteTextView));
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(playerNameTextview.getWindowToken(), 0);
        if (autoShake) {
            if (playerNameTextview.getText().toString().length() > 0
                    && !checkIfAlreadyAssigned()) {
                // auto shake - directly start animations and sound effects
                vibrate();
                findViewById(R.id.BumperLeftDrawable).startAnimation(
                        animationShake1);
                mPlayerLeft.start();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        vibrate();
                        findViewById(R.id.BumperRightDrawable).startAnimation(
                                animationShake2); // ending of animation 2 will automatically lead to #onAnimationEnd
                        mPlayerRight.start();
                        stopShakeCall();
                    }
                }, 200);
            }


        } else {
            // manual shake - only if enabled by user settings
            registerSensorListener();
        }
    }

    private boolean checkIfAlreadyAssigned() {
        AutoCompleteTextView v = ((AutoCompleteTextView) findViewById(R.id.PlayerNameAutoCompleteTextView));
        String playerName = v.getText().toString().trim();
        for (PlayerAssignment checkAssignment : TeamReactor.getAssignments()) {
            if (null != checkAssignment.getPlayer()
                    && checkAssignment.getPlayer().getName()
                    .equalsIgnoreCase(playerName)) {
                v.setText(playerName);
                v.requestFocus();
                Toast.makeText(getApplicationContext(),
                        getString(R.string.playerAlreadyAssigned),
                        Toast.LENGTH_LONG).show();
                return true;
            }
        }

        return false;
    }

    private void startShakeCall() {
        findViewById(R.id.ShakeTextView).startAnimation(getAnimationGlow());
        findViewById(R.id.ShakeTextView).setVisibility(View.VISIBLE);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        getAccelerometer(event);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        chooseTeamAssignment();
    }

    private void chooseTeamAssignment() {
        sensorManager.unregisterListener(this);
        stopShakeCall();


        View myView = findViewById(R.id.NextPlayerButton);
        myView.setVisibility(View.INVISIBLE);

        findViewById(R.id.BumperLeftDrawable).startAnimation(
                animationSlideOutLeft);
        findViewById(R.id.BumperRightDrawable).startAnimation(
                animationSlideOutRight);
        findViewById(R.id.TeamNumberTextView).setVisibility(View.VISIBLE);
        findViewById(R.id.TeamCaptionTextView).setVisibility(View.VISIBLE);
        findViewById(R.id.TeamIntroductionTextView).setVisibility(View.VISIBLE);
        findViewById(R.id.BumperLeftDrawable).setVisibility(View.GONE);
        findViewById(R.id.BumperRightDrawable).setVisibility(View.GONE);
        findViewById(R.id.NextPlayerIncludeLayout).setVisibility(View.VISIBLE);
        if (AnimationHelper.reveal(myView) == null) {
            myView.setVisibility(View.VISIBLE);
        }

        if (null != findViewById(R.id.InputPlayerIncludeLayout)) {
            findViewById(R.id.InputPlayerIncludeLayout)
                    .setVisibility(View.GONE);
        }

        String playername = ((AutoCompleteTextView) findViewById(R.id.PlayerNameAutoCompleteTextView))
                .getText().toString().trim();

        String teamHeaderText = playername + ", "
                + getString(R.string.assignmenttext_part1) + " "
                + getMyAssignment().getOrderNumber() + " "
                + getString(R.string.assignmenttext_part2);
        String speakText = playername + " - " + getString(R.string.team) + " "
                + getMyAssignment().getTeam();
        if (getMyAssignment().getOrderNumber() == 1) {
            speakText = getString(R.string.captain) + " " + playername + " "
                    + getString(R.string.assignmenttext_captain) + " "
                    + getString(R.string.team) + " "
                    + getMyAssignment().getTeam();
        }
        ((TextView) findViewById(R.id.TeamIntroductionTextView))
                .setText(teamHeaderText);
        ((TextView) findViewById(R.id.TeamNumberTextView)).setText(String
                .valueOf(getMyAssignment().getTeam()));

        TeamReactor.getAssignments().remove(getMyAssignment());
        Player playerNew = new Player(playername);
        getMyAssignment().setPlayer(playerNew);
        TeamReactor.getAssignments().add(getMyAssignment());
        try {
            getFacade().persistPlayer(playerNew);
        } catch (Exception e) {
            Log.d(Flags.LOGTAG, playerNew + " already known.");
        }

        LinearLayout teamLayout = (LinearLayout) findViewById(R.id.AssignedPlayerLayout);
        int teamcount = getIntent().getIntExtra(Flags.TEAMCOUNT, -1);
        for (int i = 1; i <= teamcount; i++) {
            LinearLayout playerLayout = new LinearLayout(getApplicationContext());
            int padding = (int) (getResources().getDimension(R.dimen.activity_horizontal_margin)) / 2;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(0, 4, 0, 4);

            playerLayout.setPadding(padding, 0, padding, 0);
            playerLayout.setOrientation(LinearLayout.VERTICAL);
            ImageView viewTeamIcon = new ImageView(getApplicationContext());
            viewTeamIcon.setImageResource(R.drawable.team);
            teamLayout.addView(playerLayout);
            for (PlayerAssignment assignment : TeamReactor.getAssignmentsByTeam(i)) {
                if (assignment.isRevealed()) {
                    TextView tv = new TextView(getApplicationContext());
                    tv.setVisibility(View.INVISIBLE);
                    tv.setPadding(0, padding, 0, padding);
                    tv.setText(" " + assignment.getPlayer().getName() + " ");
                    tv.setTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
                    tv.setBackground(getResources().getDrawable(R.drawable.roundedcorner));
                    if (playerLayout.getChildCount() <= 0) {
                        playerLayout.addView(viewTeamIcon);
                    }

                    playerLayout.addView(tv, layoutParams);
                    if (AnimationHelper.reveal(tv) == null) {
                        tv.setVisibility(View.VISIBLE);
                    }

                }
            }
        }

        if (TeamReactor.hasAssignmentsLeft()) {
            int playersLeft = TeamReactor.getAssignments().size()
                    - TeamReactor.getAssignmentsRevealed();
            ((TextView) findViewById(R.id.GameProgressTextView))
                    .setText(playersLeft + " "
                            + getString(R.string.playersleftwithoutteam));
        } else {
            ((TextView) findViewById(R.id.GameProgressTextView))
                    .setText(getString(R.string.decisiontext));
        }

        TTS_Tool.getInstance(this)
                .sprechen(speakText, TextToSpeech.QUEUE_FLUSH);
        invalidateOptionsMenu();
    }


    @Override
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    private void completeTeams() {
        GameRecord saveGameRecord = new GameRecord(TeamReactor.getAssignments());
        saveGameRecord.setSport(getIntent().getStringExtra(Flags.SPORT));
        long id = getFacade().persistGame(saveGameRecord);
        Intent callOverview = new Intent(getApplicationContext(),
                GameRecordListActivity.class);
        int teamcount = getIntent().getIntExtra(Flags.TEAMCOUNT, -1);
        callOverview.putExtra(Flags.GAME_ID, id);
        callOverview.putExtra(Flags.TEAMCOUNT, teamcount);
        startActivity(callOverview);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE
                || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            acceptPlayername();
            return true;
        }
        return false;
    }

    /**
     * @return the facade
     */
    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getApplicationContext());
        }
        return facade;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getFacade().getObjDB_API().close();
    }

    /**
     * @return the myAssignment
     */
    public PlayerAssignment getMyAssignment() {
        if (null == myAssignment) {
            myAssignment = TeamReactor.revealNextAssignment();
        }
        return myAssignment;
    }

    /**
     * @return the animationGlow
     */
    private Animation getAnimationGlow() {
        if (null == animationGlow) {
            animationGlow = AnimationUtils.loadAnimation(this, R.anim.glow);
        }
        return animationGlow;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.NextPlayerButton) {
            if (TeamReactor.hasAssignmentsLeft()) {
                Intent callChooser = new Intent(getApplicationContext(),
                        TeamChooserActivity.class);
                callChooser.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                callChooser.putExtra(Flags.SPORT, getIntent().getStringExtra(Flags.SPORT));
                callChooser.putExtra(Flags.TEAMCOUNT, getIntent().getIntExtra(Flags.TEAMCOUNT, -1));
                startActivity(callChooser);
                overridePendingTransition(R.anim.enter_from_right, R.anim.left);
            } else {
                completeTeams();
            }
        }
    }

    public void showSoftkeyboard_if_needed() {
        AutoCompleteTextView playerNameTextView = (AutoCompleteTextView) findViewById(R.id.PlayerNameAutoCompleteTextView);
        if (playerNameTextView.isEnabled()) {
            playerNameTextView.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            playerNameTextView.selectAll();
        }
    }

}
