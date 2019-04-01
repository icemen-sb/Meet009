package ru.relastic.meet009;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class DBManager {
    private final DbHelper dbhelper;

    public DBManager(Context context) {
        dbhelper = new DbHelper(context);
    }
    public ArrayList<Bundle> getData(){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<Bundle> data = new ArrayList<>();

        try {
            db = dbhelper.getReadableDatabase();
            cursor = db.query(DbHelper.TABLE_NAME,null,null,null,null,null,DbHelper.FIELD_ID);
            data.addAll(parceCursor(cursor));
        }catch (SQLException e) {
            Log.v("SQLiteException", e.getMessage());
        }finally {
            if (db != null && cursor !=null) {
                cursor.close();
                db.close();
            }
        }
        return data;
    }
    public Bundle getDataById(int id){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Bundle retVal = null;
        try {
            db = dbhelper.getReadableDatabase();
            cursor = db.query(DbHelper.TABLE_NAME,null,DbHelper.FIELD_ID+"=?",
                    new String[] { String.valueOf(id) },null,null,null);
            retVal = new Bundle();
            retVal.putInt(DbHelper.FIELD_POS,1);
            cursor.moveToFirst();
            retVal.putInt(DbHelper.FIELD_ID,cursor.getInt(cursor.getColumnIndex(DbHelper.FIELD_ID)));
            retVal.putString(DbHelper.FIELD_NOTE,cursor.getString(cursor.getColumnIndex(DbHelper.FIELD_NOTE)));

        }catch (SQLException e) {
            Log.v("SQLiteException", e.getMessage());
        }finally {
            if (db != null && cursor !=null) {
                cursor.close();
                db.close();
            }
        }
        return retVal;
    }
    public int updateData(Bundle value) {
        int retval = 0;
        int id = value.getInt(DbHelper.FIELD_ID);
        SQLiteDatabase db = null;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbHelper.FIELD_NOTE,value.getString(DbHelper.FIELD_NOTE));

        try {
            db = dbhelper.getWritableDatabase();
            db.beginTransaction();
            if (id >0) {
                //update
                db.update(DbHelper.TABLE_NAME, contentValues, DbHelper.FIELD_ID + "=?",
                        new String[]{String.valueOf(id)});
                retval = value.getInt(DbHelper.FIELD_ID);
            }else {
                //insert
                db.insert(DbHelper.TABLE_NAME, null, contentValues);
                Cursor cursor = db.query(DbHelper.TABLE_NAME,null,null,
                        null,null,null,DbHelper.FIELD_ID);
                cursor.moveToLast();
                id = cursor.getInt(cursor.getColumnIndex(DbHelper.FIELD_ID));
            }
            db.setTransactionSuccessful();
            retval=id;
        }catch (SQLiteException e) {
            Log.v("SQLiteException",e.getMessage());
        }finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
            db.close();
        }
        return retval;
    }

    public static ArrayList<Bundle> parceCursor(Cursor cursor) {
        ArrayList<Bundle> data = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            int i = 1;
            Bundle bundle= new Bundle();
            bundle.putInt(DbHelper.FIELD_POS,i);
            bundle.putInt(DbHelper.FIELD_ID,cursor.getInt(cursor.getColumnIndex(DbHelper.FIELD_ID)));
            bundle.putString(DbHelper.FIELD_NOTE,cursor.getString(cursor.getColumnIndex(DbHelper.FIELD_NOTE)));
            data.add(bundle);
            while (!cursor.isLast()) {
                cursor.moveToNext();
                i++;
                bundle= new Bundle();
                bundle.putInt(DbHelper.FIELD_POS,i);
                bundle.putInt(DbHelper.FIELD_ID,cursor.getInt(cursor.getColumnIndex(DbHelper.FIELD_ID)));
                bundle.putString(DbHelper.FIELD_NOTE,cursor.getString(cursor.getColumnIndex(DbHelper.FIELD_NOTE)));
                data.add(bundle);
            }
        }
        return data;
    }
    public class DbHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "database.db";
        private static final int VERSION_DB = 3;
        public static final String TABLE_NAME="notes";
        public static final String FIELD_POS="pos";
        public static final String FIELD_ID="id";
        public static final String FIELD_NOTE="note";
        public static final int LEN_BREAF_STRING = 32;

        public DbHelper(Context context) {
            this(context, DB_NAME, null, VERSION_DB);
        }

        public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTables(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            deleteTables(db);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        private void createTables(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "+DbHelper.TABLE_NAME+" (" + DbHelper.FIELD_ID + " integer primary key autoincrement, " + DbHelper.FIELD_NOTE + " text)");
        }

        private void deleteTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS "+DbHelper.TABLE_NAME);
        }
    }
}
