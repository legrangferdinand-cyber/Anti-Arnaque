package com.antiarnaque.app.model;

/** Une analyse enregistrée localement dans SQLite. */
public final class HistoryEntry {

    private long id;
    private final String message;
    private final float score;
    private final RiskLevel level;
    private final String reasons;
    private final String engine;
    private final boolean reported;
    private final long timestamp;

    public HistoryEntry(long id, String message, float score, RiskLevel level,
                        String reasons, String engine, boolean reported, long timestamp) {
        this.id = id;
        this.message = message;
        this.score = score;
        this.level = level;
        this.reasons = reasons;
        this.engine = engine;
        this.reported = reported;
        this.timestamp = timestamp;
    }

    public static HistoryEntry from(AnalysisResult result) {
        return new HistoryEntry(0L, result.getMessage(), result.getScore(), result.getLevel(),
                result.getReasonsText(), result.getEngine(), false, result.getTimestamp());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public float getScore() {
        return score;
    }

    public int getScorePercent() {
        return Math.round(score * 100f);
    }

    public RiskLevel getLevel() {
        return level;
    }

    public String getReasons() {
        return reasons;
    }

    public String getEngine() {
        return engine;
    }

    public boolean isReported() {
        return reported;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
