package de.pasligh.android.teamme;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import static de.pasligh.android.teamme.tools.TeamReactor.getAssignmentsByTeam;

public class TeamSectionFragment extends Fragment implements View.OnClickListener {

    public static final String ARG_SECTION_NUMBER = "section_number";

    private BottomSheetBehavior sheetBehavior;
    private LinearLayout bottom_sheet;

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
        layout = (LinearLayout) rootView.findViewById(R.id.TeamOverviewSectionLayout);
        assignments = getAssignmentsByTeam(sectionNr);

        Log.i(Flags.LOGTAG, "View " + container.getParent().toString());

        bottom_sheet = ((View) container.getParent()).findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

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
            ((TextView) playerDetail.findViewById(R.id.PlayerPositionTextView)).setOnClickListener(TeamSectionFragment.this);
            ((TextView) playerDetail.findViewById(R.id.PlayerNameTextView)).setOnClickListener(TeamSectionFragment.this);

            layout.addView(playerDetail);
        }

        return rootView;
    }

    @Override
    public void onClick(View v) {
        try {
            if ((v.getId() == R.id.PlayerDetailIcon) || (v.getId() == R.id.PlayerPositionTextView) || (v.getId() == R.id.PlayerNameTextView)) {

                final PlayerAssignment pa = assignments.get(layout.indexOfChild(((View) v.getParent())));
                final List<PlayerAssignment> assignmentsReOrdered = getAssignmentsByTeam(sectionNr);

                final Button btn = bottom_sheet.findViewById(R.id.assignmentPromoteBT);
                TextView tv = bottom_sheet.findViewById(R.id.assignmentNameTV);
                tv.setText(pa.getPlayer().getName());
                btn.setEnabled(assignmentsReOrdered.size() > 1);
                btn.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               // rearrange
                                               String oldCaptainName = assignmentsReOrdered.get(0).getPlayer().getName();
                                               String newCaptainName = pa.getPlayer().getName();

                                               assignmentsReOrdered.get(0).getPlayer().setName(newCaptainName);
                                               assignmentsReOrdered.get(pa.getOrderNumber() - 1).getPlayer().setName(oldCaptainName);

                                               // and move this to the team reactor & backend
                                               if (getFacade().deleteAssignments(pa.getGame(), pa.getTeam())) {
                                                   for (PlayerAssignment playerAssignment : assignmentsReOrdered) {
                                                       getFacade().addPlayerAssignment(playerAssignment);
                                                       Log.i(Flags.LOGTAG, "New: " + playerAssignment);
                                                   }
                                               }

                                               Log.i(Flags.LOGTAG, "Team change: " + newCaptainName + " is now el capitano!");
                                               TeamReactor.overwriteAssignments(new HashSet<PlayerAssignment>(getFacade().getAssignments(pa.getGame())));

                                               notifyListener();
                                           }
                                       }
                );

                if (pa.getOrderNumber() == 1) {
                    btn.setVisibility(View.GONE);
                } else {
                    btn.setVisibility(View.VISIBLE);
                }

                Button btnKick = bottom_sheet.findViewById(R.id.assignmentKillBT);
                btnKick.setEnabled(assignmentsReOrdered.size() > 1);
                btnKick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TeamReactor.getAssignments().remove(pa);
                        getFacade().removePlayerFromAssignments(pa.getGame(), pa.getPlayer().getName());
                        TeamReactor.overwriteAssignments(new HashSet<PlayerAssignment>(getFacade().getAssignments(pa.getGame())));
                        notifyListener();
                    }
                });

                Button btnInfo = bottom_sheet.findViewById(R.id.assignmentInfoBT);
                btnInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), PlayerDetailActivity.class);
                        intent.putExtra(PlayerDetailFragment.ARG_ITEM_ID, pa.getPlayer().getName());

                        startActivity(intent);
                    }
                });

                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                Animation bounce = (Animation) AnimationUtils.loadAnimation(getContext(), R.anim.bounce);
                ((View) v.getParent()).findViewById(R.id.PlayerDetailIcon).startAnimation(bounce);
            }
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, "Problem with on click event: " + e.getLocalizedMessage());
        }
    }

    public void notifyListener() {
        for (TeamView_Interface t_listener : getListener()) {
            t_listener.notifyAdapter();
        }
    }


}