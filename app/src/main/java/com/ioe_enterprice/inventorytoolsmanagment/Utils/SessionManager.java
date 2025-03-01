package com.ioe_enterprice.inventorytoolsmanagment.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserCache";
    private static final String KEY_USER_ID = "usuarioID";
    private static final String KEY_UBICACION_ID = "ubicacionID"; // Puedes agregar más datos aquí
    private static final String KEY_REMEMBER_ME = "rememberMe";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_ROL = "userRol";
    private static final String KEY_USER_EMAIL = "userEmail";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // 🔹 Guardar usuarioID en caché
    public void saveUserID(int userID) {
        editor.putInt(KEY_USER_ID, userID);
        editor.apply();
    }

    // 🔹 Obtener usuarioID
    public int getUserID() {
        return sharedPreferences.getInt(KEY_USER_ID, -1); // -1 si no existe
    }

    // 🔹 Guardar nombre de usuario en caché
    public void saveUserName(String userName) {
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    // 🔹 Obtener nombre de usuario
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, ""); // Cadena vacía si no existe
    }

    // 🔹 Guardar rol de usuario en caché
    public void saveUserRol(String userRol) {
        editor.putString(KEY_USER_ROL, userRol);
        editor.apply();
    }

    // 🔹 Obtener rol de usuario
    public String getUserRol() {
        return sharedPreferences.getString(KEY_USER_ROL, ""); // Cadena vacía si no existe
    }

    // 🔹 Guardar email de usuario en caché
    public void saveUserEmail(String userEmail) {
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.apply();
    }

    // 🔹 Obtener email de usuario
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, ""); // Cadena vacía si no existe
    }

    // 🔹 Guardar ubicacionID en caché
    public void saveUbicacionID(int ubicacionID) {
        editor.putInt(KEY_UBICACION_ID, ubicacionID);
        editor.apply();
    }

    // 🔹 Obtener ubicacionID
    public int getUbicacionID() {
        return sharedPreferences.getInt(KEY_UBICACION_ID, -1);
    }

    // 🔹 Limpiar sesión (logout)
    public void clearSession() {
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_UBICACION_ID);
        editor.remove(KEY_REMEMBER_ME);
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_ROL);
        editor.remove(KEY_USER_EMAIL);
        editor.apply();
    }

    // 🔹 Guardar el estado de "Recuérdame"
    public void setRememberMe(boolean rememberMe) {
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }

    // 🔹 Obtener el estado de "Recuérdame"
    public boolean isRememberMe() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false); // Por defecto es false
    }

    // 🔹 Guardar el estado de inicio de sesión
    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    // 🔹 Verificar si el usuario está logueado
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false); // Por defecto es false
    }

}