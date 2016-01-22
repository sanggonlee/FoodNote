package com.example.foodnote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.foodnote.RecipeContract.RecipeEntry;

public class RecipeDbHelper extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Recipe.db";
    
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_RECIPE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + RecipeEntry.TABLE_NAME + " (" +
                    RecipeEntry._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
                    RecipeEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    RecipeEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    RecipeEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    RecipeEntry.COLUMN_NAME_INGREDIENTS + TEXT_TYPE + COMMA_SEP +
                    RecipeEntry.COLUMN_NAME_IMAGE + TEXT_TYPE + COMMA_SEP +
                    RecipeEntry.COLUMN_NAME_UPDATE_TIME + INTEGER_TYPE +
                    " )";

    private static final String SQL_CREATE_STEP_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + RecipeContract.StepEntry.TABLE_NAME + " (" +
                    RecipeContract.StepEntry._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
                    RecipeContract.StepEntry.COLUMN_NAME_RECIPE_ID + INTEGER_TYPE + COMMA_SEP +
                    RecipeContract.StepEntry.COLUMN_NAME_STEP_NUM + INTEGER_TYPE + COMMA_SEP +
                    RecipeContract.StepEntry.COLUMN_NAME_STEP_DESCRIPTION + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_RECIPE_ENTRIES =
            "DROP TABLE IF EXISTS " + RecipeEntry.TABLE_NAME;

    private static final String SQL_DELETE_STEP_ENTRIES =
            "DROP TABLE IF EXISTS " + RecipeContract.StepEntry.TABLE_NAME;
    
    public RecipeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_RECIPE_ENTRIES);
        db.execSQL(SQL_CREATE_STEP_ENTRIES);
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Modify this later
        db.execSQL(SQL_DELETE_RECIPE_ENTRIES);
        db.execSQL(SQL_DELETE_STEP_ENTRIES);
        onCreate(db);
    }
}
