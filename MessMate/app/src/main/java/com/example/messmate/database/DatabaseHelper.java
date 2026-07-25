package com.example.messmate.database;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.messmate.models.MessModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MessMate.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESS = "messes";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MESS_TABLE = "CREATE TABLE " + TABLE_MESS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT, "
                + "rating REAL, "
                + "rating_count INTEGER, "
                + "type TEXT, "
                + "price REAL, "
                + "location TEXT)";
        db.execSQL(CREATE_MESS_TABLE);

        // Populate seed data based on your UI mockup
        db.execSQL("INSERT INTO " + TABLE_MESS + " VALUES (1, 'Green House Mess', 4.5, 120, 'Pure Veg', 3000, 'Kothrud, Pune')");
        db.execSQL("INSERT INTO " + TABLE_MESS + " VALUES (2, 'Sai Tiffin Service', 4.3, 96, 'Veg & Non-Veg', 2600, 'Kothrud, Pune')");
        db.execSQL("INSERT INTO " + TABLE_MESS + " VALUES (3, 'Student Meals', 4.6, 203, 'Pure Veg', 3200, 'Karve Nagar, Pune')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESS);
        onCreate(db);
    }

    public List<MessModel> getAllMesses() {
        List<MessModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESS, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new MessModel(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getFloat(2),
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getDouble(5),
                        cursor.getString(6)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}