package de.pasligh.android.teamme.backend;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import de.pasligh.android.teamme.objects.Game;
import de.pasligh.android.teamme.objects.Player;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.objects.Score;
import de.pasligh.android.teamme.tools.Flags;

@SuppressLint("SimpleDateFormat")
public class BackendFacade {

    private SQLiteDatabase objDatabase;
    private DatabaseHelper objDB_API;
    private Context objApplicationContext;
    private SimpleDateFormat objDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    public BackendFacade(Context p_objApplicationContext) {
        objApplicationContext = p_objApplicationContext;
    }

    /**
     * @return the objDB_API
     */
    public DatabaseHelper getObjDB_API() {
        if (objDB_API == null) {
            objDB_API = new DatabaseHelper(objApplicationContext);
        }
        return objDB_API;
    }

    /**
     * @return the objDatabase
     */
    public SQLiteDatabase getObjDatabase() {
        if (objDatabase == null) {
            objDatabase = getObjDB_API().getWritableDatabase();
        }
        return objDatabase;
    }

    public long persistGame(Game p_saveGame) {
        long value = -1;
        try {
            value = getObjDatabase().insert(DatabaseHelper.TABLE_GAMES, null,
                    createGame_Values(p_saveGame));
            if (value >= 0) {
                for (PlayerAssignment saveAssignment : p_saveGame.getAssignments()) {
                    saveAssignment.setGame((int) value);
                    getObjDatabase().insert(DatabaseHelper.TABLE_ASSIGNMENTS, null,
                            createAssignment_Values(saveAssignment));
                }
            }

        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
        }

        return value;
    }

    public long persistPlayer(Player p_playerNew) {
        ContentValues values = new ContentValues();
        values.put("name", p_playerNew.getName());
        return getObjDatabase().insert(DatabaseHelper.TABLE_PLAYERS, null,
                values);

    }

    public int getTeamCount(long p_lngGameID) {
        try {
            Cursor query = getObjDatabase().query(DatabaseHelper.TABLE_ASSIGNMENTS, new String[]{"MAX(team)"}, "game_id = ?1", new String[]{String.valueOf(p_lngGameID)}, null, null, null);
            query.moveToFirst();
            int i = query.getInt(0);
            query.close();
            return i;
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return -1;
        }
    }

    public List<Game> getGames(String p_strSports) {
        List<Game> games = new ArrayList<Game>();

        try {
            Cursor query = getObjDatabase().query(false,
                    DatabaseHelper.TABLE_GAMES, new String[]{"_id", "category_ordinal", "sport", "created"},
                    "sport = ?1", new String[]{p_strSports}, null, null, "_id", null);

            while (query.moveToNext()) {
                Game gameRead = new Game();
                gameRead.setId(query.getInt(0));
                //gameRead...
                gameRead.setSport(query.getString(2));
                gameRead.setStartedAt(objDateFormat.parse(query.getString(3)));
                games.add(gameRead);
            }
            query.close();
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        }

        return games;
    }

    public List<Score> getScores(long p_lngGameID) {
        List<Score> scores = new ArrayList<Score>();

        try {
            Cursor query;
            if (p_lngGameID >= 0) {
                query = getObjDatabase().query(false,
                        DatabaseHelper.TABLE_SCORES, new String[]{"TEAM", "game_id", "round", "scorecount"},
                        "game_id = ?1", new String[]{String.valueOf(p_lngGameID)}, null, null, "game_id", null);
            } else {
                query = getObjDatabase().query(false,
                        DatabaseHelper.TABLE_SCORES, new String[]{"TEAM", "game_id", "round", "scorecount"},
                        null, null, null, null, "game_id", null);
            }

            while (query.moveToNext()) {
                Score scoreRead = new Score();
                scoreRead.setTeamNr(query.getInt(0));
                scoreRead.setGameId(query.getInt(1));
                scoreRead.setRoundNr((query.getInt(2)));
                scoreRead.setScoreCount
                        (query.getInt(3));
                scores.add(scoreRead);
            }
            query.close();
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        }

        return scores;
    }

    private ContentValues createAssignment_Values(
            PlayerAssignment p_saveAssignment) {
        ContentValues valuesReturn = new ContentValues();
        valuesReturn.put("sequence", p_saveAssignment.getOrderNumber());
        valuesReturn.put("team", p_saveAssignment.getTeam());
        valuesReturn.put("game_id", p_saveAssignment.getGame());
        valuesReturn.put("player_id", p_saveAssignment.getPlayer().getName());
        return valuesReturn;
    }

    private ContentValues createScore_Values(Score p_scoreValues, boolean p_scorecount) {
        ContentValues valuesReturn = new ContentValues();
        valuesReturn.put("team", p_scoreValues.getTeamNr());
        valuesReturn.put("game_id", p_scoreValues.getGameId());
        valuesReturn.put("round", p_scoreValues.getRoundNr());
        if (p_scorecount) {
            valuesReturn.put("scorecount", p_scoreValues.getScoreCount());
        }
        return valuesReturn;
    }

    private ContentValues createGame_Values(Game p_saveGame) {
        ContentValues valuesReturn = new ContentValues();
        valuesReturn.put("created",
                objDateFormat.format(p_saveGame.getStartedAt()));
        valuesReturn.put("sport", p_saveGame.getSport());
        valuesReturn.put("category_ordinal", -1);
        return valuesReturn;
    }

