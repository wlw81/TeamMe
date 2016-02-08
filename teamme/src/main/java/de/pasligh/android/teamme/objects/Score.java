package de.pasligh.android.teamme.objects;

/**
 * Created by Thomas on 07.02.2016.
 */
public class Score {

    int id;
    int gameId;
    int teamNr;

    int roundNr;
    int scoreCount;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
