package de.pasligh.android.teamme.objects;

/**
 * Created by Thomas on 07.02.2016.
 */
public class Score {

    int gameId;
    int teamNr;

    int roundNr;
    int scoreCount;

    @Override
    public String toString() {
        return "Game " + getGameId() + "Round #"+getRoundNr()+" Team " + getTeamNr() + ": " + getScoreCount();
    }

    @Override
    public boolean equals(Object compare) {
        if (compare instanceof Score) {
            Score s = (Score) compare;
            return s.getGameId() == getGameId() && s.getRoundNr() == getRoundNr() && s.getTeamNr() == getTeamNr();
        }
        return false;
    }

    public int getRoundNr() {
        return roundNr;
    }

    public void setRoundNr(int roundNr) {
        this.roundNr = roundNr;
    }


    public int getScoreCount() {
        return scoreCount;
    }

    public void setScoreCount(int scoreCount) {
        this.scoreCount = scoreCount;
    }


    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getTeamNr() {
        return teamNr;
    }

    public void setTeamNr(int teamNr) {
        this.teamNr = teamNr;
    }

}
