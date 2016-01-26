package de.pasligh.android.teamme.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import de.pasligh.android.teamme.R;
import de.pasligh.android.teamme.objects.PlayerAssignemnt;

public final class TeamReactor {

	private static final Set<PlayerAssignemnt> assignments = new HashSet<PlayerAssignemnt>();
	private static int assignmentsDone;

	private TeamReactor() {
		super();
	}

	public static void decideTeams(int p_teamCount, int p_playerCount) {
		int currentTeam = 0;
		int teamPosNumber = 1;
		assignmentsDone = 0;
		assignments.clear();
		for (int i = 0; i < p_playerCount; i++) {
			if (currentTeam < p_teamCount) {
				currentTeam++;
			} else {
				currentTeam = 1;
				teamPosNumber++;
			}

			PlayerAssignemnt assignment = new PlayerAssignemnt();
			assignment.setTeam(currentTeam);
			assignment.setOrderNumber(teamPosNumber);
			assignments.add(assignment);
		}
	}

	public static boolean hasAssignmentsLeft() {
		return getAssignmentsDone() < assignments.size();
	}

	public static PlayerAssignemnt revealNextAssignment() {
		Iterator<PlayerAssignemnt> iterator = assignments.iterator();

		while (iterator.hasNext()) {
			PlayerAssignemnt assignemnt = iterator.next();
			if (!assignemnt.isAssigned()) {
				assignmentsDone++;
				assignemnt.setAssigned(true);
				return assignemnt;
			}
		}

		return null;
	}

	/**
	 * @return the assignmentsDone
	 */
	public static int getAssignmentsDone() {
		return assignmentsDone;
	}

	/**
	 * @return the assignments
	 */
	public static Set<PlayerAssignemnt> getAssignments() {
		return assignments;
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
