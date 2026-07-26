package com.example.messmate.common.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.messmate.common.models.MessModel;
import com.example.messmate.common.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MessMate.db";
    private static final int DATABASE_VERSION = 6; // Bumped version for orders table

    private static final String TABLE_USERS = "users";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_MESSES = "messes";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users Table
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE, "
                + "phone TEXT, password TEXT, role TEXT)");

        // Orders Table
        db.execSQL("CREATE TABLE " + TABLE_ORDERS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, user_email TEXT, mess_name TEXT, "
                + "plan_name TEXT, price TEXT, status TEXT, date TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_MESSES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, rating REAL, "
                + "rating_count INTEGER, type TEXT, price_per_month REAL, location TEXT)");

        // Pre-seed Admin and Owner accounts
        db.execSQL("INSERT INTO " + TABLE_USERS + " (name, email, phone, password, role) "
                + "VALUES ('Admin User', 'admin@messmate.com', '9999999999', 'admin123', 'ADMIN')");

        db.execSQL("INSERT INTO " + TABLE_USERS + " (name, email, phone, password, role) "
                + "VALUES ('Mess Owner', 'owner@messmate.com', '8888888888', 'owner123', 'OWNER')");

        db.execSQL("INSERT INTO " + TABLE_MESSES + " (name, rating, rating_count, type, price_per_month, location) "
                + "VALUES ('Annapurna Mess', 4.5, 120, 'Pure Veg', 2500.0, 'Near Main Gate')");

        db.execSQL("INSERT INTO " + TABLE_MESSES + " (name, rating, rating_count, type, price_per_month, location) "
                + "VALUES ('Royal Mess', 4.2, 85, 'Veg / Non-Veg', 3000.0, 'College Road')");

        db.execSQL("INSERT INTO " + TABLE_MESSES + " (name, rating, rating_count, type, price_per_month, location) "
                + "VALUES ('Student Kitchen', 4.0, 45, 'Pure Veg', 2200.0, 'Hostel Area')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSES);
        onCreate(db);
    }

    public boolean registerUser(String name, String email, String phone, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("phone", phone);
        values.put("password", password);
        values.put("role", role);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE email=? AND password=?", new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT name, email, phone FROM " + TABLE_USERS + " WHERE email=?", new String[]{email});
    }

    public String getUserRole(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT role FROM " + TABLE_USERS + " WHERE email=?", new String[]{email});
        String role = "USER";
        if (cursor.moveToFirst()) {
            role = cursor.getString(0);
        }
        cursor.close();
        return role;
    }



    // --- ORDERS METHODS ---
    public boolean insertMess(String name, float rating, int ratingCount, String type, double pricePerMonth, String location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("rating", rating);
        values.put("rating_count", ratingCount);
        values.put("type", type);
        values.put("price_per_month", pricePerMonth);
        values.put("location", location);

        long result = db.insert(TABLE_MESSES, null, values);
        return result != -1;
    }

    public List<MessModel> getAllMesses() {
        List<MessModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESSES + " ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                float rating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating"));
                int ratingCount = cursor.getInt(cursor.getColumnIndexOrThrow("rating_count"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                double pricePerMonth = cursor.getDouble(cursor.getColumnIndexOrThrow("price_per_month"));
                String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));

                list.add(new MessModel(id, name, rating, ratingCount, type, pricePerMonth, location));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }




    // --- ORDERS METHODS ---

    public boolean placeOrder(String userEmail, String messName, String planName, String price, String status, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_email", userEmail);
        values.put("mess_name", messName);
        values.put("plan_name", planName);
        values.put("price", price);
        values.put("status", status);
        values.put("date", date);

        long result = db.insert(TABLE_ORDERS, null, values);
        return result != -1;
    }

    public List<OrderItem> getAllOrders() {
        List<OrderItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String orderId = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                String userEmail = cursor.getString(cursor.getColumnIndexOrThrow("user_email"));
                String messName = cursor.getString(cursor.getColumnIndexOrThrow("mess_name"));
                String planType = cursor.getString(cursor.getColumnIndexOrThrow("plan_name"));
                String price = cursor.getString(cursor.getColumnIndexOrThrow("price"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String mealType = "Veg/Non-Veg"; // Or fetch from database column if available

                list.add(new OrderItem(orderId, messName, planType, mealType, status, time, userEmail, price));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

}