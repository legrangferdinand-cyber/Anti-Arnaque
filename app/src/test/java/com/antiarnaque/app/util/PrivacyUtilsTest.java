package com.antiarnaque.app.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public final class PrivacyUtilsTest {

    @Test
    public void hashIsStableAndDoesNotContainTheMessage() {
        String hash = PrivacyUtils.sha256("Vous avez gagné !");
        assertEquals(64, hash.length());
        assertEquals(hash, PrivacyUtils.sha256("Vous avez gagné !"));
        assertNotEquals("Vous avez gagné !", hash);
    }
}
