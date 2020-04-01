package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.TeamReactor;

public class TeamSectionFragment extends Fragment implements View.OnClickListener {

    public static final String ARG_SECTION_NUMBER = "section_number";

    private List<PlayerAssignment> assignments;
    private LinearLayout layout;
    private BackendFacade facade;
    private int sectionr;


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

        sectionr = (getArguments().getInt(ARG_SECTION_NUMBER));
        Log.d(Flags.LOGTAG, "Create View " + sectionr);
        View rootView = inflater.inflate(R.layout.fragment_team_overview,
                container, false);
        layout = (LinearLayout) rootView
                .findViewById(R.id.TeamOverviewSectionLayout);
        assignments = TeamReactor
                .getAssignmentsByTeam(sectionr);
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

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setView(viewInflated);
            ToggleButton btn = viewInflated.findViewById(R.id.assignmentPromoteBT);
            TextView tv = viewInflated.findViewById(R.id.assignmentNameTV);
            tv.setText(pa.getPlayer().getName());
            btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    List<PlayerAssignment> assignmentsReOrdered = TeamReactor
                            .getAssignmentsByTeam(sectionr);
                    PlayerAssignment paReOrder = findAssignment(assignmentsReOrdered, pa.getId());

                    // rearrange
                    if (isChecked) {
                        PlayerAssignment paOldCaptain = assignmentsReOrdered.remove(0);
                        paOldCaptain.setOrderNumber(paReOrder.getOrderNumber());
                        TeamReactor.getAssignmentsByTeam(sectionr).add(paOldCaptain);

                        paReOrder.setOrderNumber(1);
                        Log.i(Flags.LOGTAG, "Team change: " + paReOrder.getPlayer() + " is now el capitano!");
                    } else {
                        paReOrder.setOrderNumber(getFacade().getNextOrderNo(pa.getGame(), paReOrder.getTeam()));
                        Log.i(Flags.LOGTAG, "Team change: " + paReOrder.getPlayer() + " is not the captain anymore");
                    }

                    // clean up to database
                    int orderNo = 1;
                    for (PlayerAssignment t : assignmentsReOrdered) {
                        if (orderNo != t.getOrderNumber()) {
                            getFacade().deleteAssignment(t.getOrderNumber(), t.getPlayer().getName());
                            t.setOrderNumber(orderNo);
                            t.setRevealed(true);
                            getFacade().addPlayerAssignment(t);
                        }
                        orderNo++;
                    }

                    // and move this to the team reactor
                    TeamReactor.overwriteAssignments(new HashSet<PlayerAssignment>(getFacade().getAssignments(pa.getGame())));
                }
            });
            btn.setChecked(pa.getOrderNumber() == 1);
            //Toast.makeText(getContext(), .getPlayer().toString(), Toast.LENGTH_SHORT).show();

            Button btnKick = viewInflated.findViewById(R.id.assignmentKillBT);
            btnKick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TeamReactor.getAssignments().remove(pa);
                    getFacade().deleteAssignment(pa.getGame(), pa.getPlayer().getName());
                }
            });

            final AlertDialog alertToShow = builder.create();
            alertToShow.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            alertToShow.show();

        } else {
            ((ImageButton) v.findViewById(R.id.PlayerDetailIcon)).performClick();
        }
    }

    private PlayerAssignment findAssignment(List<PlayerAssignment> assignmentsReOrdered, int id) {
        for (PlayerAssignment t : assignmentsReOrdered) {
            if (t.getId() == id) {
                return t;
            }
        }
    }

}