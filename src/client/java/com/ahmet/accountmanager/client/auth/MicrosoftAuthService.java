package com.ahmet.accountmanager.client.auth;

import com.ahmet.accountmanager.client.AccountState;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SystemBrowserOptions;
import net.minecraft.util.Util;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

public final class MicrosoftAuthService {

    private static final String AUTHORITY =
            "https://login.microsoftonline.com/consumers";

    private static final URI REDIRECT_URI =
            URI.create("http://localhost");

    private static final Set<String> SCOPES =
            Set.of("XboxLive.signin");

    private MicrosoftAuthService() {
    }

    public static CompletableFuture<LoginResult> signIn() {

        // Starting a new sign-in invalidates all previous async auth work and
        // clears any older Minecraft token before another account is used.
        long sessionGeneration = AuthSession.beginSignIn();

        try {

            PublicClientApplication application =
                    PublicClientApplication
                            .builder(AuthConfig.CLIENT_ID)
                            .authority(AUTHORITY)
                            .build();

            SystemBrowserOptions browserOptions =
                    SystemBrowserOptions.builder()
                            .openBrowserAction(url ->
                                    Util.getPlatform().openUri(
                                            URI.create(url.toString())
                                    )
                            )
                            .build();

            InteractiveRequestParameters parameters =
                    InteractiveRequestParameters
                            .builder(REDIRECT_URI)
                            .scopes(SCOPES)
                            .systemBrowserOptions(browserOptions)
                            .build();

            return application
                    .acquireToken(parameters)
                    .thenApply(result -> {

                        if (!AuthSession.setMicrosoftAccessToken(
                                sessionGeneration,
                                result.accessToken()
                        )) {
                            throw new CancellationException(
                                    "Authentication session changed."
                            );
                        }

                        try {
                            MinecraftProfileService.MinecraftProfile profile =
                                    MinecraftProfileService.getProfile(
                                            result.accessToken(),
                                            sessionGeneration
                                    );

                            ensureCurrentSession(sessionGeneration);

                            return new LoginResult(
                                    true,
                                    true,
                                    result.account() != null
                                            ? result.account().username()
                                            : null,
                                    profile.ign(),
                                    profile.uuid(),
                                    profile.skinUrl(),
                                    "Minecraft profile loaded."
                            );

                        } catch (CancellationException exception) {
                            throw exception;

                        } catch (Exception exception) {

                            ensureCurrentSession(sessionGeneration);

                            String errorMessage =
                                    exception.getMessage() == null
                                            ? ""
                                            : exception.getMessage();

                            String lowerMessage =
                                    errorMessage.toLowerCase();

                            boolean authorizationRejected =
                                    errorMessage.contains("403")
                                            || lowerMessage.contains("app registration")
                                            || lowerMessage.contains("not authorized");

                            return new LoginResult(
                                    true,
                                    false,
                                    result.account() != null
                                            ? result.account().username()
                                            : null,
                                    null,
                                    null,
                                    null,
                                    authorizationRejected
                                            ? "Minecraft Services rejected this app registration."
                                            : "Minecraft profile could not be loaded."
                            );
                        }
                    });

        } catch (Exception exception) {

            // If setup itself fails, invalidate the generation that was just
            // created so no partially-started login can later restore state.
            if (AuthSession.isCurrent(sessionGeneration)) {
                AuthSession.clearAll();
            }

            return CompletableFuture.completedFuture(
                    new LoginResult(
                            false,
                            false,
                            null,
                            null,
                            null,
                            null,
                            "Could not start Microsoft login."
                    )
            );
        }
    }

    public record LoginResult(
            boolean microsoftSignedIn,
            boolean minecraftProfileLoaded,
            String microsoftUsername,
            String minecraftIgn,
            String minecraftUuid,
            String skinUrl,
            String message
    ) {
    }

    public static boolean canRefreshMinecraftProfile() {
        return AuthSession.hasMicrosoftAccessToken();
    }

    public static CompletableFuture<LoginResult> refreshMinecraftProfile() {

        AuthSession.TokenSnapshot snapshot =
                AuthSession.microsoftTokenSnapshot();

        if (!snapshot.isPresent()) {
            return CompletableFuture.completedFuture(
                    new LoginResult(
                            false,
                            false,
                            null,
                            null,
                            null,
                            null,
                            "Microsoft sign-in is required."
                    )
            );
        }

        return CompletableFuture.supplyAsync(() -> {

            ensureCurrentSession(snapshot.generation());

            try {

                MinecraftProfileService.MinecraftProfile profile =
                        MinecraftProfileService.getProfile(
                                snapshot.token(),
                                snapshot.generation()
                        );

                ensureCurrentSession(snapshot.generation());

                return new LoginResult(
                        true,
                        true,
                        AccountState.microsoftAccount.isBlank()
                                ? null
                                : AccountState.microsoftAccount,
                        profile.ign(),
                        profile.uuid(),
                        profile.skinUrl(),
                        "Minecraft profile refreshed."
                );

            } catch (CancellationException exception) {
                throw exception;

            } catch (Exception exception) {

                ensureCurrentSession(snapshot.generation());

                String errorMessage =
                        exception.getMessage() == null
                                ? ""
                                : exception.getMessage();

                String lowerMessage =
                        errorMessage.toLowerCase();

                boolean authorizationRejected =
                        errorMessage.contains("403")
                                || lowerMessage.contains("app registration")
                                || lowerMessage.contains("not authorized");

                return new LoginResult(
                        true,
                        false,
                        AccountState.microsoftAccount.isBlank()
                                ? null
                                : AccountState.microsoftAccount,
                        null,
                        null,
                        null,
                        authorizationRejected
                                ? "Minecraft Services rejected this app registration."
                                : "Minecraft profile refresh failed."
                );
            }
        });
    }

    public static void clearSession() {
        AuthSession.clearAll();
    }

    private static void ensureCurrentSession(long expectedGeneration) {
        if (!AuthSession.isCurrent(expectedGeneration)) {
            throw new CancellationException(
                    "Authentication session changed."
            );
        }
    }
}
