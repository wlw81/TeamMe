package de.pasligh.android.teamme.backend;

import de.pasligh.android.teamme.tools.Flags;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static String TABLE_GAMES = "GAME";
	public static String TABLE_PLAYERS = "PLAYER";
	public static String TABLE_ASSIGNMENTS = "ASSIGNMENTS";

	private static final String CREATE_GAMES = "CREATE TABLE "
			+ TABLE_GAMES
			+ " (_id integer primary key autoincrement, category_ordinal integer, created date not null);";
	private static final String CREATE_ASSIGNMENTS = "CREATE TABLE "
			+ TABLE_ASSIGNMENTS
			+ " (_id integer primary key autoincrement, sequence integer not null, team integer not null, game_id integer not null, player_id text not null);";
	private static final String CREATE_PLAYERS = "CREATE TABLE "
			+ TABLE_PLAYERS + " (name text primary key not null);";

	public DatabaseHelper(Context context) {
		super(context, Flags.LOGTAG, null, Flags.DBVERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.execSQL(CREATE_ASSIGNMENTS);
			db.execSQL(CREATE_PLAYERS);
			db.execSQL(CREATE_GAMES);
		} catch (SQLException e) {
			Log.e(Flags.LOGTAG, e.getLocalizedMessage());
		}
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public void onOpen(SQLiteDatabase db) {
//		 onCreate(db); // temp!
		super.onOpen(db);
	}

}
