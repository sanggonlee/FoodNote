package com.example.foodnote;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.widget.Toast;

import com.example.foodnote.backend.apis.recipeApi.RecipeApi;
import com.example.foodnote.backend.apis.recipeApi.model.Recipe;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecipeLoader extends AsyncTaskLoader<List<Recipe>> {
    private final String TAG = "RecipeLoader";

    private Context mContext;
    private ProgressDialog mProgressDialog;

    public RecipeLoader(Context context, ProgressDialog progressDialog) {
        super(context);
        this.mContext = context;
        this.mProgressDialog = progressDialog;
    }

    @Override
    public void onStartLoading() {
        super.onStartLoading();
        Log.i(TAG, "load started");
        mProgressDialog.setMessage("Loading recipes...");
        mProgressDialog.show();
        forceLoad();
    }

    @Override
    public List<Recipe> loadInBackground() {
        if (ActionStateSingleton.getInstance().getBrowser() == R.id.recipe_book) {
            return loadLocalItems();
        } else if (ActionStateSingleton.getInstance().getBrowser() == R.id.recipe_world) {
            return loadRemoteItems();
        } else {
            Log.e(TAG, "Trying to load with unspecified browser. Giving back null result.");
            return null;
        }
    }

    private List<Recipe> loadLocalItems() {
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
                recipe.setImageData(c.getString(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_IMAGE)));
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

    private List<Recipe> loadRemoteItems() {
        RecipeApi recipeApi = CloudEndpointBuilderHelper.getRecipeEndpoints();
        try {
            return recipeApi.list().execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
