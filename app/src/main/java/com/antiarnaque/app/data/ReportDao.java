package com.antiarnaque.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.antiarnaque.app.model.ReportEntry;

import java.util.ArrayList;
import java.util.List;

/** File d'attente locale des signalements (sync FIFO). */
public final class ReportDao {

    private final AppDatabase database;

    public ReportDao(Context context) {
        this.database = AppDatabase.getInstance(context);
    }

    public long insert(ReportEntry report) {
        ContentValues values = new ContentValues();
        values.put(AppDatabase.COL_HASH, report.getMessageHash());
        values.put(AppDatabase.COL_SCORE, report.getScore());
        values.put(AppDatabase.COL_REASON, report.getReason());
        values.put(AppDatabase.COL_SYNCED, report.isSynced() ? 1 : 0);
        values.put(AppDatabase.COL_TIMESTAMP, report.getTimestamp());

        SQLiteDatabase db = database.getWritableDatabase();
        return db.insert(AppDatabase.TABLE_REPORTS, null, values);
    }

    public List<ReportEntry> getPending() {
        List<ReportEntry> reports = new ArrayList<>();
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.query(AppDatabase.TABLE_REPORTS, null,
                AppDatabase.COL_SYNCED + " = 0", null, null, null,
                AppDatabase.COL_TIMESTAMP + " ASC");
        try {
            while (cursor.moveToNext()) {
                ReportEntry entry = fromCursor(cursor);
                reports.add(entry);
            }
        } finally {
            cursor.close();
        }
        return reports;
    }

    public int countAll() {
        return count("1 = 1");
    }

    public int countPending() {
        return count(AppDatabase.COL_SYNCED + " = 0");
    }

    public int countSynced() {
        return count(AppDatabase.COL_SYNCED + " = 1");
    }

    public void markSynced(long id) {
        ContentValues values = new ContentValues();
        values.put(AppDatabase.COL_SYNCED, 1);
        SQLiteDatabase db = database.getWritableDatabase();
        db.update(AppDatabase.TABLE_REPORTS, values, AppDatabase.COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    private int count(String where) {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + AppDatabase.TABLE_REPORTS
                + " WHERE " + where, null);
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    private static ReportEntry fromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabase.COL_ID));
        String hash = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabase.COL_HASH));
        float score = cursor.getFloat(cursor.getColumnIndexOrThrow(AppDatabase.COL_SCORE));
        String reason = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabase.COL_REASON));
        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabase.COL_TIMESTAMP));
        boolean synced = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabase.COL_SYNCED)) == 1;
        return new ReportEntry(id, hash, score, reason, timestamp, synced);
    }
}
