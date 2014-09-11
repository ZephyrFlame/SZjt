package com.ilovelixin.szjt;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "collection.db";
    private static final int DATABASE_VERSION = 1;  

    public DatabaseHelper(Context context) 
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) 
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS history" + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, type INTEGER, datetime LONG)"); 
        db.execSQL("CREATE TABLE IF NOT EXISTS faverate" + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, type INTEGER, keyword VARCHAR, info VARCHAR)"); 
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
    {
        // TODO Auto-generated method stub
    }
}
