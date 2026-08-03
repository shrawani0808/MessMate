package com.example.messmate.presentation.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "MessMateSession";

    private static final String KEY_LOGGED_IN = "loggedIn";
    private static final String KEY_UID = "uid";
    private static final String KEY_ROLE = "role";
    private static final String KEY_EMAIL = "email";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {

        preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    public void saveLoginSession(String uid, String email, String role) {

        preferences.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_UID, uid)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public boolean isLoggedIn() {

        return preferences.getBoolean(KEY_LOGGED_IN, false);
    }

    public String getUid() {

        return preferences.getString(KEY_UID, null);
    }

    public String getEmail() {

        return preferences.getString(KEY_EMAIL, null);
    }

    public String getRole() {

        return preferences.getString(KEY_ROLE, null);
    }

    public void logout() {

        preferences.edit().clear().apply();
    }
}