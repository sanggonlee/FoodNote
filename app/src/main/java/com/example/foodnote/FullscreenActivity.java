package com.example.foodnote;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Date;

import com.example.foodnote.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
    public static final String PREFS = "Writing";

    String TAG = "FullscreenActivity";

    RelativeLayout rightRL;
    DrawerLayout drawerLayout;

    EditText mTitleText;
    EditText mDescription;
    EditText mIngredients;

    String unsavedTitle;
    String unsavedDescription;
    String unsavedIngredients;

    RecipeDbHelper mDbHelper;

    RecipeListAdapter mAdapter;
    AddStepListAdapter mAddStepAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // transparent action bar
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen);

        rightRL = (RelativeLayout)findViewById(R.id.drawer_right);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Color.parseColor("#DE000000"));

        mTitleText = (EditText)findViewById(R.id.title);
        mDescription = (EditText)findViewById(R.id.description);
        mIngredients = (EditText)findViewById(R.id.ingredients);

        /** Preferences call from last time before app close    By. CHAN */
        SharedPreferences settings = getSharedPreferences(PREFS, 0);
        unsavedTitle = settings.getString("unsavedTitle", "");
        unsavedDescription = settings.getString("unsavedDescription", "");
        unsavedIngredients = settings.getString("unsavedIngredients", "");
        mTitleText.setText(unsavedTitle);
        mDescription.setText(unsavedDescription);
        mIngredients.setText(unsavedIngredients);
        /***/

        mDbHelper = new RecipeDbHelper(getApplicationContext());

        ListView recipeListView = (ListView)findViewById(R.id.recipeListView);
        mAdapter = new RecipeListAdapter(getApplicationContext());
        recipeListView.setAdapter(mAdapter);

        ListView addStepListView = (ListView)findViewById(R.id.addStepsList);
        mAddStepAdapter = new AddStepListAdapter(this, addStepListView);
        mAddStepAdapter.add(new AddStepItem(""));
        addStepListView.setAdapter(mAddStepAdapter);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.food_note_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_compose:
                drawerLayout.openDrawer(rightRL);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onAddRecipeBackgroundClicked(View view) {
        // Dismiss virtual keyboard. Ugly, but seems to be the simplest way for now..
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    public void onHideButtonClicked(View view) {
        drawerLayout.closeDrawer(rightRL);
    }

    public void onCancelButtonClicked(View view) {
        clearContents();
        mAddStepAdapter.clear();
        drawerLayout.closeDrawer(rightRL);
    }

    public void onSubmitButtonClicked(View view) {
        RecipeItem recipeItem = new RecipeItem(
                mTitleText.getText().toString(),
                mDescription.getText().toString(),
                mIngredients.getText().toString(),
                new Date());
        try {
            insertRecipeDataToDb(recipeItem);
            mAdapter.add(recipeItem);
            Toast.makeText(getApplicationContext(),
                    "Successfully saved the recipe for \"" + mTitleText.getText().toString() + "\"",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(getApplicationContext(),
                    "Recipe save failed. Please try again.",
                    Toast.LENGTH_LONG).show();
            return; // don't clear the contents if unsuccessful
        }

        clearContents();
        mAddStepAdapter.clear();
        drawerLayout.closeDrawer(rightRL);
    }

    public void insertRecipeDataToDb(RecipeItem recipeItem) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_ENTRY_ID, mAdapter.getCount());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_TITLE, recipeItem.getTitle());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_DESCRIPTION, recipeItem.getDescription());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_INGREDIENTS, recipeItem.getIngredients());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME, recipeItem.getDate().getTime());

        Log.i(TAG, "Inserting a Recipe entry data " + values.toString() + " to DB");
        db.insert(RecipeContract.RecipeEntry.TABLE_NAME, null, values);
    }

    private void clearContents() {
        mTitleText.setText("");
        mDescription.setText("");
        mIngredients.setText("");
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(rightRL)) {
            drawerLayout.closeDrawer(rightRL);
        } else {
            // default behaviour
            finish();
        }
    }

    // Load stored RecipeItems
    private void loadItems() {
        // HACK! Uncomment the below line when schema is changed (only once)
        // TODO: To be removed when the schema is finalized
        //getApplicationContext().deleteDatabase(RecipeDbHelper.DATABASE_NAME);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Specifies which columns to use from the database
        String[] projection = {
                RecipeContract.RecipeEntry._ID,
                RecipeContract.RecipeEntry.COLUMN_NAME_TITLE,
                RecipeContract.RecipeEntry.COLUMN_NAME_DESCRIPTION,
                RecipeContract.RecipeEntry.COLUMN_NAME_INGREDIENTS,
                RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME,
        };

        // Sort by decreasing order of date
        String sortOrder = RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME + " DESC";

        Cursor c = db.query(
                RecipeContract.RecipeEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            mAdapter.add(new RecipeItem(
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_TITLE)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_DESCRIPTION)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_INGREDIENTS)),
                    new Date(c.getLong(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME)))
            ));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Load saved RecipeItems, if necessary
        if (mAdapter.getCount() == 0) {
            loadItems();
        }
    }

    /** Preferences before close app    By. CHAN */
    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences settings = getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        unsavedTitle = mTitleText.getText().toString();
        unsavedDescription = mDescription.getText().toString();
        unsavedIngredients = mIngredients.getText().toString();
        editor.putString("unsavedTitle", unsavedTitle);
        editor.putString("unsavedDescription", unsavedDescription);
        editor.putString("unsavedIngredients", unsavedIngredients);
        editor.commit();
    }
    /***/
}
