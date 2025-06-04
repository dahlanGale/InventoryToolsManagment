package com.ioe_enterprice.inventorytoolsmanagment.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Utility class to store and retrieve user information throughout the app.
 * Uses static fields for immediate access and SharedPreferences for persistence.
 */
public class UserCache {
    private static final String TAG = "UserCache";
    
    // SharedPreferences keys
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_SUCURSAL_ID = "sucursal_id";
    private static final String KEY_SUCURSAL_NOMBRE = "sucursal_nombre";
    
    // In-memory cache
    private static int userID = -1;
    private static String username = "";
    private static int sucursalID = -1;
    private static String sucursalNombre = "";
    
    /**
     * Initialize the cache from SharedPreferences
     * @param context Application context
     */
    public static void init(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            userID = prefs.getInt(KEY_USER_ID, -1);
            username = prefs.getString(KEY_USERNAME, "");
            sucursalID = prefs.getInt(KEY_SUCURSAL_ID, -1);
            sucursalNombre = prefs.getString(KEY_SUCURSAL_NOMBRE, "");
            
            Log.d(TAG, "UserCache initialized. UserID: " + userID + ", SucursalID: " + sucursalID);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing UserCache", e);
        }
    }
    
    /**
     * Save user data to cache and SharedPreferences
     * @param context Application context
     * @param userID User ID
     * @param username Username
     * @param sucursalID Branch ID
     * @param sucursalNombre Branch name
     */
    public static void saveUserData(Context context, int userID, String username, int sucursalID, String sucursalNombre) {
        // Update in-memory cache
        UserCache.userID = userID;
        UserCache.username = username;
        UserCache.sucursalID = sucursalID;
        UserCache.sucursalNombre = sucursalNombre;
        
        // Update SharedPreferences
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_USER_ID, userID);
            editor.putString(KEY_USERNAME, username);
            editor.putInt(KEY_SUCURSAL_ID, sucursalID);
            editor.putString(KEY_SUCURSAL_NOMBRE, sucursalNombre);
            editor.apply();
            
            Log.d(TAG, "User data saved. UserID: " + userID + ", SucursalID: " + sucursalID);
        } catch (Exception e) {
            Log.e(TAG, "Error saving user data", e);
        }
    }
    
    /**
     * Clear all user data from cache and SharedPreferences
     * @param context Application context
     */
    public static void clearUserData(Context context) {
        // Clear in-memory cache
        userID = -1;
        username = "";
        sucursalID = -1;
        sucursalNombre = "";
        
        // Clear SharedPreferences
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            
            Log.d(TAG, "User data cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing user data", e);
        }
    }
    
    // Getters
    public static int getUserID() {
        return userID;
    }
    
    public static String getUsername() {
        return username;
    }
    
    public static int getSucursalID() {
        return sucursalID;
    }
    
    public static String getSucursalNombre() {
        return sucursalNombre;
    }
    
    /**
     * Check if a user is logged in
     * @return true if there is a user logged in, false otherwise
     */
    public static boolean isLoggedIn() {
        return userID > 0;
    }
}
