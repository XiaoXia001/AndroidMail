package com.zjw.androidmail.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.zjw.androidmail.utils.DButil;

/**
 * Created by zhaojinwei on 2017/4/5.
 */

public class MailConstantProvider extends ContentProvider {

    private DButil util;

    @Override
    public boolean onCreate(){
        util = new DButil(getContext());
        return false;
    }

    @Override
    public String getType(Uri uri){
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        SQLiteDatabase db = util.getReadableDatabase();
        Cursor cursor = db.query("mail", null, selection, selectionArgs, null, null, null);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values){
        SQLiteDatabase db = util.getWritableDatabase();
        long id = db.insert("mail", null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        SQLiteDatabase db =  util.getWritableDatabase();
        db.update("mail", values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        SQLiteDatabase db = util.getWritableDatabase();
        db.delete("mail", selection, selectionArgs);
        return 0;
    }
}
