package com.antiarnaque.app.analysis;

import com.antiarnaque.app.model.AnalysisResult;
import com.antiarnaque.app.model.RiskLevel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests unitaires du classifieur de repli (aucun appareil requis). */
public final class HeuristicSmsAnalyzerTest {

    private final HeuristicSmsAnalyzer analyzer = new HeuristicSmsAnalyzer();

    @Test
    public void scamMessageIsDetectedAsHighRisk() throws Exception {
        AnalysisResult result = analyzer.analyze(
                "Félicitations ! Vous avez gagné 1 000 000 FCFA. "
                        + "Envoyez votre code de validation reçu par SMS au 3900 maintenant.");

        assertTrue("score attendu >= 0.75, obtenu " + result.getScore(), result.getScore() >= 0.75f);
        assertEquals(RiskLevel.ELEVE, result.getLevel());
        assertTrue(result.isSpam());
        assertFalse(result.getReasons().isEmpty());
    }

    @Test
    public void legitimateMessageHasLowRisk() throws Exception {
        AnalysisResult result = analyzer.analyze(
                "Votre colis arrive demain entre 9h et 12h. Merci de votre patience.");

        assertTrue("score attendu < 0.40, obtenu " + result.getScore(), result.getScore() < 0.40f);
        assertEquals(RiskLevel.FAIBLE, result.getLevel());
        assertFalse(result.isSpam());
    }

    @Test
    public void otpRequestIsFlagged() throws Exception {
        AnalysisResult result = analyzer.analyze(
                "Je suis l'agent de votre banque, communiquez le code reçu par SMS.");

        assertTrue(result.getScore() > 0.4f);
        assertTrue(result.getReasonsText().contains("code"));
    }

    @Test(expected = AnalysisException.class)
    public void emptyMessageIsRejected() throws Exception {
        analyzer.analyze("   ");
    }

    @Test
    public void scoreAlwaysStaysBetweenZeroAndOne() throws Exception {
        AnalysisResult result = analyzer.analyze(
                "Urgent ! Paiement mobile money, envoyez le code, lien http://bit.ly/x, "
                        + "compte bloqué, agent banque, gagnant, mot de passe, 12345678.");
        assertTrue(result.getScore() <= 1f && result.getScore() >= 0f);
        assertEquals("heuristique", analyzer.name());
    }
}
