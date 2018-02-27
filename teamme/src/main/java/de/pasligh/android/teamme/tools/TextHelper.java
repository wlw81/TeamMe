package de.pasligh.android.teamme.tools;

import android.util.Log;

import java.util.List;
import java.util.Locale;

import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.objects.Score;

/**
 * Created by Thomas on 15.03.2016.
 */
public class TextHelper {

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

    public static int getWinnerTeam_by_OverallScore(List<Score> p_lisScores) {
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

    public static int getWinnerTeam_by_RoundsOrScore(List<Score> p_scoreList, int p_rounds, int p_teamCount) {
        if(p_rounds <= 0){
            return Flags.DRAW_TEAM;
        }else{
            Integer[][] wonRounds = new Integer[p_rounds][p_teamCount + 1];
            Integer[][] pointsEachRound = new Integer[p_rounds][p_teamCount + 1];

            for (int currentRoundNr = 0; currentRoundNr < pointsEachRound.length; currentRoundNr++) {
                int highestScore = -1;
                for (int currentTeam = 1; currentTeam < pointsEachRound[currentRoundNr].length; currentTeam++) {
                    // we're adding everything up to an 2dim array, so we can scroll through nicely
                    for (Score s : p_scoreList) {
                        if (s.getRoundNr() == currentRoundNr && s.getTeamNr() == currentTeam) {
                            if (pointsEachRound[currentRoundNr][currentTeam] == null) {
                                pointsEachRound[currentRoundNr][currentTeam] = new Integer(0);
                            }
                            pointsEachRound[currentRoundNr][currentTeam] += s.getScoreCount();

                            // let's measure the highest score!
                            if (s.getScoreCount() >= highestScore) {
                                highestScore = s.getScoreCount();
                            }
                        }
                    }
                }


                // let's see wich team gained the most points this round
                for (int currentTeam = 1; currentTeam < pointsEachRound[currentRoundNr].length; currentTeam++) {
                    if (pointsEachRound[currentRoundNr][currentTeam] == highestScore) {
                        if (wonRounds[currentRoundNr][currentTeam] == null) {
                            wonRounds[currentRoundNr][currentTeam] = new Integer(0);
                        }
                        wonRounds[currentRoundNr][currentTeam] += 1; // team scored a round point!
                    }
                }


            }

            Integer[] overAllScoreEachTeam = new Integer[p_teamCount + 1];
            Integer[] roundPointsEachTeam = new Integer[p_teamCount + 1];

            for (int round = 0; round < wonRounds.length; round++) {
                for (int team = 1; team < wonRounds[round].length; team++) {

                    if (roundPointsEachTeam[team] == null) {
                        roundPointsEachTeam[team] = new Integer(0);
                    }

                    if (overAllScoreEachTeam[team] == null) {
                        overAllScoreEachTeam[team] = new Integer(0);
                    }

                    if (null != wonRounds[round][team]) {
                        roundPointsEachTeam[team] += wonRounds[round][team];
                    }

                    if (null != pointsEachRound[round][team]) {
                        overAllScoreEachTeam[team] += pointsEachRound[round][team];
                    }
                }
            }


            int maxRoundPoints = 0;
            int winningTeam = -1;
            for (int team = 1; team < roundPointsEachTeam.length; team++) {
                if (winningTeam == -1 || roundPointsEachTeam[team] > maxRoundPoints) {
                    maxRoundPoints = roundPointsEachTeam[team];
                    winningTeam = team;
                    Log.i(Flags.LOGTAG, "Wins through roundpoints - team " + team + " round points:  " + roundPointsEachTeam[team]);
                } else if (roundPointsEachTeam[team] == maxRoundPoints) {
                    winningTeam = Flags.DRAW_TEAM;
                    Log.i(Flags.LOGTAG, "Even through roundpoints - team " + team + " round points:  " + roundPointsEachTeam[team]);
                }
            }

            if (winningTeam == Flags.DRAW_TEAM) {
                winningTeam = -1;
                int maxOverAllScore = 0;
                for (int team = 1; team < overAllScoreEachTeam.length; team++) {
                    int compareScore = overAllScoreEachTeam[team];
                    if (winningTeam == -1 || compareScore > maxOverAllScore) {
                        maxOverAllScore = compareScore;
                        winningTeam = team;
                        Log.i(Flags.LOGTAG, "Wins through overall score - team " + team + " score:  " + compareScore);
                    } else if (compareScore == maxOverAllScore) {
                        winningTeam = Flags.DRAW_TEAM;
                        Log.i(Flags.LOGTAG, "Even through overall score - team " + team + " score:  " + compareScore);
                    }
                }
            }
            return winningTeam;
        }
    }


}
