package com.example.foodnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.example.foodnote.RecipeContract.RecipeEntry;
import com.example.foodnote.backend.apis.recipeApi.model.Recipe;

import java.util.List;

public class RecipeDbHelper extends SQLiteOpenHelper{
    private static final String TAG = "RecipeDbHelper";

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

    private static final String SQL_CREATE_INGREDIENTS_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + RecipeContract.IngredientsEntry.TABLE_NAME + " (" +
                    RecipeContract.IngredientsEntry._ID + INTEGER_TYPE + "PRIMARY KEY" + COMMA_SEP +
                    RecipeContract.IngredientsEntry.COLUMN_NAME_INGREDIENT + TEXT_TYPE + COMMA_SEP +
                    "UNIQUE(" + RecipeContract.IngredientsEntry.COLUMN_NAME_INGREDIENT + ")" +
                    " )";

    private static final String SQL_DELETE_RECIPE_ENTRIES =
            "DROP TABLE IF EXISTS " + RecipeEntry.TABLE_NAME;

    private static final String SQL_DELETE_STEP_ENTRIES =
            "DROP TABLE IF EXISTS " + RecipeContract.StepEntry.TABLE_NAME;

    private static final String SQL_DELETE_INGREDIENTS_ENTRIES =
            "DROP TABLE IF EXISTS " + RecipeContract.IngredientsEntry.TABLE_NAME;
    
    public RecipeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_RECIPE_ENTRIES);
        db.execSQL(SQL_CREATE_STEP_ENTRIES);
        db.execSQL(SQL_CREATE_INGREDIENTS_ENTRIES);
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Modify this later
        db.execSQL(SQL_DELETE_RECIPE_ENTRIES);
        db.execSQL(SQL_DELETE_STEP_ENTRIES);
        db.execSQL(SQL_DELETE_INGREDIENTS_ENTRIES);
        onCreate(db);
    }

    public void insertRecipeDataToDb(Recipe recipe) {
        // insert recipe data
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_TITLE, recipe.getTitle());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_DESCRIPTION, recipe.getDescription());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_INGREDIENTS, recipe.getIngredients());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_IMAGE, recipe.getImageData());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME, recipe.getDate().getValue());

        Long recipeId = recipe.getId();
        if (ActionStateSingleton.getInstance().getEditorAction() == ActionStateSingleton.EditorAction.Create) {
            recipeId = db.insert(RecipeContract.RecipeEntry.TABLE_NAME, null, values);
        } else if (ActionStateSingleton.getInstance().getEditorAction() == ActionStateSingleton.EditorAction.Edit) {
            db.update(RecipeContract.RecipeEntry.TABLE_NAME,
                    values,
                    RecipeContract.RecipeEntry._ID + " = " + recipeId,
                    null);

            // delete the existing steps for this recipe
            db.delete(RecipeContract.StepEntry.TABLE_NAME,
                    RecipeContract.StepEntry._ID + " = " + recipeId,
                    null);
        } else {

        }

        // SQL statement for inserting ingredients data
        String[] ingredients = recipe.getIngredients().split("[ ]*,[ ]*");
        String ingredientInsertSqlQuery =
                "INSERT OR IGNORE INTO " + RecipeContract.IngredientsEntry.TABLE_NAME + " (" +
                        RecipeContract.IngredientsEntry.COLUMN_NAME_INGREDIENT +
                        ") VALUES (?);";
        SQLiteStatement ingredientInsertStatement = db.compileStatement(ingredientInsertSqlQuery);

        // SQL statement for inserting steps data
        List<String> steps = recipe.getSteps();
        String stepInsertSqlQuery = "INSERT INTO " + RecipeContract.StepEntry.TABLE_NAME + " (" +
                RecipeContract.StepEntry.COLUMN_NAME_RECIPE_ID + ", " +
                RecipeContract.StepEntry.COLUMN_NAME_STEP_NUM + ", " +
                RecipeContract.StepEntry.COLUMN_NAME_STEP_DESCRIPTION +
                ") VALUES (?,?,?);";
        SQLiteStatement stepInsertStatement = db.compileStatement(stepInsertSqlQuery);

        // start db transaction for both ingredients and steps
        try {
            db.beginTransaction();
            int index;
            for (index = 0; index<ingredients.length; index++) {
                ingredientInsertStatement.clearBindings();
                ingredientInsertStatement.bindString(1, ingredients[index]);
                ingredientInsertStatement.execute();
            }

            for (index = 0; index < steps.size(); index++) {
                stepInsertStatement.clearBindings();
                stepInsertStatement.bindLong(1, recipeId);
                stepInsertStatement.bindLong(2, index);
                stepInsertStatement.bindString(3, steps.get(index));
                stepInsertStatement.execute();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
