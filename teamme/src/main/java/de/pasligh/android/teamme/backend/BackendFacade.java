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

import de.pasligh.android.teamme.objects.GameRecord;
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

    public long persistGame(GameRecord p_saveGameRecord) {
        long value = -1;
        try {
            value = getObjDatabase().insert(DatabaseHelper.TABLE_GAMES, null,
                    createGame_Values(p_saveGameRecord));
            if (value >= 0) {
                for (PlayerAssignment saveAssignment : p_saveGameRecord.getAssignments()) {
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
        Cursor query = null;
        try {
            query = getObjDatabase().query(DatabaseHelper.TABLE_ASSIGNMENTS, new String[]{"MAX(team)"}, "game_id = ?1", new String[]{String.valueOf(p_lngGameID)}, null, null, null);
            query.moveToFirst();
            int i = query.getInt(0);
            return i;
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return -1;

        } finally {
            if (query != null) {
                query.close();
            }
        }

    }

    public List<GameRecord> getGames() {
        List<GameRecord> gameRecords = new ArrayList<GameRecord>();
        Cursor query = null;
        try {
            query = getObjDatabase().query(false,
                    DatabaseHelper.TABLE_GAMES, new String[]{"_id", "category_ordinal", "sport", "created"},
                    null, null, null, null, "_id desc", "50");

            while (query.moveToNext()) {
                GameRecord gameRecordRead = new GameRecord();
                gameRecordRead.setId(query.getInt(0));
                //gameRecordRead...
                gameRecordRead.setSport(query.getString(2));
                gameRecordRead.setStartedAt(objDateFormat.parse(query.getString(3)));
                gameRecords.add(gameRecordRead);
            }

        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return gameRecords;
    }

    public List<GameRecord> getGamesByPlayer(String p_playername) {
        List<GameRecord> gameRecords = new ArrayList<GameRecord>();
        Cursor query = null;
        try {

            query = getObjDatabase().query(false,
                    DatabaseHelper.TABLE_ASSIGNMENTS, new String[]{"_id", "sequence", "team", "game_id", "player_id"},
                    "player_id = ?1", new String[]{p_playername}, null, null, "game_id desc", "10");
            List<PlayerAssignment>  lis = moveQueryToAssignments(query);

            for(PlayerAssignment assignment : lis){
                query = getObjDatabase().query(DatabaseHelper.TABLE_GAMES, new String[]{"_id", "category_ordinal", "sport", "created"},
                        "_id = ?1", new String[]{String.valueOf(assignment.getGame())}, null, null, "created desc", "10");

                while (query.moveToNext()) {
                    GameRecord lastgame = new GameRecord();
                    lastgame.setId(query.getInt(0));
                    //gameRead...
                    lastgame.setSport(query.getString(2));
                    lastgame.setStartedAt(objDateFormat.parse(query.getString(3)));
                    gameRecords.add(lastgame);
                }
                query.close();
            }

        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        } finally {
            if (query != null) {
                query.close();
            }
        }

        return gameRecords;
    }

    public List<GameRecord> getGames(String p_strSports) {
        List<GameRecord> gameRecords = new ArrayList<GameRecord>();
        Cursor query = null;
        try {
            query = getObjDatabase().query(false,
                    DatabaseHelper.TABLE_GAMES, new String[]{"_id", "category_ordinal", "sport", "created"},
                    "sport = ?1", new String[]{p_strSports}, null, null, "_id", null);

            while (query.moveToNext()) {
                GameRecord gameRecordRead = new GameRecord(getAssignments(query.getInt(0)));
                gameRecordRead.setId(query.getInt(0));
                gameRecordRead.setSport(query.getString(2));
                gameRecordRead.setStartedAt(objDateFormat.parse(query.getString(3)));
                gameRecords.add(gameRecordRead);
            }
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return gameRecords;
    }

    public List<Score> getScores(long p_lngGameID) {
        List<Score> scores = new ArrayList<Score>();
        Cursor query = null;

        try {
            if (p_lngGameID >= 0) {
                query = getObjDatabase().query(false,
                        DatabaseHelper.TABLE_SCORES, new String[]{"TEAM", "game_id", "round", "scorecount"},
                        "game_id = ?1", new String[]{String.valueOf(p_lngGameID)}, null, null, "game_id, round, team", null);
            } else {
                query = getObjDatabase().query(false,
                        DatabaseHelper.TABLE_SCORES, new String[]{"TEAM", "game_id", "round", "scorecount"},
                        null, null, null, null, "game_id, round, team", null);
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
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        } finally {
            if (query != null) {
                query.close();
            }
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

    private ContentValues createGame_Values(GameRecord p_saveGameRecord) {
        ContentValues valuesReturn = new ContentValues();
        valuesReturn.put("created",
                objDateFormat.format(p_saveGameRecord.getStartedAt()));
        valuesReturn.put("sport", p_saveGameRecord.getSport());
        valuesReturn.put("category_ordinal", -1);
        return valuesReturn;
    }

    public List<Player> getPlayers() {
        Cursor query = null;
        try {
            query = getObjDatabase().query(false,
                    DatabaseHelper.TABLE_PLAYERS, new String[]{"NAME"},
                    null, null, null, null, "NAME", null);
            return moveQueryToPlayers(query);
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        } finally {
            if (query != null) {
                query.close();
            }
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
        Cursor query = null;
        try {
            query = getObjDatabase().query(DatabaseHelper.TABLE_GAMES, new String[]{"created"},
                    null, null, null, null, "created desc", "1");
            query.moveToFirst();
            lasttime = objDateFormat.parse(query.getString(0));
            query.close();
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        } finally {
            if (query != null) {
                query.close();
            }
        }

        return lasttime;
    }

    public GameRecord getGame(int p_intID) {
        Cursor query = null;
        GameRecord readGameRecord = null;
        try {
            query = getObjDatabase().query(DatabaseHelper.TABLE_GAMES, new String[]{"_id", "category_ordinal", "sport", "created"},
                    "_id = ?1", new String[]{String.valueOf(p_intID)}, null, null, "_id", "1");
            query.moveToFirst();
            readGameRecord = new GameRecord();
            readGameRecord.setId(query.getInt(0));
            //gameRead...
            readGameRecord.setSport(query.getString(2));
            readGameRecord.setStartedAt(objDateFormat.parse(query.getString(3)));
            query.close();
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        } finally {
            if (query != null) {
                query.close();
            }
        }

        return readGameRecord;
    }

    public GameRecord getLastGamePlayed_by_Player(String p_playername) {
        GameRecord lastgame = null;
        Cursor query = null;
        try {

            query = getObjDatabase().query(false,
                    DatabaseHelper.TABLE_ASSIGNMENTS, new String[]{"_id", "sequence", "team", "game_id", "player_id"},
                    "player_id = ?1", new String[]{p_playername}, null, null, "game_id desc", "1");
            List<PlayerAssignment>  lis = moveQueryToAssignments(query);

            if(!lis.isEmpty()){
                query = getObjDatabase().query(DatabaseHelper.TABLE_GAMES, new String[]{"_id", "category_ordinal", "sport", "created"},
                        "_id = ?1", new String[]{String.valueOf(lis.get(0).getGame())}, null, null, "created desc", "1");

                query.moveToFirst();
                lastgame = new GameRecord();
                lastgame.setId(query.getInt(0));
                //gameRead...
                lastgame.setSport(query.getString(2));
                lastgame.setStartedAt(objDateFormat.parse(query.getString(3)));
                query.close();
            }

        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        } finally {
            if (query != null) {
                query.close();
            }
        }

        return lastgame;
    }

    public GameRecord getLastGamePlayed() {
        GameRecord lastgame = null;
        Cursor query = null;
        try {
            query = getObjDatabase().query(DatabaseHelper.TABLE_GAMES, new String[]{"_id", "category_ordinal", "sport", "created"},
                    null, null, null, null, "created desc", "1");
            query.moveToFirst();
            lastgame = new GameRecord();
            lastgame.setId(query.getInt(0));
            //gameRead...
            lastgame.setSport(query.getString(2));
            lastgame.setStartedAt(objDateFormat.parse(query.getString(3)));
            query.close();
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        } finally {
            if (query != null) {
                query.close();
            }
        }

        return lastgame;
    }

    public List<PlayerAssignment> getAssignments(int p_intGameID) {
        Cursor query = null;
        try {
            query = getObjDatabase().query(false,
                    DatabaseHelper.TABLE_ASSIGNMENTS, new String[]{"_id", "sequence", "team", "game_id", "player_id"},
                    "game_id = ?1", new String[]{String.valueOf(p_intGameID)}, null, null, "_id", null);
            return moveQueryToAssignments(query);
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        } finally {
            if (query != null) {
                query.close();
            }
        }
    }

    public PlayerAssignment getCaptain(int p_intGameID, int p_team) {
        Cursor query = null;
        try {
            query = getObjDatabase().query(false,
                    DatabaseHelper.TABLE_ASSIGNMENTS, new String[]{"_id", "sequence", "team", "game_id", "player_id"},
                    "game_id = ?1 and team = ?2", new String[]{String.valueOf(p_intGameID), String.valueOf(p_team)}, null, null, "sequence", "1");
            return moveQueryToAssignments(query).get(0);
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.toString());
            return null;
        } finally {
            if (query != null) {
                query.close();
            }
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

    public boolean mergePlayer(String p_strFromPlayer, String p_strToPlayer) {
        try {
            ContentValues valuesReturn = new ContentValues();
            valuesReturn.put("player_id", p_strToPlayer);
            getObjDatabase().update(DatabaseHelper.TABLE_ASSIGNMENTS, valuesReturn, "player_id = '" + p_strFromPlayer + "'", null);
            return deletePlayer(p_strFromPlayer);
        } catch (Exception e) {
            Log.i(Flags.LOGTAG, e.getMessage());
        }
        return false;
    }

    public boolean deletePlayer(String p_strPlayername) {
        Cursor query = null;
        try {
            // getObjDatabase().delete(DatabaseHelper.TABLE_ASSIGNMENTS, "player_id = ?1", new String[]{p_strPlayername}); don't do that - because it will mess all recorded data!!!
            int i = getObjDatabase().delete(DatabaseHelper.TABLE_PLAYERS, "name = ?1", new String[]{p_strPlayername});
            return i > 0;
        } catch (Exception e) {
            Log.i(Flags.LOGTAG, e.getMessage());
        }
        return false;
    }


}
