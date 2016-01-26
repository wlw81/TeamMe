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
import de.pasligh.android.teamme.objects.PlayerAssignemnt;
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
		long value = getObjDatabase().insert(DatabaseHelper.TABLE_GAMES, null,
				createGame_Values(p_saveGame));
		for (PlayerAssignemnt saveAssignment : p_saveGame.getAssignments()) {
			saveAssignment.setGame((int) value);
			getObjDatabase().insert(DatabaseHelper.TABLE_ASSIGNMENTS, null,
					createAssignment_Values(saveAssignment));
		}

		return value;
	}

	public long persistPlayer(Player p_playerNew) {
		ContentValues values = new ContentValues();
		values.put("name", p_playerNew.getName());
		return getObjDatabase().insert(DatabaseHelper.TABLE_PLAYERS, null,
				values);

	}

	private ContentValues createAssignment_Values(
			PlayerAssignemnt p_saveAssignment) {
		ContentValues valuesReturn = new ContentValues();
		valuesReturn.put("sequence", p_saveAssignment.getOrderNumber());
		valuesReturn.put("team", p_saveAssignment.getTeam());
		valuesReturn.put("game_id", p_saveAssignment.getGame());
		valuesReturn.put("player_id", p_saveAssignment.getPlayer().getName());
		return valuesReturn;
	}

	private ContentValues createGame_Values(Game p_saveGame) {
		ContentValues valuesReturn = new ContentValues();
		valuesReturn.put("created",
				objDateFormat.format(p_saveGame.getStartedAt()));
		valuesReturn.put("category_ordinal", -1);
		return valuesReturn;
	}

	public List<Player> getPlayers() {
		try {
			Cursor query = getObjDatabase().query(false,
					DatabaseHelper.TABLE_PLAYERS, new String[] { "NAME" },
					null, null, null, null, "NAME", null);
			return moveQueryToPlayers(query);
		} catch (Exception e) {
			Log.e(Flags.LOGTAG, e.toString());
			return null;
		}
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
	
	public int getGameCount(){
		Cursor mCount= getObjDatabase().rawQuery("select count(*) from "+DatabaseHelper.TABLE_GAMES, null);
		mCount.moveToFirst();
		int count= mCount.getInt(0);
		mCount.close();
		return count;
	}
	
	public Date getLastTimePlayed() {
		Date lasttime = null;
		try {
			Cursor query = getObjDatabase().query(DatabaseHelper.TABLE_GAMES, new String[] { "created" },
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

	public List<PlayerAssignemnt> getAllAssignments() {
		try {
			Cursor query = getObjDatabase().query(false,
					DatabaseHelper.TABLE_ASSIGNMENTS, new String[] { "_id", "sequence", "team", "game_id", "player_id" },
					null, null, null, null, "_id", null);
			return moveQueryToAssignments(query);
		} catch (Exception e) {
			Log.e(Flags.LOGTAG, e.toString());
			return null;
		}
	}

	private List<PlayerAssignemnt> moveQueryToAssignments(Cursor query) {
		List<PlayerAssignemnt> assignments = new ArrayList<PlayerAssignemnt>();
		while (query.moveToNext()) {
			PlayerAssignemnt addAssignment = new PlayerAssignemnt();
			addAssignment.setId(query.getInt(0));
			addAssignment.setOrderNumber(query.getInt(1));
			addAssignment.setTeam(query.getInt(2));
			addAssignment.setGame(query.getInt(3));
			addAssignment.setPlayer(new Player(query.getString(4)));
			
			assignments.add(addAssignment);
		}
		return assignments;
	}

}
