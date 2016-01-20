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
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

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

    RelativeLayout addRecipeRightRL;
    RelativeLayout viewRecipeRightRL;
    DrawerLayout drawerLayout;

    EditText mRecipeAddTitleText;
    EditText mRecipeAddDescription;
    EditText mRecipeAddIngredients;

    String unsavedTitle;
    String unsavedDescription;
    String unsavedIngredients;

    RecipeDbHelper mDbHelper;

    RecipeListAdapter mAdapter;
    AddStepListAdapter mAddStepAdapter;
    ViewStepListAdapter mViewStepAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // transparent action bar
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen);

        addRecipeRightRL = (RelativeLayout)findViewById(R.id.drawer_right_add_recipe);
        viewRecipeRightRL = (RelativeLayout)findViewById(R.id.drawer_right_view_recipe);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Color.parseColor("#DE000000"));

        SwipeDetector activitySwipeDetector = new SwipeDetector(this);

        RelativeLayout addRecipeLayout = (RelativeLayout)findViewById(R.id.recipeAddLayout);
        addRecipeLayout.setOnTouchListener(activitySwipeDetector);
        RelativeLayout viewRecipeLayout = (RelativeLayout)findViewById(R.id.recipeViewLayout);
        viewRecipeLayout.setOnTouchListener(activitySwipeDetector);

        mRecipeAddTitleText = (EditText)findViewById(R.id.recipeAddTitle);
        mRecipeAddDescription = (EditText)findViewById(R.id.recipeAddDescription);
        mRecipeAddIngredients = (EditText)findViewById(R.id.recipeAddIngredients);

        /** Preferences call from last time before app close    By. CHAN */
        SharedPreferences settings = getSharedPreferences(PREFS, 0);
        unsavedTitle = settings.getString("unsavedTitle", "");
        unsavedDescription = settings.getString("unsavedDescription", "");
        unsavedIngredients = settings.getString("unsavedIngredients", "");
        mRecipeAddTitleText.setText(unsavedTitle);
        mRecipeAddDescription.setText(unsavedDescription);
        mRecipeAddIngredients.setText(unsavedIngredients);
        /***/

        mDbHelper = new RecipeDbHelper(getApplicationContext());

        ListView recipeListView = (ListView)findViewById(R.id.recipeListView);
        mAdapter = new RecipeListAdapter(getApplicationContext());
        recipeListView.setAdapter(mAdapter);

        ListView addStepListView = (ListView)findViewById(R.id.recipeAddStepsList);
        mAddStepAdapter = new AddStepListAdapter(this, addStepListView);
        mAddStepAdapter.add(new AddStepItem(""));
        addStepListView.setAdapter(mAddStepAdapter);

        ListView viewStepListView = (ListView)findViewById(R.id.recipeViewStepsList);
        mViewStepAdapter = new ViewStepListAdapter(this, viewStepListView);
        viewStepListView.setAdapter(mViewStepAdapter);

        recipeListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecipeItem recipeItem = (RecipeItem) parent.getItemAtPosition(position);

                TextView recipeViewTitleText = (TextView) findViewById(R.id.recipeViewTitle);
                recipeViewTitleText.setText(recipeItem.getTitle());

                TextView recipeViewDescriptionText = (TextView) findViewById(R.id.recipeViewDescription);
                recipeViewDescriptionText.setText(recipeItem.getDescription());

                TextView recipeViewIngredientsText = (TextView) findViewById(R.id.recipeViewIngredients);
                recipeViewIngredientsText.setText("Ingredients: " + recipeItem.getIngredients());

                // retrieve step data
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                String[] projection = {
                        RecipeContract.StepEntry.COLUMN_NAME_RECIPE_ID,
                        RecipeContract.StepEntry.COLUMN_NAME_STEP_NUM,
                        RecipeContract.StepEntry.COLUMN_NAME_STEP_DESCRIPTION,
                };

                // Sort by decreasing order of date
                String sortOrder = RecipeContract.StepEntry.COLUMN_NAME_STEP_NUM + " ASC";

                Cursor c = null;
                try {
                    c = db.query(
                            RecipeContract.StepEntry.TABLE_NAME,
                            projection,
                            RecipeContract.StepEntry.COLUMN_NAME_RECIPE_ID + " = "
                                    + String.valueOf(recipeItem.getId()),
                            null,
                            null,
                            null,
                            sortOrder
                    );

                    mViewStepAdapter.clear();
                    for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                        mViewStepAdapter.add(new AddStepItem(c.getString(c.getColumnIndexOrThrow(
                                RecipeContract.StepEntry.COLUMN_NAME_STEP_DESCRIPTION))
                        ));
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }

                drawerLayout.openDrawer(viewRecipeRightRL);
            }
        });
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
                drawerLayout.openDrawer(addRecipeRightRL);
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
        drawerLayout.closeDrawer(addRecipeRightRL);
    }

    public void onCancelButtonClicked(View view) {
        clearContents();
        mAddStepAdapter.clear();
        drawerLayout.closeDrawer(addRecipeRightRL);
    }

    public void onSubmitButtonClicked(View view) {
        if (mRecipeAddTitleText.getText().length() == 0) {
            Toast.makeText(getApplicationContext(),
                    "Recipe title missing",
                    Toast.LENGTH_LONG).show();
            return;
        }

        RecipeItem recipeItem = new RecipeItem(
                mAdapter.getCount(),
                mRecipeAddTitleText.getText().toString(),
                mRecipeAddDescription.getText().toString(),
                mRecipeAddIngredients.getText().toString(),
                new Date());
        try {
            mAddStepAdapter.removeLast();  // don't insert the empty entry
            insertRecipeDataToDb(recipeItem);
            mAdapter.addToFront(recipeItem);
            Toast.makeText(getApplicationContext(),
                    "Successfully saved the recipe for \"" + mRecipeAddTitleText.getText().toString() + "\"",
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
        drawerLayout.closeDrawer(addRecipeRightRL);
    }

    public void onRecipeViewCloseButtonClicked(View view) {
        drawerLayout.closeDrawer(viewRecipeRightRL);
    }

    public void insertRecipeDataToDb(RecipeItem recipeItem) {
        // insert recipe data
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_ENTRY_ID, mAdapter.getCount());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_TITLE, recipeItem.getTitle());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_DESCRIPTION, recipeItem.getDescription());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_INGREDIENTS, recipeItem.getIngredients());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME, recipeItem.getDate().getTime());

        db.insert(RecipeContract.RecipeEntry.TABLE_NAME, null, values);

        // insert step data
        try {
            String sqlQuery = "INSERT INTO " + RecipeContract.StepEntry.TABLE_NAME + " (" +
                    RecipeContract.StepEntry.COLUMN_NAME_RECIPE_ID + ", " +
                    RecipeContract.StepEntry.COLUMN_NAME_STEP_NUM + ", " +
                    RecipeContract.StepEntry.COLUMN_NAME_STEP_DESCRIPTION +
                    ") VALUES (?,?,?);";
            SQLiteStatement statement = db.compileStatement(sqlQuery);
            db.beginTransaction();
            for (int i = 0; i < mAddStepAdapter.getCount(); i++) {
                statement.clearBindings();
                statement.bindLong(1, mAdapter.getCount()); // recipe id
                statement.bindLong(2, i);
                statement.bindString(3, mAddStepAdapter.getItem(i).getStep());
                statement.execute();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void clearContents() {
        mRecipeAddTitleText.setText("");
        mRecipeAddDescription.setText("");
        mRecipeAddIngredients.setText("");
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(addRecipeRightRL)) {
            drawerLayout.closeDrawer(addRecipeRightRL);
        } else if (drawerLayout.isDrawerOpen(viewRecipeRightRL)) {
            drawerLayout.closeDrawer(viewRecipeRightRL);
        } else {
            // default behaviour
            finish();
        }
    }

    public void onSwipeRight() {
        if (drawerLayout.isDrawerOpen(addRecipeRightRL)) {
            drawerLayout.closeDrawer(addRecipeRightRL);
        } else if (drawerLayout.isDrawerOpen(viewRecipeRightRL)){
            drawerLayout.closeDrawer(viewRecipeRightRL);
        }
    }

    // Load stored RecipeItems
    private void loadItems() {
        // HACK! Uncomment the below lines when schema is changed (only once)
        // TODO: To be removed when the schema is finalized
        //getApplicationContext().deleteDatabase(RecipeDbHelper.DATABASE_NAME);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Specifies which columns to use from the database
        String[] projection = {
                RecipeContract.RecipeEntry.COLUMN_NAME_ENTRY_ID,
                RecipeContract.RecipeEntry.COLUMN_NAME_TITLE,
                RecipeContract.RecipeEntry.COLUMN_NAME_DESCRIPTION,
                RecipeContract.RecipeEntry.COLUMN_NAME_INGREDIENTS,
                RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME,
        };

        // Sort by decreasing order of date
        String sortOrder = RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME + " DESC";

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
                mAdapter.add(new RecipeItem(
                        c.getLong(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_ENTRY_ID)),
                        c.getString(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_TITLE)),
                        c.getString(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_DESCRIPTION)),
                        c.getString(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_INGREDIENTS)),
                        new Date(c.getLong(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME)))
                ));
            }
        } finally {
            if (c != null) {
                c.close();
            }
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
        unsavedTitle = mRecipeAddTitleText.getText().toString();
        unsavedDescription = mRecipeAddDescription.getText().toString();
        unsavedIngredients = mRecipeAddIngredients.getText().toString();
        editor.putString("unsavedTitle", unsavedTitle);
        editor.putString("unsavedDescription", unsavedDescription);
        editor.putString("unsavedIngredients", unsavedIngredients);
        editor.commit();
    }
    /***/

    public class SwipeDetector implements View.OnTouchListener {
        private Activity activity;
        static final int MIN_DISTANCE = 60;
        private float downX, upX;

        public SwipeDetector(Activity activity) {
            this.activity = activity;
        }

        public void onSwipeRight() {
            ((FullscreenActivity) activity).onSwipeRight();
        }

        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    upX = event.getX();

                    float deltaX = downX - upX;

                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        // left or right
                        if (deltaX < 0) {
                            this.onSwipeRight();
                        }
                    } else {
                        onAddRecipeBackgroundClicked(v);
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
