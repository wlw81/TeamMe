package de.pasligh.android.teamme.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.util.Log;

import de.pasligh.android.teamme.R;
import de.pasligh.android.teamme.objects.PlayerAssignemnt;

public final class TeamReactor {


    private static final Set<PlayerAssignemnt> assignments = new HashSet<PlayerAssignemnt>();
    private static int assignmentsRevealed;

    static int currentTeam = 0;
    static int teamPosNumber = 1;

    private TeamReactor() {
        super();
    }

    public static void decideTeams(int p_teamCount, int p_playerCount, List<PlayerAssignemnt> p_lisPreAssignments) {

        resetReactor();

        for (int i = 0; i < p_playerCount; i++) {
            assignments.add(doAssignment(p_teamCount));
        }

        if (null != p_lisPreAssignments && !p_lisPreAssignments.isEmpty()) {
            for (PlayerAssignemnt pre : p_lisPreAssignments) {
                transferAssignment(pre);
            }
        }
    }

    private static void resetReactor() {
        teamPosNumber = 1;
        assignmentsRevealed = 0;
        assignments.clear();
    }

    /**
     * Transfers a pre assignment to the team reactor assignments.
     * @param pre
     * @return transferDone
     */
    private static boolean transferAssignment(PlayerAssignemnt pre) {
        if (pre.getPlayer() != null) {
            for (PlayerAssignemnt assignmentByTeamReactor : assignments) {
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

    private static PlayerAssignemnt doAssignment(int p_teamCount) {
        if (currentTeam < p_teamCount) {
            currentTeam++;
        } else {
            currentTeam = 1;
            teamPosNumber++;
        }

        PlayerAssignemnt assignment = new PlayerAssignemnt();
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

    public static PlayerAssignemnt revealNextAssignment() {
        Iterator<PlayerAssignemnt> iterator = assignments.iterator();

        while (iterator.hasNext()) {
            PlayerAssignemnt assignemnt = iterator.next();
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
    public static Set<PlayerAssignemnt> getAssignments() {
        return assignments;
    }

    public static void overwriteAssignments(Set<PlayerAssignemnt> p_set){
        resetReactor();
        assignments.addAll(p_set);
        assignmentsRevealed = p_set.size();
    }

    public static String createPlayertitle(Context p_contxt,
                                           PlayerAssignemnt p_assignment) {
        if (p_assignment.getOrderNumber() == 0) {
            return (p_contxt.getResources().getString(R.string.captain));
        } else {
            StringBuilder playertitle = new StringBuilder(p_contxt
                    .getResources().getString(R.string.number));
            playertitle.append(" ").append(p_assignment.getOrderNumber());
            return playertitle.toString();
        }
    }

    public static List<PlayerAssignemnt> getAssignmentsByTeam(int p_teamNr) {
        PlayerAssignemnt[] teamAssignments = new PlayerAssignemnt[getAssignments()
                .size()];
        Iterator<PlayerAssignemnt> iterator = getAssignments().iterator();
        while (iterator.hasNext()) {
            PlayerAssignemnt next = iterator.next();
            if (next.getTeam() == p_teamNr) {
                teamAssignments[next.getOrderNumber()] = next;
            }
        }

        // remove nuller items
        List<PlayerAssignemnt> assignmentsReturn = new ArrayList<PlayerAssignemnt>();
        for (PlayerAssignemnt assignmentAdd : teamAssignments) {
            if (null != assignmentAdd) {
                assignmentsReturn.add(assignmentAdd);
            }
        }
        return assignmentsReturn;

    }

}
