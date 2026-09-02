package com.ahmet.accountmanager.client.auth;

/**
 * Thread-safe in-memory authentication state.
 *
 * <p>The generation value changes whenever a new sign-in begins or the user
 * clears the account. Async work can capture a generation and discard stale
 * results instead of restoring tokens from an older session.</p>
 */
final class AuthSession {

    private static long generation;
    private static String microsoftAccessToken;
    private static String minecraftAccessToken;

    private AuthSession() {
    }

    static synchronized long beginSignIn() {
        generation++;
        microsoftAccessToken = null;
        minecraftAccessToken = null;
        return generation;
    }

    static synchronized long currentGeneration() {
        return generation;
    }

    static synchronized boolean isCurrent(long expectedGeneration) {
        return generation == expectedGeneration;
    }

    static synchronized boolean setMicrosoftAccessToken(
            long expectedGeneration,
            String accessToken
    ) {
        if (generation != expectedGeneration) {
            return false;
        }

        microsoftAccessToken = accessToken;
        return true;
    }

    static synchronized boolean setMinecraftAccessToken(
            long expectedGeneration,
            String accessToken
    ) {
        if (generation != expectedGeneration) {
            return false;
        }

        minecraftAccessToken = accessToken;
        return true;
    }

    static synchronized TokenSnapshot microsoftTokenSnapshot() {
        return new TokenSnapshot(generation, microsoftAccessToken);
    }

    static synchronized TokenSnapshot minecraftTokenSnapshot() {
        return new TokenSnapshot(generation, minecraftAccessToken);
    }

    static synchronized boolean hasMicrosoftAccessToken() {
        return microsoftAccessToken != null
                && !microsoftAccessToken.isBlank();
    }

    static synchronized boolean hasMinecraftAccessToken() {
        return minecraftAccessToken != null
                && !minecraftAccessToken.isBlank();
    }

    static synchronized void clearMinecraftAccessToken() {
        minecraftAccessToken = null;
    }

    static synchronized void clearAll() {
        generation++;
        microsoftAccessToken = null;
        minecraftAccessToken = null;
    }

    record TokenSnapshot(long generation, String token) {
        boolean isPresent() {
            return token != null && !token.isBlank();
        }
    }
}
