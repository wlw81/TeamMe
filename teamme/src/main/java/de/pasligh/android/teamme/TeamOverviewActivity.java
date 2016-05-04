package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.PredicateLayout;
import de.pasligh.android.teamme.tools.ShareHelper;
import de.pasligh.android.teamme.tools.TeamReactor;

public class TeamOverviewActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    private BackendFacade facade;
    private ShareActionProvider mShareActionProvider;
    private long gameId;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_overview);

        gameId = getIntent().getExtras().getLong(Flags.GAME_ID);

        findViewById(R.id.reportScoresFAB).setOnClickListener(this);
    }

    @Override
    protected void onResume() {

        if (gameId < 0) {
            gameId = getIntent().getIntExtra
                    (Flags.GAME_ID, -1);
        }

        Set<PlayerAssignment> readAssignments = new HashSet<>();
        for (PlayerAssignment p : getFacade().getAssignments((int) gameId)) {
            p.setRevealed(true);
            readAssignments.add(p);
        }

        if(readAssignments.isEmpty()){
            Intent backHomne = new Intent(getApplicationContext(),
                    GameCreatorActivity.class);
            startActivity(backHomne);
        }else{
            TeamReactor.overwriteAssignments(readAssignments);

            // Create the adapter that will return a fragment for each of the three
            // primary sections of the app.
            mSectionsPagerAdapter = new SectionsPagerAdapter(
                    getSupportFragmentManager());

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            super.onResume();
        }


    }

    @Override
    public void onBackPressed() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(
                TeamOverviewActivity.this);
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
              ;
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

    private Intent createShareIntent() {
        StringBuilder shareText = new StringBuilder();
        shareText.append(getString(R.string.shareIntent)).append(" ");
        int teamNr = 1;
        List<PlayerAssignment> assignments;
        while (!(assignments = TeamReactor.getAssignmentsByTeam(teamNr))
                .isEmpty()) {
            shareText.append(getString(R.string.team).toUpperCase(Locale.getDefault())).append(" ")
                    .append(assignments.get(0).getPlayer().getName().toUpperCase(Locale.getDefault()))
                    .append(": ");

            for (int i = 0; i < assignments.size(); i++) {
                shareText.append((i + 1) + ". ").append(assignments.get(i).getPlayer().getName())
                        .append(" ");
            }

            teamNr++;
        }

        // create app footer
        ShareHelper.appendFooter_Signature(shareText, getString(R.string.shareFooter));

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.decisiontext);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString().trim());
        return shareIntent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.team_overview, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.TeamOverviewShareContext);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setShareIntent(createShareIntent());
        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a TeamSectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment = new TeamSectionFragment();
            Bundle args = new Bundle();
            args.putInt(TeamSectionFragment.ARG_SECTION_NUMBER, (position + 1));
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            Iterator<PlayerAssignment> iterator = TeamReactor.getAssignments()
                    .iterator();
            Set<Integer> setTeamNr = new HashSet<Integer>();
            while (iterator.hasNext()) {
                PlayerAssignment next = iterator.next();
                setTeamNr.add(next.getTeam());
            }
            return setTeamNr.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(R.string.team) + " " + TeamReactor.getAssignmentsByTeam(position+1).get(0).getPlayer().getName();
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class TeamSectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public TeamSectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            int sectionr = (getArguments().getInt(ARG_SECTION_NUMBER));
            View rootView = inflater.inflate(R.layout.fragment_team_overview,
                    container, false);
            PredicateLayout layout = (PredicateLayout) rootView
                    .findViewById(R.id.TeamOverviewSectionLayout);
            for (PlayerAssignment assignmentToLayout : TeamReactor
                    .getAssignmentsByTeam(sectionr)) {
                RelativeLayout playerDetail = (RelativeLayout) inflater
                        .inflate(R.layout.fragment_player_detail, container,
                                false);
                TextView playerPositionTextView = (TextView) playerDetail
                        .findViewById(R.id.PlayerPositionTextView);
                TextView playerNameTextView = (TextView) playerDetail
                        .findViewById(R.id.PlayerNameTextView);

                playerPositionTextView.setText(TeamReactor.createPlayertitle(
                        getActivity().getApplicationContext(),
                        assignmentToLayout));
                playerNameTextView.setText(assignmentToLayout.getPlayer()
                        .getName());
                layout.addView(playerDetail);
            }

            return rootView;
        }
    }

    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getApplicationContext());
        }
        return facade;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == (R.id.reportScoresFAB)) {
            Intent reportScores = new Intent(getApplicationContext(),
                    ReportScoresActivity.class);
            reportScores.putExtra(Flags.GAME_ID, gameId);
            startActivity(reportScores);
        }
    }
}
