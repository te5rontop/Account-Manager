package com.ahmet.accountmanager.client.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;



public final class MinecraftProfileService {


    private static final HttpClient HTTP_CLIENT =
            HttpClient.newHttpClient();

    private static volatile String activeMinecraftAccessToken;

    private static final HttpClient HTTP =
            HttpClient.newHttpClient();

    private MinecraftProfileService() {
    }

    public static MinecraftProfile getProfile(
            String microsoftAccessToken
    ) throws Exception {

        // 1. Microsoft token -> Xbox User Token
        XboxUserToken xbox = getXboxUserToken(
                microsoftAccessToken
        );

        // 2. Xbox User Token -> XSTS
        XstsToken xsts = getXstsToken(
                xbox.token()
        );

        // 3. XSTS -> Minecraft Services access
        String minecraftAccessToken =
                getMinecraftAccessToken(
                        xsts.userHash(),
                        xsts.token()
                );

        // 4. Minecraft profile
        return requestMinecraftProfile(
                minecraftAccessToken
        );
    }

    private static XboxUserToken getXboxUserToken(
            String microsoftAccessToken
    ) throws Exception {

        JsonObject properties = new JsonObject();

        properties.addProperty(
                "AuthMethod",
                "RPS"
        );

        properties.addProperty(
                "SiteName",
                "user.auth.xboxlive.com"
        );

        properties.addProperty(
                "RpsTicket",
                "d=" + microsoftAccessToken
        );

        JsonObject body = new JsonObject();

        body.addProperty(
                "RelyingParty",
                "http://auth.xboxlive.com"
        );

        body.addProperty(
                "TokenType",
                "JWT"
        );

        body.add(
                "Properties",
                properties
        );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "https://user.auth.xboxlive.com/user/authenticate"
                                )
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .header(
                                "x-xbl-contract-version",
                                "1"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        body.toString()
                                )
                        )
                        .build();

        HttpResponse<String> response =
                HTTP.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Xbox authentication failed: HTTP "
                            + response.statusCode()
            );
        }

        JsonObject json =
                JsonParser
                        .parseString(response.body())
                        .getAsJsonObject();

        String token =
                json.get("Token").getAsString();

        String userHash =
                json.getAsJsonObject("DisplayClaims")
                        .getAsJsonArray("xui")
                        .get(0)
                        .getAsJsonObject()
                        .get("uhs")
                        .getAsString();

        return new XboxUserToken(
                token,
                userHash
        );
    }

    private static XstsToken getXstsToken(
            String xboxToken
    ) throws Exception {

        JsonArray tokens = new JsonArray();
        tokens.add(xboxToken);

        JsonObject properties = new JsonObject();

        properties.addProperty(
                "SandboxId",
                "RETAIL"
        );

        properties.add(
                "UserTokens",
                tokens
        );

        JsonObject body = new JsonObject();

        body.add(
                "Properties",
                properties
        );

        body.addProperty(
                "RelyingParty",
                "rp://api.minecraftservices.com/"
        );

        body.addProperty(
                "TokenType",
                "JWT"
        );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "https://xsts.auth.xboxlive.com/xsts/authorize"
                                )
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .header(
                                "x-xbl-contract-version",
                                "1"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        body.toString()
                                )
                        )
                        .build();

        HttpResponse<String> response =
                HTTP.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "XSTS authentication failed: HTTP "
                            + response.statusCode()
            );
        }

        JsonObject json =
                JsonParser
                        .parseString(response.body())
                        .getAsJsonObject();

        String token =
                json.get("Token").getAsString();

        String userHash =
                json.getAsJsonObject("DisplayClaims")
                        .getAsJsonArray("xui")
                        .get(0)
                        .getAsJsonObject()
                        .get("uhs")
                        .getAsString();

        return new XstsToken(
                token,
                userHash
        );
    }

    private static String getMinecraftAccessToken(
            String userHash,
            String xstsToken
    ) throws Exception {

        JsonObject body = new JsonObject();

        body.addProperty(
                "identityToken",
                "XBL3.0 x="
                        + userHash
                        + ";"
                        + xstsToken
        );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "https://api.minecraftservices.com/authentication/login_with_xbox"
                                )
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        body.toString()
                                )
                        )
                        .build();

        HttpResponse<String> response =
                HTTP.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {

            if (response.statusCode() == 403) {
                throw new RuntimeException(
                        "Minecraft Services rejected this app registration."
                );
            }

            throw new RuntimeException(
                    "Minecraft authentication failed: HTTP "
                            + response.statusCode()
            );
        }

        JsonObject json =
                JsonParser
                        .parseString(response.body())
                        .getAsJsonObject();

        String minecraftAccessToken = json
                .get("access_token")
                .getAsString();

        activeMinecraftAccessToken = minecraftAccessToken;

        return minecraftAccessToken;
    }

    private static MinecraftProfile requestMinecraftProfile(
            String minecraftAccessToken
    ) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "https://api.minecraftservices.com/minecraft/profile"
                                )
                        )
                        .header(
                                "Authorization",
                                "Bearer " + minecraftAccessToken
                        )
                        .GET()
                        .build();

        HttpResponse<String> response =
                HTTP.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Minecraft profile failed: HTTP "
                            + response.statusCode()
            );
        }

        JsonObject json =
                JsonParser
                        .parseString(response.body())
                        .getAsJsonObject();

        String uuid =
                json.get("id").getAsString();

        String ign =
                json.get("name").getAsString();

        String skinUrl = "";

        JsonArray skins =
                json.getAsJsonArray("skins");

        if (skins != null && !skins.isEmpty()) {

            for (int i = 0; i < skins.size(); i++) {

                JsonObject skin =
                        skins
                                .get(i)
                                .getAsJsonObject();

                if (
                        skin.has("state")
                                && "ACTIVE".equals(
                                skin.get("state")
                                        .getAsString()
                        )
                ) {

                    skinUrl =
                            skin.get("url")
                                    .getAsString();

                    break;
                }
            }
        }

        return new MinecraftProfile(
                ign,
                uuid,
                skinUrl
        );
    }

    private record XboxUserToken(
            String token,
            String userHash
    ) {
    }

    private record XstsToken(
            String token,
            String userHash
    ) {
    }

    public record MinecraftProfile(
            String ign,
            String uuid,
            String skinUrl
    ) {
    }
    public static void uploadSkin(
            File skinFile,
            boolean slimModel
    ) throws Exception {

        if (activeMinecraftAccessToken == null
                || activeMinecraftAccessToken.isBlank()) {

            throw new IllegalStateException(
                    "No authenticated Minecraft session is available."
            );
        }

        if (skinFile == null || !skinFile.exists()) {
            throw new IllegalArgumentException(
                    "Skin file does not exist."
            );
        }

        String boundary =
                "----AccountManagerBoundary"
                        + System.currentTimeMillis();

        String variant =
                slimModel ? "slim" : "classic";

        byte[] fileBytes =
                Files.readAllBytes(skinFile.toPath());

        ByteArrayOutputStream body =
                new ByteArrayOutputStream();

        String header =
                "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"variant\"\r\n\r\n"
                        + variant + "\r\n"
                        + "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"file\"; filename=\"skin.png\"\r\n"
                        + "Content-Type: image/png\r\n\r\n";

        body.write(
                header.getBytes(StandardCharsets.UTF_8)
        );

        body.write(fileBytes);

        body.write(
                ("\r\n--" + boundary + "--\r\n")
                        .getBytes(StandardCharsets.UTF_8)
        );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "https://api.minecraftservices.com/minecraft/profile/skins"
                                )
                        )
                        .header(
                                "Authorization",
                                "Bearer " + activeMinecraftAccessToken
                        )
                        .header(
                                "Content-Type",
                                "multipart/form-data; boundary=" + boundary
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofByteArray(
                                        body.toByteArray()
                                )
                        )
                        .build();

        HttpResponse<String> response =
                HTTP_CLIENT.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new RuntimeException(
                    "Minecraft skin upload failed. HTTP "
                            + response.statusCode()
            );
        }
    }
    public static void clearSession() {
        activeMinecraftAccessToken = null;
    }

    public static boolean hasActiveMinecraftSession() {
        return activeMinecraftAccessToken != null
                && !activeMinecraftAccessToken.isBlank();
    }
    public static void changeMinecraftName(
            String newIgn
    ) throws Exception {

        if (activeMinecraftAccessToken == null
                || activeMinecraftAccessToken.isBlank()) {

            throw new IllegalStateException(
                    "No authenticated Minecraft session is available."
            );
        }

        if (newIgn == null
                || !newIgn.matches("[A-Za-z0-9_]{3,16}")) {

            throw new IllegalArgumentException(
                    "Minecraft IGN must be 3-16 characters and contain only letters, numbers, or _."
            );
        }

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "https://api.minecraftservices.com/minecraft/profile/name/"
                                                + newIgn
                                )
                        )
                        .header(
                                "Authorization",
                                "Bearer " + activeMinecraftAccessToken
                        )
                        .PUT(
                                HttpRequest.BodyPublishers.noBody()
                        )
                        .build();

        HttpResponse<String> response =
                HTTP_CLIENT.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        switch (response.statusCode()) {

            case 200 -> {
                return;
            }

            case 400 -> throw new IllegalArgumentException(
                    "The requested Minecraft IGN is invalid."
            );

            case 401 -> throw new IllegalStateException(
                    "Minecraft authentication expired."
            );

            case 403 -> throw new IllegalStateException(
                    "Minecraft name change is not currently allowed or the name is unavailable."
            );

            case 404 -> throw new IllegalStateException(
                    "Minecraft profile was not found."
            );

            case 429 -> throw new IllegalStateException(
                    "Too many name change requests. Try again later."
            );

            default -> throw new IllegalStateException(
                    "Minecraft name change failed. HTTP "
                            + response.statusCode()
            );
        }
    }
}
