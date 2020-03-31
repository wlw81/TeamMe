package de.pasligh.android.teamme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.GameRecord;
import de.pasligh.android.teamme.objects.Player;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.TeamReactor;
import de.pasligh.android.teamme.tools.TextHelper;

/**
 * A fragment representing a single GameRecord detail screen.
 * This fragment is either contained in a {@link GameRecordListActivity}
 * in two-pane mode (on tablets) or a {@link GameRecordDetailActivity}
 * on handsets.
 */
public class GameRecordDetailFragment extends Fragment {

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_GAME_ID = "item_id";
    private static Context applicationContext;

    private BackendFacade facade;

    public void setApplicationContext(Context p_context) {
        if (p_context != null) {
            this.applicationContext = p_context;
        }
    }

    public Context getApplicationContext() {
        return applicationContext;
    }

    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getApplicationContext());
        }
        return facade;
    }


    /**
     * The record content this fragment is presenting.
     */
    private GameRecord mItem;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GameRecordDetailFragment() {
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.team_overview, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.TeamOverviewShareContext);

        // Fetch and store ShareActionProvider
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setShareIntent(createShareIntent());
    }

    private Intent createShareIntent() {
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        StringBuilder shareText = new StringBuilder();
        String title = mItem.getSport() + " " + dateFormat.format(mItem.getStartedAt());

        shareText.append("[").append(title).append("] ");
        shareText.append(TextHelper.createTeamDecided_ShareText(getString(R.string.shareIntent), getString(R.string.team)));

        // create app footer
        TextHelper.appendFooter_Signature(shareText, getString(R.string.shareFooter));

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.decisiontext);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString().trim());
        return shareIntent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments().containsKey(ARG_GAME_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = getFacade().getGame(Integer.parseInt(getArguments().getString(ARG_GAME_ID)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.gamerecord_detail, container, false);

        if (mItem != null) {
            final Set<PlayerAssignment> readAssignments = new HashSet<>();
            for (PlayerAssignment p : getFacade().getAssignments((int) mItem.getId())) {
                p.setRevealed(true);
                readAssignments.add(p);
            }

            TeamReactor.overwriteAssignments(readAssignments);

            // Get the ViewPager and set it's PagerAdapter so that it can display items
            final ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.gamerecordDetailVP);
            viewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));
            // Give the TabLayout the ViewPager
            final TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.sliding_tabs);
            tabLayout.setupWithViewPager(viewPager);

            rootView.findViewById(R.id.gamerecordDetailFAB).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent reportScores = new Intent(getApplicationContext(),
                            ReportScoresActivity.class);
                    reportScores.putExtra(Flags.GAME_ID, mItem.getId());
                    startActivity(reportScores);
                }
            });
            rootView.findViewById(R.id.gamerecordDetailAddPlayerFAB).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    View viewInflated = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_player_input, (ViewGroup) rootView, false);
                    // Set up the input
                    final AutoCompleteTextView input = (AutoCompleteTextView) viewInflated.findViewById(R.id.PlayerInputTV);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setView(viewInflated);
                    builder.setTitle(getString(R.string.addPlayer));

                    // Set up the input
                    ArrayAdapter<String> playerAdapter = new ArrayAdapter<String>(getContext(),
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
                                assignmentNew.setGame(mItem.getId());
                                assignmentNew.setTeam(tabLayout.getSelectedTabPosition()); // todo check here
                                assignmentNew.setPlayer(new Player(m_Text));
                                assignmentNew.setRevealed(true);
                                try {
                                    getFacade().persistPlayer(assignmentNew.getPlayer());
                                } catch (Exception e) {
                                    Log.d(Flags.LOGTAG, assignmentNew.getPlayer() + " already known.");
                                }

                                for (PlayerAssignment pa : readAssignments) {
                                    if (pa.getPlayer().getName().equals(assignmentNew.getPlayer().getName())) {
                                        readAssignments.remove(pa);
                                        return;
                                    }
                                }

                                /**
                                 * todo Needs to be coded
                                 */

                                getFacade().addPlayerAssignment(assignmentNew);
                                mItem.getAssignments().add(assignmentNew);
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


                }
            });

        }
        return rootView;
    }

}