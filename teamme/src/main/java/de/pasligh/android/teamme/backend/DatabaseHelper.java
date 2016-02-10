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
    public static String TABLE_SCORES = "SCORE";

    private static final String CREATE_GAMES = "CREATE TABLE "
            + TABLE_GAMES
            + " (_id integer primary key autoincrement, category_ordinal integer, sport text not null default ('unknown'), created date not null);";
    private static final String CREATE_ASSIGNMENTS = "CREATE TABLE "
            + TABLE_ASSIGNMENTS
            + " (_id integer primary key autoincrement, sequence integer not null, team integer not null, game_id integer not null, player_id text not null);";
    private static final String CREATE_PLAYERS = "CREATE TABLE "
            + TABLE_PLAYERS + " (name text primary key not null);";
    private static final String CREATE_SCORES = "CREATE TABLE "
            + TABLE_SCORES + " (game_id integer not null, round integer not null, team integer not null, scorecount integer not null, PRIMARY KEY (game_id, round, team));";

    private static final String UPGRADE_1_to_2_GAMES_TABLE = "alter table " + TABLE_GAMES + " add column sport text not null default ('unknown')";

    public DatabaseHelper(Context context) {
        super(context, Flags.LOGTAG, null, Flags.DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_ASSIGNMENTS);
            db.execSQL(CREATE_PLAYERS);
            db.execSQL(CREATE_GAMES);
            db.execSQL(CREATE_SCORES);
        } catch (SQLException e) {
            Log.e(Flags.LOGTAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            try {
                db.execSQL(CREATE_SCORES);
                db.execSQL(UPGRADE_1_to_2_GAMES_TABLE);
            } catch (SQLException e) {
                Log.e(Flags.LOGTAG, e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        //db.execSQL("drop table score;");
    }

}
