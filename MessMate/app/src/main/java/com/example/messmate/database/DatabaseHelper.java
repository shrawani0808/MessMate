package com.example.messmate.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.messmate.models.MessModel;
import com.example.messmate.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MessMate.db";
    // Bumped version to 2 to trigger onUpgrade and create the new table
    private static final int DATABASE_VERSION = 2;

    // Table Names
    private static final String TABLE_MESS = "messes";
    private static final String TABLE_ORDERS = "orders";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Mess Table
        String CREATE_MESS_TABLE = "CREATE TABLE " + TABLE_MESS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT, "
                + "rating REAL, "
                + "rating_count INTEGER, "
                + "type TEXT, "
                + "price REAL, "
                + "location TEXT)";
        db.execSQL(CREATE_MESS_TABLE);

        // Create Orders Table
        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + " ("
                + "order_id TEXT PRIMARY KEY, "
                + "mess_name TEXT, "
                + "plan_type TEXT, "
                + "meal_type TEXT, "
                + "status TEXT, "
                + "time TEXT)";
        db.execSQL(CREATE_ORDERS_TABLE);

        // Seed Mess Data
        db.execSQL("INSERT INTO " + TABLE_MESS + " VALUES (1, 'Green House Mess', 4.5, 120, 'Pure Veg', 3000, 'Kothrud, Pune')");
        db.execSQL("INSERT INTO " + TABLE_MESS + " VALUES (2, 'Sai Tiffin Service', 4.3, 96, 'Veg & Non-Veg', 2600, 'Kothrud, Pune')");
        db.execSQL("INSERT INTO " + TABLE_MESS + " VALUES (3, 'Student Meals', 4.6, 203, 'Pure Veg', 3200, 'Karve Nagar, Pune')");

        // Seed Orders Data
        db.execSQL("INSERT INTO " + TABLE_ORDERS + " VALUES ('101', 'Green House Mess', 'Monthly Plan', 'Lunch', 'Confirmed', '1:30 PM')");
        db.execSQL("INSERT INTO " + TABLE_ORDERS + " VALUES ('102', 'Sai Tiffin Service', 'Weekly Plan', 'Dinner', 'Preparing', '8:00 PM')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        onCreate(db);
    }

    // Fetch all Messes
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

    // Fetch all Orders
    public List<OrderItem> getAllOrders() {
        List<OrderItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ORDERS, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new OrderItem(
                        cursor.getString(0), // orderId
                        cursor.getString(1), // messName
                        cursor.getString(2), // planType
                        cursor.getString(3), // mealType
                        cursor.getString(4), // status
                        cursor.getString(5)  // time
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}