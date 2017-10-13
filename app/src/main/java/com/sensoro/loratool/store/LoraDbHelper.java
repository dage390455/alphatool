package com.sensoro.loratool.store;

import android.content.Context;
import android.database.DefaultDatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sensoro on 16/9/5.
 */

public class LoraDbHelper extends SQLiteOpenHelper {


    public static final String TABLE_DEVICE = "device_info";
    public static final String TABLE_DEVICE_OUT = "device_out";
    public static final String TABLE_UPGRADE_INFO = "upgrade_info";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DEVICE_ID = "device_id";
    public static final String COLUMN_DEVICE_SN = "device_sn";
    public static final String COLUMN_DEVICE_VERSION = "device_version";
    public static final String COLUMN_DEVICE_CONTENT = "device_content";
    public static final String COLUMN_DEVICE_FIRMWARE_VERSION = "firmware_version";
    public static final String COLUMN_DEVICE_UPGRADE_DATA = "upgrade_sn";
    public static final String DB_NAME = "db_lora_device";

    public static final int DB_VERSION = 4;

    public static LoraDbHelper instance;

    private LoraDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION, new DefaultDatabaseErrorHandler());
    }


    public static LoraDbHelper init(Context context) {
        if (instance == null) {
            synchronized (LoraDbHelper.class) {
                instance = new LoraDbHelper(context);
            }
        }
        return instance;
    }

    public void clearTable(String table) {
        String sql = "DELETE FROM " + table + ";";
        try {
            this.getWritableDatabase().execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.getWritableDatabase().close();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_DEVICE +
                "(" + COLUMN_DEVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DEVICE_SN + " TEXT," +
                COLUMN_DEVICE_VERSION + " TEXT, " +
                COLUMN_DEVICE_CONTENT + " TEXT" +
                ")";
        db.execSQL(sql);

        String sql1 = "CREATE TABLE IF NOT EXISTS " + TABLE_DEVICE_OUT +
                "(" + COLUMN_DEVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DEVICE_SN + " TEXT" +
                ")";
        db.execSQL(sql1);
        String sql2 = "CREATE TABLE IF NOT EXISTS " + TABLE_UPGRADE_INFO +
                "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DEVICE_FIRMWARE_VERSION + " TEXT," +
                COLUMN_DEVICE_UPGRADE_DATA + " TEXT" +
                ")";
        db.execSQL(sql2);
        db.setVersion(DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql1 = "CREATE TABLE IF NOT EXISTS " + TABLE_DEVICE_OUT +
                "(" + COLUMN_DEVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DEVICE_SN + " TEXT" +
                ")";
        db.execSQL(sql1);
        String sql2 = "CREATE TABLE IF NOT EXISTS " + TABLE_UPGRADE_INFO +
                "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DEVICE_FIRMWARE_VERSION + " TEXT," +
                COLUMN_DEVICE_UPGRADE_DATA + " TEXT" +
                ")";
        db.execSQL(sql2);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICE_OUT);
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_UPGRADE_INFO);
    }
}
