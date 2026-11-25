package com.antiarnaque.app.report;

import android.content.Context;
import android.text.TextUtils;

import com.antiarnaque.app.data.HistoryDao;
import com.antiarnaque.app.data.PreferencesManager;
import com.antiarnaque.app.data.ReportDao;
import com.antiarnaque.app.model.AnalysisResult;
import com.antiarnaque.app.model.ReportEntry;
import com.antiarnaque.app.util.PrivacyUtils;

import java.util.List;

/** Orchestration : enregistrement local, anonymisation, envoi et file d'attente. */
public final class ReportManager {

    private final ReportDao reportDao;
    private final HistoryDao historyDao;
    private final PreferencesManager preferences;

    public ReportManager(Context context) {
        this.reportDao = new ReportDao(context);
        this.historyDao = new HistoryDao(context);
        this.preferences = new PreferencesManager(context);
    }

    /** Enregistre le signalement localement puis tente un envoi immédiat. */
    public ReportResult submit(String message, AnalysisResult result) {
        String hash = PrivacyUtils.sha256(message.trim().toLowerCase(java.util.Locale.ROOT));
        String reason = result.getReasons().isEmpty()
                ? "analysé comme " + result.getLabel()
                : result.getReasons().get(0);
        ReportEntry entry = new ReportEntry(hash, result.getScore(), reason, System.currentTimeMillis());
        long id = reportDao.insert(entry);
        historyDao.markReported(message.trim());

        boolean sent = false;
        String endpoint = preferences.getReportBaseUrl();
        if (!TextUtils.isEmpty(endpoint)) {
            try {
                sent = new HttpReportService(endpoint).send(entry);
                if (sent) {
                    reportDao.markSynced(id);
                }
            } catch (Exception ignored) {
                // Le signalement reste en file d'attente et sera rejoué plus tard.
            }
        }
        return new ReportResult(sent, sent);
    }

    /** Rejoue les signalements non synchronisés. */
    public SyncResult syncPending() {
        String endpoint = preferences.getReportBaseUrl();
        if (TextUtils.isEmpty(endpoint)) {
            return new SyncResult(0, 0, false);
        }
        List<ReportEntry> pending = reportDao.getPending();
        int success = 0;
        for (ReportEntry entry : pending) {
            try {
                if (new HttpReportService(endpoint).send(entry)) {
                    reportDao.markSynced(entry.getId());
                    success++;
                }
            } catch (Exception ignored) {
                // On continue avec les autres entrées.
            }
        }
        return new SyncResult(success, pending.size(), true);
    }

    public int countAll() {
        return reportDao.countAll();
    }

    public int countPending() {
        return reportDao.countPending();
    }

    public int countSynced() {
        return reportDao.countSynced();
    }

    /** Résultat d'un signalement : le booléen indique si le serveur l'a reçu. */
    public static final class ReportResult {
        public final boolean stored;
        public final boolean sent;

        ReportResult(boolean stored, boolean sent) {
            this.stored = stored;
            this.sent = sent;
        }
    }

    public static final class SyncResult {
        public final int success;
        public final int total;
        public final boolean configured;

        SyncResult(int success, int total, boolean configured) {
            this.success = success;
            this.total = total;
            this.configured = configured;
        }
    }
}
