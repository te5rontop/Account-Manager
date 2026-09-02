package com.ahmet.accountmanager.client.auth;

import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SystemBrowserOptions;
import net.minecraft.util.Util;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import com.ahmet.accountmanager.client.AccountState;

public final class MicrosoftAuthService {
    private static volatile String activeMicrosoftAccessToken;

    private static final String AUTHORITY =
            "https://login.microsoftonline.com/consumers";

    private static final URI REDIRECT_URI =
            URI.create("http://localhost");

    private static final Set<String> SCOPES =
            Set.of("XboxLive.signin");

    private MicrosoftAuthService() {
    }

    public static CompletableFuture<LoginResult> signIn() {

        try {

            PublicClientApplication application =
                    PublicClientApplication
                            .builder(AuthConfig.CLIENT_ID)
                            .authority(AUTHORITY)
                            .build();

            SystemBrowserOptions browserOptions =
                    SystemBrowserOptions.builder()
                            .openBrowserAction(url -> {
                                Util.getPlatform().openUri(
                                        URI.create(url.toString())
                                );
                            })
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

                        try {
                            activeMicrosoftAccessToken =
                                    result.accessToken();

                            MinecraftProfileService.MinecraftProfile profile =
                                    MinecraftProfileService.getProfile(
                                            result.accessToken()
                                    );

                            return new LoginResult(
                                    true,
                                    true,
                                    result.account().username(),
                                    profile.ign(),
                                    profile.uuid(),
                                    profile.skinUrl(),
                                    "Minecraft profile loaded."
                            );

                        } catch (Exception exception) {

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
    ) {}
        public static boolean canRefreshMinecraftProfile() {

            return activeMicrosoftAccessToken != null
                    && !activeMicrosoftAccessToken.isBlank();
        }
        public static CompletableFuture<LoginResult> refreshMinecraftProfile() {

            return CompletableFuture.supplyAsync(() -> {

                if (activeMicrosoftAccessToken == null
                        || activeMicrosoftAccessToken.isBlank()) {

                    return new LoginResult(
                            false,
                            false,
                            null,
                            null,
                            null,
                            null,
                            "Microsoft sign-in is required."
                    );
                }

                try {

                    MinecraftProfileService.MinecraftProfile profile =
                            MinecraftProfileService.getProfile(
                                    activeMicrosoftAccessToken
                            );

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

                } catch (Exception exception) {

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
        activeMicrosoftAccessToken = null;
    }

    }
