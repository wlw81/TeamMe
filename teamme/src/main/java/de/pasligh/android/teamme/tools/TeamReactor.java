package de.pasligh.android.teamme.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.util.Log;

import de.pasligh.android.teamme.R;
import de.pasligh.android.teamme.objects.Player;
import de.pasligh.android.teamme.objects.PlayerAssignment;

public final class TeamReactor {


    private static final Set<PlayerAssignment> assignments = new HashSet<PlayerAssignment>();
    private static int assignmentsRevealed;

    static int currentTeam = 0;
    static int teamPosNumber = 1;

    private TeamReactor() {
        super();
    }

    public static void decideTeams(int p_teamCount, int p_playerCount, List<PlayerAssignment> p_lisPreAssignments) {
        resetReactor();

        // adding the assignments to the hashset, will already randomize the order!
        for (int i = 0; i < p_playerCount; i++) {
            assignments.add(doAssignment(p_teamCount));
        }

        if (null != p_lisPreAssignments && !p_lisPreAssignments.isEmpty()) {
            for (PlayerAssignment pre : p_lisPreAssignments) {
                transferAssignment(pre);
            }
        }
    }

    public static void resetReactor() {
        currentTeam = 0;
        teamPosNumber = 1;
        assignmentsRevealed = 0;
        assignments.clear();
    }

    /**
     * Transfers a pre assignment to the team reactor assignments.
     *
     * @param pre
     * @return transferDone
     */
    private static boolean transferAssignment(PlayerAssignment pre) {
        if (pre.getPlayer() != null) {
            for (PlayerAssignment assignmentByTeamReactor : assignments) {
                if (assignmentByTeamReactor.getPlayer() == null) {

                    // if there is no team preferred or the preferred team is fitting to the current assignment
                    if (pre.getTeam() <= 0 || pre.getTeam() == assignmentByTeamReactor.getTeam()) {
                        if (pre.getOrderNumber() != 1 || assignmentByTeamReactor.getOrderNumber() == 1) {
                            assignmentByTeamReactor.setPlayer(pre.getPlayer());
                            assignmentsRevealed++;
                            assignmentByTeamReactor.setRevealed(true);
                            Log.i(Flags.LOGTAG, "Pre assigned -> " + assignmentByTeamReactor);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private static PlayerAssignment doAssignment(int p_teamCount) {
        if (currentTeam < p_teamCount) {
            currentTeam++;
        } else {
            currentTeam = 1;
            teamPosNumber++;
        }

        PlayerAssignment assignment = new PlayerAssignment();
        assignment.setTeam(currentTeam);
        assignment.setOrderNumber(teamPosNumber);
        return assignment;
    }

    public static void decideTeams(int p_teamCount, int p_playerCount) {
        decideTeams(p_teamCount, p_playerCount, null);
    }

    public static boolean hasAssignmentsLeft() {
        return getAssignmentsRevealed() < assignments.size();
    }

    public static PlayerAssignment revealNextAssignment() {
        Iterator<PlayerAssignment> iterator = assignments.iterator();

        while (iterator.hasNext()) {
            PlayerAssignment assignemnt = iterator.next();
            if (!assignemnt.isRevealed()) {
                assignmentsRevealed++;
                assignemnt.setRevealed(true);
                return assignemnt;
            }
        }

        return null;
    }

    /**
     * @return the assignmentsRevealed
     */
    public static int getAssignmentsRevealed() {
        return assignmentsRevealed;
    }

    /**
     * @return the assignments
     */
    public static Set<PlayerAssignment> getAssignments() {
        return assignments;
    }

    public static void overwriteAssignments(Set<PlayerAssignment> p_set) {
        resetReactor();
        assignments.addAll(p_set);
        assignmentsRevealed = p_set.size();
    }

    public static String createPlayertitle(Context p_contxt,
                                           PlayerAssignment p_assignment) {
        if (p_assignment.getOrderNumber() == 0) {
            return (p_contxt.getResources().getString(R.string.captain));
        } else {
            StringBuilder playertitle = new StringBuilder(p_contxt
                    .getResources().getString(R.string.number));
            playertitle.append(" ").append(p_assignment.getOrderNumber());
            return playertitle.toString();
        }
    }

    public static List<PlayerAssignment> getAssignmentsByTeam(int p_teamNr) {
        List<PlayerAssignment> teamAssignments = new ArrayList<>();

        for (PlayerAssignment p : getAssignments()) {
            if (p.getTeam() == p_teamNr) {
                if(p.getPlayer() != null){
                    teamAssignments.add(p);}
            }
        }

        return teamAssignments;

    }

}
