package com.b183237x.signaltracker;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class SignalTrackerDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "SignalTracker";
    private static final int DB_VERSION = 1;


    SignalTrackerDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the READINGS table
        db.execSQL("CREATE TABLE READINGS ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "latitude REAL, "
                + "longitude REAL, "
                + "signal_type INTEGER, "
                + "signal_value INTEGER, "
                + "uploaded INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
