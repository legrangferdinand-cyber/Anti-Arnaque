package com.antiarnaque.app.report;

import com.antiarnaque.app.model.ReportEntry;

/**
 * Canal de signalement collectif. Deux implémentations sont prévues :
 * - HttpReportService (Flask / Cloud Function) — active par défaut si une URL est saisie,
 * - FirebaseReportService (Firestore) — voir docs/INTEGRATION_FIREBASE.md.
 */
public interface ReportService {

    /** @return true si le signalement a bien été reçu par le serveur. */
    boolean send(ReportEntry report) throws Exception;
}
