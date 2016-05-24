package de.pasligh.android.teamme;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.tools.PredicateLayout;
import de.pasligh.android.teamme.tools.TeamReactor;

public class TeamSectionFragment extends Fragment {

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