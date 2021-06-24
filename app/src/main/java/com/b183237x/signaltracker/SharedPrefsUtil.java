package com.b183237x.signaltracker;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SharedPrefsUtil {

    private static SharedPreferences sharedPreferences;


    private static SharedPreferences getSharedPrefs(Context context) throws GeneralSecurityException, IOException {

        String masterKeyAlias = "";

        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

        sharedPreferences = EncryptedSharedPreferences.create(
                "SignalTrackerPrefs",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        return sharedPreferences;
    }


    public static String getPrefVal(Context context, String key) throws GeneralSecurityException, IOException {
        SharedPreferences sharedPreferences = getSharedPrefs(context);
        String value = "";
        if (sharedPreferences.contains(key)) {
            value = sharedPreferences.getString(key, "");
        }
        return value;
    }


    public static void setPrefVal(Context context, String key, String value) throws GeneralSecurityException, IOException {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(key, value);
        editor.commit();
    }



}
