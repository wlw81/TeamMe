package de.pasligh.android.teamme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.GameRecord;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.ShareHelper;
import de.pasligh.android.teamme.tools.TeamReactor;

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
    public static final String ARG_ITEM_ID = "item_id";
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = getFacade().getGame(Integer.parseInt(getArguments().getString(ARG_ITEM_ID)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(Flags.LOGTAG, "onResume: "+mItem.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.gamerecord_detail, container, false);
        if (mItem != null) {

            rootView.findViewById(R.id.gamerecordDetailFAB).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent reportScores = new Intent(getApplicationContext(),
                            ReportScoresActivity.class);
                    reportScores.putExtra(Flags.GAME_ID, mItem.getId());
                    startActivity(reportScores);
                }
            });

            Set<PlayerAssignment> readAssignments = new HashSet<>();
            for (PlayerAssignment p : getFacade().getAssignments((int) mItem.getId())) {
                p.setRevealed(true);
                readAssignments.add(p);
            }

            TeamReactor.overwriteAssignments(readAssignments);

            // Get the ViewPager and set it's PagerAdapter so that it can display items
            ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
            SectionsPagerAdapter sadapter
                    = new SectionsPagerAdapter(getChildFragmentManager());
            viewPager.setAdapter(sadapter);
            // Give the TabLayout the ViewPager
            TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.sliding_tabs);
            tabLayout.setupWithViewPager(viewPager);


        }
        return rootView;
    }

}