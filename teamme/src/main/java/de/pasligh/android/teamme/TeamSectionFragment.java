package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.TeamReactor;
import de.pasligh.android.teamme.tools.TeamView_Interface;

public class TeamSectionFragment extends Fragment implements View.OnClickListener {

    public static final String ARG_SECTION_NUMBER = "section_number";

    public List<TeamView_Interface> getListener() {
        if (listener == null) {
            listener = new ArrayList<TeamView_Interface>();
        }

        return listener;
    }

    public void setListener(List<TeamView_Interface> listener) {
        this.listener = listener;
    }

    private List<TeamView_Interface> listener;

    private List<PlayerAssignment> assignments;
    private LinearLayout layout;
    private BackendFacade facade;
    private int sectionNr;
    private AlertDialog alertToShow;


    public TeamSectionFragment() {
    }

    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getContext());
        }
        return facade;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sectionNr = (getArguments().getInt(ARG_SECTION_NUMBER));
        Log.d(Flags.LOGTAG, "Create View " + sectionNr);
        View rootView = inflater.inflate(R.layout.fragment_team_overview,
                container, false);
        layout = (LinearLayout) rootView
                .findViewById(R.id.TeamOverviewSectionLayout);
        assignments = TeamReactor
                .getAssignmentsByTeam(sectionNr);
        for (PlayerAssignment assignmentToLayout : assignments) {
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
            playerDetail.setOnClickListener(TeamSectionFragment.this);
            ((ImageButton) playerDetail.findViewById(R.id.PlayerDetailIcon)).setOnClickListener(TeamSectionFragment.this);
            layout.addView(playerDetail);
        }

        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof ImageButton) {
            View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_assignment_change, (ViewGroup) v.getParent(), false);
            final PlayerAssignment pa = assignments.get(layout.indexOfChild(((View) v.getParent())));
            final List<PlayerAssignment> assignmentsReOrdered = TeamReactor.getAssignmentsByTeam(sectionNr);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setView(viewInflated);
            ToggleButton btn = viewInflated.findViewById(R.id.assignmentPromoteBT);
            TextView tv = viewInflated.findViewById(R.id.assignmentNameTV);
            tv.setText(pa.getPlayer().getName());
            btn.setEnabled(assignmentsReOrdered.size() > 1);
            btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    // rearrange
                    if (isChecked) {
                        String oldCaptainName = assignmentsReOrdered.get(0).getPlayer().getName();
                        String newCaptainName = pa.getPlayer().getName();

                        assignmentsReOrdered.get(0).getPlayer().setName(newCaptainName);
                        assignmentsReOrdered.get(pa.getOrderNumber() - 1).getPlayer().setName(oldCaptainName);

                        Log.i(Flags.LOGTAG, "Team change: " + pa.getPlayer() + " is now el capitano!");
                    } else {
                        String oldCaptainName = pa.getPlayer().getName();
                        String newCaptainName = assignmentsReOrdered.get(1).getPlayer().getName();

                        assignmentsReOrdered.get(0).getPlayer().setName(newCaptainName);
                        assignmentsReOrdered.get(1).getPlayer().setName(oldCaptainName);

                        Log.i(Flags.LOGTAG, "Team change: " + pa.getPlayer() + " is not the captain anymore");
                    }

                    // and move this to the team reactor & backend
                    if (getFacade().deleteAssignments(pa.getGame(), pa.getTeam())) {
                        int orderNo = 1;
                        for (PlayerAssignment playerAssignment : assignmentsReOrdered) {
                            playerAssignment.setOrderNumber(orderNo++);
                            getFacade().addPlayerAssignment(playerAssignment);
                            Log.i(Flags.LOGTAG, "Replaced: " + pa.getPlayer());
                        }
                    }

                    TeamReactor.overwriteAssignments(new HashSet<PlayerAssignment>(getFacade().getAssignments(pa.getGame())));

                    notifyListener();
                    if (alertToShow != null) {
                        alertToShow.dismiss();
                    }
                }
            });
            btn.setChecked(pa.getOrderNumber() == 1);

            Button btnKick = viewInflated.findViewById(R.id.assignmentKillBT);
            btnKick.setEnabled(assignmentsReOrdered.size() > 1);
            btnKick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TeamReactor.getAssignments().remove(pa);
                    getFacade().deleteAssignment(pa.getGame(), pa.getPlayer().getName());
                    notifyListener();
                    alertToShow.dismiss();
                }
            });

            alertToShow = builder.create();
            alertToShow.show();

        } else {
            ((ImageButton) v.findViewById(R.id.PlayerDetailIcon)).performClick();
        }
    }

    public void notifyListener() {
        for (TeamView_Interface t_listener : getListener()) {
            t_listener.notifyAdapter();
        }
    }

}