    public List<Player> getPlayers() {
        try {
            Cursor query = getObjDatabase().query(false,
                    DatabaseHelper.TABLE_PLAYERS, new String[]{"NAME"},
                    null, null, null, null, "NAME", null);
            return moveQueryToPlayers(query);
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        }
    }

    public String[] getSports() {
        Cursor query = getObjDatabase().query(true, DatabaseHelper.TABLE_GAMES, new String[]{"SPORT"}, null, null, null, null, "SPORT", null);
        String[] sports = new String[query.getCount()];
        int i = 0;
        while (query.moveToNext()) {
            sports[i] = query.getString(0);
            i++;
        }
        return sports;
    }

    public String[] getPlayersAsStringArray() {
        List<Player> players = getPlayers();
        String[] playerArray = new String[players.size()];
        for (int i = 0; i < playerArray.length; i++) {
            playerArray[i] = players.get(i).getName();
        }
        return playerArray;
    }

    private List<Player> moveQueryToPlayers(Cursor query) {
        List<Player> lisPlayers = new ArrayList<Player>();
        while (query.moveToNext()) {
            Player objWorkflow_Add = new Player(query.getString(0));
            lisPlayers.add(objWorkflow_Add);
        }
        return lisPlayers;
    }

    public int getGameCount() {
        Cursor mCount = getObjDatabase().rawQuery("select count(*) from " + DatabaseHelper.TABLE_GAMES, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public Date getLastTimePlayed() {
        Date lasttime = null;
        try {
            Cursor query = getObjDatabase().query(DatabaseHelper.TABLE_GAMES, new String[]{"created"},
                    null, null, null, null, "created desc", "1");
            query.moveToFirst();
            lasttime = objDateFormat.parse(query.getString(0));
            query.close();
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        }

        return lasttime;
    }

    public Game getGame(int p_intID) {
        Game readGame = null;
        try {
            Cursor query = getObjDatabase().query(DatabaseHelper.TABLE_GAMES, new String[]{"_id", "category_ordinal", "sport", "created"},
                    "_id = ?1", new String[]{String.valueOf(p_intID)}, null, null, "_id", "1");
            query.moveToFirst();
            readGame = new Game();
            readGame.setId(query.getInt(0));
            //gameRead...
            readGame.setSport(query.getString(2));
            readGame.setStartedAt(objDateFormat.parse(query.getString(3)));
            query.close();
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        }

        return readGame;
    }

    public Game getLastGamePlayed() {
        Game lastgame = null;
        try {
            Cursor query = getObjDatabase().query(DatabaseHelper.TABLE_GAMES, new String[]{"_id", "category_ordinal", "sport", "created"},
                    null, null, null, null, "created desc", "1");
            query.moveToFirst();
            lastgame = new Game();
            lastgame.setId(query.getInt(0));
            //gameRead...
            lastgame.setSport(query.getString(2));
            lastgame.setStartedAt(objDateFormat.parse(query.getString(3)));
            query.close();
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        }

        return lastgame;
    }

    public List<PlayerAssignment> getAssignments(int p_intGameID) {
        try {
            Cursor query = getObjDatabase().query(false,
                    DatabaseHelper.TABLE_ASSIGNMENTS, new String[]{"_id", "sequence", "team", "game_id", "player_id"},
                    "game_id = ?1", new String[]{String.valueOf(p_intGameID)}, null, null, "_id", null);
            return moveQueryToAssignments(query);
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        }
    }

    public List<PlayerAssignment> getAllAssignments() {
        try {
            Cursor query = getObjDatabase().query(false,
                    DatabaseHelper.TABLE_ASSIGNMENTS, new String[]{"_id", "sequence", "team", "game_id", "player_id"},
                    null, null, null, null, "_id", null);
            return moveQueryToAssignments(query);
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        }
    }

    private List<PlayerAssignment> moveQueryToAssignments(Cursor query) {
        List<PlayerAssignment> assignments = new ArrayList<PlayerAssignment>();
        while (query.moveToNext()) {
            PlayerAssignment addAssignment = new PlayerAssignment();
            addAssignment.setId(query.getInt(0));
            addAssignment.setOrderNumber(query.getInt(1));
            addAssignment.setTeam(query.getInt(2));
            addAssignment.setGame(query.getInt(3));
            addAssignment.setPlayer(new Player(query.getString(4)));

            assignments.add(addAssignment);
        }
        return assignments;
    }

    public void mergeScore(Score p_score) {
        int id = -1;
        try {
            // getObjDatabase().update(DatabaseHelper.TABLE_SCORES, createScore_Values(p_score, true), "game_id = ?1 and round = ?2 and team = ?3", new String[]{String.valueOf(p_score.getGameId()), String.valueOf(p_score.getRoundNr()), String.valueOf(p_score.getTeamNr())});
            id = getObjDatabase().update(DatabaseHelper.TABLE_SCORES, createScore_Values(p_score, true), "game_id = " + p_score.getGameId() + " and round = " + p_score.getRoundNr() + " and team = " + p_score.getTeamNr(), null);
        } catch (Exception e) {
            Log.i(Flags.LOGTAG, e.getMessage());
        } finally {
            if (id <= 0) {
                id = (int) getObjDatabase().insert(DatabaseHelper.TABLE_SCORES, null, createScore_Values(p_score, true));
            }
        }

    }


}
