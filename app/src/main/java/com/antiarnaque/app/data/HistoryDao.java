package com.antiarnaque.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.antiarnaque.app.model.AnalysisResult;
import com.antiarnaque.app.model.HistoryEntry;
import com.antiarnaque.app.model.RiskLevel;

import java.util.ArrayList;
import java.util.List;

/** Accès SQLite à l'historique des analyses. Toutes les méthodes sont bloquantes. */
public final class HistoryDao {

    private final AppDatabase database;

    public HistoryDao(Context context) {
        this.database = AppDatabase.getInstance(context);
    }

    public long insert(AnalysisResult result) {
        ContentValues values = new ContentValues();
        values.put(AppDatabase.COL_MESSAGE, result.getMessage());
        values.put(AppDatabase.COL_SCORE, result.getScore());
        values.put(AppDatabase.COL_LEVEL, result.getLevel().name());
        values.put(AppDatabase.COL_REASONS, result.getReasonsText());
        values.put(AppDatabase.COL_ENGINE, result.getEngine());
        values.put(AppDatabase.COL_REPORTED, 0);
        values.put(AppDatabase.COL_TIMESTAMP, result.getTimestamp());

        SQLiteDatabase db = database.getWritableDatabase();
        return db.insert(AppDatabase.TABLE_HISTORY, null, values);
    }

    public List<HistoryEntry> getAll() {
        List<HistoryEntry> entries = new ArrayList<>();
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.query(AppDatabase.TABLE_HISTORY, null, null, null, null, null,
                AppDatabase.COL_TIMESTAMP + " DESC");
        try {
            while (cursor.moveToNext()) {
                entries.add(fromCursor(cursor));
            }
        } finally {
            cursor.close();
        }
        return entries;
    }

    public void deleteById(long id) {
        SQLiteDatabase db = database.getWritableDatabase();
        db.delete(AppDatabase.TABLE_HISTORY, AppDatabase.COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public void clear() {
        SQLiteDatabase db = database.getWritableDatabase();
        db.delete(AppDatabase.TABLE_HISTORY, null, null);
    }

    public void markReported(String message) {
        ContentValues values = new ContentValues();
        values.put(AppDatabase.COL_REPORTED, 1);
        SQLiteDatabase db = database.getWritableDatabase();
        db.update(AppDatabase.TABLE_HISTORY, values, AppDatabase.COL_MESSAGE + " = ?",
                new String[]{message});
    }

    private static HistoryEntry fromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabase.COL_ID));
        String message = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabase.COL_MESSAGE));
        float score = cursor.getFloat(cursor.getColumnIndexOrThrow(AppDatabase.COL_SCORE));
        String level = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabase.COL_LEVEL));
        String reasons = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabase.COL_REASONS));
        String engine = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabase.COL_ENGINE));
        boolean reported = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabase.COL_REPORTED)) == 1;
        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabase.COL_TIMESTAMP));
        return new HistoryEntry(id, message, score, RiskLevel.fromName(level), reasons,
                engine, reported, timestamp);
    }
}
