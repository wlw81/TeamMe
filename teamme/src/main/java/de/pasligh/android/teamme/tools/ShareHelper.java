package de.pasligh.android.teamme.tools;

import java.util.List;

import de.pasligh.android.teamme.R;
import de.pasligh.android.teamme.objects.Score;

/**
 * Created by Thomas on 15.03.2016.
 */
public class ShareHelper {

    private static boolean CREATE_FOOTER = true;

    public static void appendFooter_Signature(StringBuilder p_stb, String p_footer) {
        if (CREATE_FOOTER) {
            p_stb.append("... ").append(p_footer).append(": ").append("\n" +
                    "http://play.google.com/store/apps/details?id=de.pasligh.android.teamme");
            CREATE_FOOTER = false;
        }
    }

    public static int getWinnerTeam(List<Score> p_lisScores) {
        int intHighestScore = -1;
        int winnerTeam = -1;
        for (Score s : p_lisScores) {
            if (s.getScoreCount() > intHighestScore) {
                intHighestScore = s.getScoreCount();
                winnerTeam = s.getTeamNr();
            } else if (intHighestScore >= 0 && s.getScoreCount() == intHighestScore && s.getTeamNr() != winnerTeam) {
                winnerTeam = 4200;
            }
        }
        return winnerTeam;
    }

}
