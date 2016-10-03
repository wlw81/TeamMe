package de.pasligh.android.teamme.tools;

import java.util.List;
import java.util.Locale;

import de.pasligh.android.teamme.R;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.objects.Score;

/**
 * Created by Thomas on 15.03.2016.
 */
public class ShareHelper {

    private static boolean CREATE_FOOTER = true;

    public static void appendFooter_Signature(StringBuilder p_stb, String p_footer) {
        if (CREATE_FOOTER) {
            p_stb.append("... ").append(p_footer).append(": ").append("\n" +
                    Flags.PLAYSTORE_LINK);
            CREATE_FOOTER = false;
        }
    }

    public static StringBuilder createTeamDecided_ShareText(String p_shareIntent, String p_team) {
        StringBuilder shareText = new StringBuilder();
        shareText.append(p_shareIntent).append(" ");
        int teamNr = 1;
        List<PlayerAssignment> assignments;
        while (!(assignments = TeamReactor.getAssignmentsByTeam(teamNr))
                .isEmpty()) {
            shareText.append(p_team.toUpperCase(Locale.getDefault())).append(" ")
                    .append(assignments.get(0).getPlayer().getName().toUpperCase(Locale.getDefault()))
                    .append(": ");

            for (int i = 0; i < assignments.size(); i++) {
                shareText.append((i + 1) + ". ").append(assignments.get(i).getPlayer().getName())
                        .append(" ");
            }

            teamNr++;
        }
        return shareText;
    }


    public static int getWinnerTeam(List<Score> p_lisScores) {
        int intHighestScore = -1;
        int winnerTeam = -1;
        for (Score s : p_lisScores) {
            if (s.getScoreCount() > intHighestScore) {
                intHighestScore = s.getScoreCount();
                winnerTeam = s.getTeamNr();
            } else if (intHighestScore >= 0 && s.getScoreCount() == intHighestScore && s.getTeamNr() != winnerTeam) {
                winnerTeam = Flags.DRAW_TEAM;
            }
        }
        return winnerTeam;
    }

}
