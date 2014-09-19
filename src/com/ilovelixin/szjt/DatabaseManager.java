package com.ilovelixin.szjt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager 
{
    private DatabaseHelper helper;
    private SQLiteDatabase db; 
    
    public DatabaseManager(Context context)
    {
        helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }
    
    public void closeDatabase()
    {
        db.close();
    }
    
    public void addHistory(List<HistoryData> historys) 
    {
        db.beginTransaction();
        try 
        {
            for (HistoryData history : historys) 
            {
                db.execSQL("INSERT INTO history VALUES(null, ?, ?, ?)", new Object[]{history.Name, history.Type, history.TimeStamp});
            }
            db.setTransactionSuccessful();
        } 
        finally 
        {
            db.endTransaction();
        }
    }
    
    public void addHistory(HistoryData history) 
    {
        ContentValues cv = new ContentValues();
        cv.put("datetime", history.TimeStamp); 
        cv.put("type", history.Type); 
        cv.put("name", history.Name); 
        db.insert("history", null, cv);
    }
    
    public void updateHistory(HistoryData history)
    {
        ContentValues cv = new ContentValues(); 
        cv.put("datetime", history.TimeStamp); 
        db.update("history", cv, "name=? and type=?", new String[]{history.Name, Integer.toString(history.Type)});
    }
    
    public void deleteHistory(HistoryData history)
    {
        db.delete("history", "name=? and type=?", new String[]{history.Name, Integer.toString(history.Type)});
    }
    
    public List<HistoryData> queryHistorys() 
    {
        List<HistoryData> historys = new ArrayList<HistoryData>();
        Cursor c = queryTheHistoryCursor();
        while (c.moveToNext()) 
        {
            HistoryData history = new HistoryData();
            history._id = c.getInt(c.getColumnIndex("_id"));
            history.Name = c.getString(c.getColumnIndex("name"));
            history.Type = c.getInt(c.getColumnIndex("type"));
            history.TimeStamp = c.getLong(c.getColumnIndex("datetime"));
            historys.add(history);
        }
        c.close();
        return historys;
    }
    
    public boolean queryHistory(String key, String value)
    {
        String lang = "select * from history where " + key + "=?";
        Cursor c = db.rawQuery(lang, new String[]{value});
        
        return c.moveToFirst();
    }
    
    public Cursor queryTheHistoryCursor()
    {
        Cursor c = db.rawQuery("SELECT * FROM history", null);
        return c;
    }
    
    public void addFaverate(List<FaverateData> faverates) 
    {
        db.beginTransaction(); 
        try 
        {
            for (FaverateData faverate : faverates) 
            {
                db.execSQL("INSERT INTO faverate VALUES(null, ?, ?, ?, ?)", new Object[]{faverate.Name, faverate.Type, faverate.Keyword, faverate.Info});
            }
            db.setTransactionSuccessful();
        } 
        finally 
        {
            db.endTransaction();
        }
    }
    
    public void addFaverate(FaverateData faverate) 
    {
        ContentValues cv = new ContentValues();
        cv.put("keyword", faverate.Keyword); 
        cv.put("type", faverate.Type); 
        cv.put("name", faverate.Name);
        cv.put("info", faverate.Info); 
        db.insert("faverate", null, cv);
    }
    
    public void deleteFaverate(FaverateData faverate)
    {
        db.delete("faverate", "keyword=?", new String[]{faverate.Keyword});  
    }
    
    public List<FaverateData> queryFaverates() 
    {
        List<FaverateData> faverates = new ArrayList<FaverateData>();
        Cursor c = queryTheFaverateCursor();
        while (c.moveToNext()) 
        {
            FaverateData faverate = new FaverateData();
            faverate._id = c.getInt(c.getColumnIndex("_id"));
            faverate.Name = c.getString(c.getColumnIndex("name"));
            faverate.Type = c.getInt(c.getColumnIndex("type"));
            faverate.Keyword = c.getString(c.getColumnIndex("keyword"));
            faverate.Info = c.getString(c.getColumnIndex("info"));
            faverates.add(faverate);
        }
        c.close();
        return faverates;
    }
    
    public boolean queryFaverate(String key, String value)
    {
        String lang = "select * from faverate where " + key + "=?";
        Cursor c = db.rawQuery(lang, new String[]{value});
        
        return c.moveToFirst();
    }
    
    public Cursor queryTheFaverateCursor()
    {
        Cursor c = db.rawQuery("SELECT * FROM faverate", null);
        return c;
    }
    
    public void closeDB() 
    {
        db.close();
    }
}
