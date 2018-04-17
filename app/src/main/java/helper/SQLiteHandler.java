package helper;

/**
 * Created by hennethcheng on 17/12/16.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Login table name
    private static final String TABLE_USER = "user";
    private static final String TABLE_MAC_ADDRESSES = "mac_addresses";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_UID = "uid";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_MAC_ADDRESS = "mac_address";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_USERNAME + " TEXT,"
                + KEY_UID + " TEXT"+ ")";
        String CREATE_MAC_ADDRESSES_TABLE = "CREATE TABLE " + TABLE_MAC_ADDRESSES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_USER_ID + " INTEGER,"
                + KEY_MAC_ADDRESS + " TEXT"+ ")";
        db.execSQL(CREATE_LOGIN_TABLE);
        db.execSQL(CREATE_MAC_ADDRESSES_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAC_ADDRESSES);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details and mac addresses in database
     * */
    public void addUserAndMacAddresses(String username, String uid, ArrayList<String> mac_addresses) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, username); // Username
        values.put(KEY_UID, uid); // Email

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
//        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);

        for(String mac_address: mac_addresses) {
            values = new ContentValues();
            values.put(KEY_USER_ID, id); // User ID
            values.put(KEY_MAC_ADDRESS, mac_address); // Mac Addresses

            // Inserting Row
            long new_id = db.insert(TABLE_MAC_ADDRESSES, null, values);
            Log.d(TAG, "New mac address inserted into sqlite: " + new_id);
        }

        db.close(); // Closing database connection

    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("username", cursor.getString(1));
            user.put("uid", cursor.getString(2));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    /**
     * Getting mac addresses from database
     * */
    public ArrayList<String> getMacAddresses() {
        ArrayList<String> macAddresses = new ArrayList<String>();
        String selectQuery = "SELECT " + KEY_MAC_ADDRESS + " FROM " + TABLE_MAC_ADDRESSES +
                " INNER JOIN " + TABLE_USER + " ON " +
                TABLE_MAC_ADDRESSES + "." + KEY_USER_ID + " = " +
                TABLE_USER + "." + KEY_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                macAddresses.add(cursor.getString(0));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching mac addresses from Sqlite: " + macAddresses.toString());

        return macAddresses;
    }

    /**
     * Recreate database: Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }
    public void deleteMacAddresses() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_MAC_ADDRESSES, null, null);
        db.close();

        Log.d(TAG, "Deleted all mac addresses info from sqlite");
    }
}
