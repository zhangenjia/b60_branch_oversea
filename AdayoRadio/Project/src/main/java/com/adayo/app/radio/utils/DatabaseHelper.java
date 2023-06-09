package com.adayo.app.radio.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author ADAYO-06
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * 带全部参数的构造函数，此构造函数必不可少
     * @param context
     * @param name
     * @param factory
     * @param version
     */
    public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建数据库sql语句 并 执行
        String sql = "create table user(name varchar(20))";
        String sql2 = "create table userTwo(nameTwo varchar(20))";
        db.execSQL(sql);
        db.execSQL(sql2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}