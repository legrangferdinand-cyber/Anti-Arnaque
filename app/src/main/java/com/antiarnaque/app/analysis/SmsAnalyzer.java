package com.antiarnaque.app.analysis;

import com.antiarnaque.app.model.AnalysisResult;

/**
 * Contrat commun à toutes les implémentations :
 * - heuristique locale (fonctionne sans réseau),
 * - modèle TFLite embarqué (à brancher plus tard),
 * - API Flask distante.
 */
public interface SmsAnalyzer {

    AnalysisResult analyze(String message) throws AnalysisException;

    String name();
}
