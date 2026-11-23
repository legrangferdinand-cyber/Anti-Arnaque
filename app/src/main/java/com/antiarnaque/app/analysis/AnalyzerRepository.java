package com.antiarnaque.app.analysis;

import android.content.Context;
import android.text.TextUtils;

import com.antiarnaque.app.data.PreferencesManager;
import com.antiarnaque.app.model.AnalysisResult;

/**
 * Choix du moteur d'analyse : modèle distant si configuré, heuristique locale sinon.
 * En cas d'échec du réseau, l'heuristique prend le relais et l'information est
 * conservée dans le résultat (transparence vis-à-vis de l'utilisateur).
 */
public final class AnalyzerRepository {

    private final PreferencesManager preferences;
    private final HeuristicSmsAnalyzer heuristic = new HeuristicSmsAnalyzer();

    public AnalyzerRepository(Context context) {
        this.preferences = new PreferencesManager(context);
    }

    public AnalysisResult analyze(String message) throws AnalysisException {
        String baseUrl = preferences.getApiBaseUrl();
        if (preferences.isRemoteAnalysisEnabled() && !TextUtils.isEmpty(baseUrl)) {
            try {
                return new RemoteSmsAnalyzer(baseUrl).analyze(message);
            } catch (Exception e) {
                AnalysisResult fallback = heuristic.analyze(message);
                fallback.setEngine("heuristique locale (serveur indisponible)");
                return fallback;
            }
        }
        return heuristic.analyze(message);
    }
}
