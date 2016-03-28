package com.example.foodnote;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Base64;
import android.util.Log;

import com.example.foodnote.backend.apis.recipeApi.model.Recipe;
import com.google.api.client.util.DateTime;

import java.util.ArrayList;
import java.util.List;

public class RecipeLoader extends AsyncTaskLoader<List<Recipe>> {
    private final String TAG = "RecipeLoader";

    public RecipeLoader(Context context) {
        super(context);
    }

    @Override
    public void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public List<Recipe> loadInBackground() {
        RecipeDbHelper dbHelper = new RecipeDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Specifies which columns to use from the database
        String[] projection = {
                RecipeContract.RecipeEntry._ID,
                RecipeContract.RecipeEntry.COLUMN_NAME_TITLE,
                RecipeContract.RecipeEntry.COLUMN_NAME_DESCRIPTION,
                RecipeContract.RecipeEntry.COLUMN_NAME_INGREDIENTS,
                RecipeContract.RecipeEntry.COLUMN_NAME_IMAGE,
                RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME,
        };

        // Sort recipes by decreasing order of date
        String sortOrder = RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME + " DESC";

        List<Recipe> recipes = new ArrayList<>();

        Cursor c = null;
        try {
            c = db.query(
                    RecipeContract.RecipeEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    sortOrder
            );

            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                Recipe recipe = new Recipe();
                recipe.setId(c.getLong(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry._ID)));
                recipe.setTitle(c.getString(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_TITLE)));
                recipe.setDescription(c.getString(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_DESCRIPTION)));
                recipe.setIngredients(c.getString(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_INGREDIENTS)));
                byte[] imageBlob = c.getBlob(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_IMAGE));
                if (imageBlob != null) {
                    recipe.setImageData(Base64.encodeToString(imageBlob, Base64.DEFAULT));
                }
                recipe.setDate(new DateTime(c.getLong(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME))));

                recipes.add(recipe);
            }

            // TODO: Do this
            /*
            String[] ingrProjection = { RecipeContract.IngredientsEntry.COLUMN_NAME_INGREDIENT };
            c = db.query(RecipeContract.IngredientsEntry.TABLE_NAME, ingrProjection,
                    null, null, null, null, null);

            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                mIngredientsAdapter.add(c.getString(c.getColumnIndexOrThrow((
                        RecipeContract.IngredientsEntry.COLUMN_NAME_INGREDIENT))));
            }*/
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return recipes;
    }
}
