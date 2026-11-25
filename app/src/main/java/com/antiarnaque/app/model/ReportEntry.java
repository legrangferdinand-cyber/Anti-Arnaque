package com.antiarnaque.app.model;

/**
 * Signalement collectif. Le message n'est jamais envoyé en clair :
 * seule une empreinte SHA-256 est transmise au serveur.
 */
public final class ReportEntry {

    private long id;
    private final String messageHash;
    private final float score;
    private final String reason;
    private final long timestamp;
    private boolean synced;

    public ReportEntry(long id, String messageHash, float score, String reason,
                       long timestamp, boolean synced) {
        this.id = id;
        this.messageHash = messageHash;
        this.score = score;
        this.reason = reason;
        this.timestamp = timestamp;
        this.synced = synced;
    }

    public ReportEntry(String messageHash, float score, String reason, long timestamp) {
        this(0L, messageHash, score, reason, timestamp, false);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public float getScore() {
        return score;
    }

    public String getReason() {
        return reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }
}
