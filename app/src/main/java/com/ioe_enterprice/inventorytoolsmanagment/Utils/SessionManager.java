package com.ioe_enterprice.inventorytoolsmanagment.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserCache";
    private static final String KEY_USER_ID = "usuarioID";
    private static final String KEY_UBICACION_ID = "ubicacionID"; // Puedes agregar más datos aquí
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
        editor.clear();
        editor.apply();
    }
}