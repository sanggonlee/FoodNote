package com.example.foodnote;

public class Constants {
    static final String WEB_CLIENT_ID = "233735259795-16oe06vj7qbkks9id9uesuuhro2a282l.apps.googleusercontent.com";
    static final String ANDROID_AUDIENCE_ID = "server:client_id:" + WEB_CLIENT_ID;

    static final String ROOT_URL =
            //"https://localhost:8080/_ah/api";
            "https://polar-elevator-122617.appspot.com/_ah/api";

    static final String PREFS_NAME = "FoodNote";

    /*
     *  Key for the currently signed in account for the shared preferences
     */
    static final String ACCOUNT_NAME_SETTINGS_NAME = "accountName";
}
