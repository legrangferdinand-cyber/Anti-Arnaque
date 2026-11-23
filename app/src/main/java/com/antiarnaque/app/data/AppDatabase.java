package com.antiarnaque.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Base SQLite locale : historique des analyses + file d'attente de signalements. */
public final class AppDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "anti_arnaque.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_HISTORY = "history";
    public static final String TABLE_REPORTS = "reports";

    public static final String COL_ID = "_id";
    public static final String COL_MESSAGE = "message";
    public static final String COL_SCORE = "score";
    public static final String COL_LEVEL = "level";
    public static final String COL_REASONS = "reasons";
    public static final String COL_ENGINE = "engine";
    public static final String COL_REPORTED = "reported";
    public static final String COL_TIMESTAMP = "created_at";
    public static final String COL_HASH = "message_hash";
    public static final String COL_REASON = "reason";
    public static final String COL_SYNCED = "synced";

    private static volatile AppDatabase instance;

    private AppDatabase(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = new AppDatabase(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_HISTORY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MESSAGE + " TEXT NOT NULL, "
                + COL_SCORE + " REAL NOT NULL, "
                + COL_LEVEL + " TEXT NOT NULL, "
                + COL_REASONS + " TEXT NOT NULL, "
                + COL_ENGINE + " TEXT NOT NULL, "
                + COL_REPORTED + " INTEGER NOT NULL DEFAULT 0, "
                + COL_TIMESTAMP + " INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_REPORTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_HASH + " TEXT NOT NULL, "
                + COL_SCORE + " REAL NOT NULL, "
                + COL_REASON + " TEXT NOT NULL, "
                + COL_SYNCED + " INTEGER NOT NULL DEFAULT 0, "
                + COL_TIMESTAMP + " INTEGER NOT NULL)");

        db.execSQL("CREATE INDEX idx_history_date ON " + TABLE_HISTORY + "(" + COL_TIMESTAMP + ")");
        db.execSQL("CREATE INDEX idx_reports_sync ON " + TABLE_REPORTS + "(" + COL_SYNCED + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORTS);
        onCreate(db);
    }
}
