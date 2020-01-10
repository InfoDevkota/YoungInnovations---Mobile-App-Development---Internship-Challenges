package com.devkotasagar.listMe.test.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.devkotasagar.listMe.test.data.ListContract.UserEntry;

public class ListMeDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ListMe.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_USER_TABLE = "" +
            "CREATE TABLE " + UserEntry.TABLE_NAME + "( " +
            UserEntry.COLUMN_USER_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            UserEntry.COLUMN_USER_IDD+ " INTEGER, " +
            UserEntry.COLUMN_EMAIL+ " TEXT, " +
            UserEntry.COLUMN_NAME+ " TEXT, " +
            UserEntry.COLUMN_phone+ " TEXT, " +
            UserEntry.COLUMN_ADDRESS_STREET+ " TEXT, " +
            UserEntry.COLUMN_ADDRESS_ZIP+ " TEXT);";

    public ListMeDBHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_USER_TABLE);

        //TestForDisplayingUsers
        sqLiteDatabase.execSQL("INSERT INTO "+ UserEntry.TABLE_NAME + "(" +
                    UserEntry.COLUMN_USER_IDD + "," +
                    UserEntry.COLUMN_NAME+ "," +
                    UserEntry.COLUMN_EMAIL+ ", " +
                    UserEntry.COLUMN_phone + ", " +
                    UserEntry.COLUMN_ADDRESS_STREET+ ", " +
                    UserEntry.COLUMN_ADDRESS_ZIP +
                ") VALUES(" +
                + 0+", "+
                "'Test', 'test@test.test', '987654321', 'Testara', '0123T')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //On Update when database schema changes
    }
}
