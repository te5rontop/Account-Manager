package com.ahmet.accountmanager.client;

public final class AccountState {

    private AccountState() {
    }

    public static boolean microsoftSignedIn = false;
    public static boolean minecraftProfileLoaded = false;
    public static boolean minecraftAuthorizationRejected = false;

    public static String microsoftAccount = "";
    public static String minecraftIgn = "";
    public static String minecraftUuid = "";

    public static String statusMessage = "Ready.";

    public static void clear() {

        microsoftSignedIn = false;
        minecraftProfileLoaded = false;
        minecraftAuthorizationRejected = false;

        microsoftAccount = "";
        minecraftIgn = "";
        minecraftUuid = "";
        statusMessage = "Account information cleared.";
    }
}