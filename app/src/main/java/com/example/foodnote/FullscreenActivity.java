package com.example.foodnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.foodnote.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
    public static final String PREFS = "Writing";
    public static final int IMAGE_CHOOSE_REQ_CODE = 0;

    String TAG = "FullscreenActivity";

    public enum RecipeEditorAction {
        None, Create, Edit
    }
    RecipeEditorAction mRecipeEditorAction;
    long mViewId;

    RelativeLayout addRecipeRightRL;
    RelativeLayout viewRecipeRightRL;
    DrawerLayout drawerLayout;

    EditText mRecipeAddTitleText;
    EditText mRecipeAddDescription;
    MultiAutoCompleteTextView mRecipeAddIngredients;

    ImageButton mPictureButton;
    @Nullable
    Bitmap mPictureBitmap;

    String unsavedTitle;
    String unsavedDescription;
    String unsavedIngredients;

    RecipeDbHelper mDbHelper;

    RecipeListAdapter mAdapter;
    AddStepListAdapter mAddStepAdapter;
    ViewStepListAdapter mViewStepAdapter;
    ArrayAdapter<String> mIngredientsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // transparent action bar
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen);

        mRecipeEditorAction = RecipeEditorAction.None;

        addRecipeRightRL = (RelativeLayout)findViewById(R.id.drawer_right_add_recipe);
        viewRecipeRightRL = (RelativeLayout)findViewById(R.id.drawer_right_view_recipe);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Color.parseColor("#DE000000"));
        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (mRecipeEditorAction == RecipeEditorAction.Edit
                        && drawerView.getId() == R.id.drawer_right_view_recipe) {
                    drawerLayout.openDrawer(addRecipeRightRL);
                }
                mViewStepAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        SwipeDetector activitySwipeDetector = new SwipeDetector(this);

        RelativeLayout addRecipeLayout = (RelativeLayout)findViewById(R.id.recipeAddLayout);
        addRecipeLayout.setOnTouchListener(activitySwipeDetector);
        RelativeLayout viewRecipeLayout = (RelativeLayout)findViewById(R.id.recipeViewLayout);
        viewRecipeLayout.setOnTouchListener(activitySwipeDetector);

        mRecipeAddTitleText = (EditText)findViewById(R.id.recipeAddTitle);
        mRecipeAddDescription = (EditText)findViewById(R.id.recipeAddDescription);
        mRecipeAddIngredients = (MultiAutoCompleteTextView)findViewById(R.id.recipeAddIngredients);
        mRecipeAddIngredients.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mAddStepAdapter.setIngredients(
                            mRecipeAddIngredients.getText().toString().split("[ ]*,[ ]*"));
                }
            }
        });

        mPictureButton = (ImageButton)findViewById(R.id.recipeAddPicture);
        mPictureBitmap = null;

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
        addStepListView.setAdapter(mAddStepAdapter);

        ListView viewStepListView = (ListView)findViewById(R.id.recipeViewStepsList);
        mViewStepAdapter = new ViewStepListAdapter(this, viewStepListView);
        viewStepListView.setAdapter(mViewStepAdapter);

        recipeListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecipeItem recipeItem = (RecipeItem) parent.getItemAtPosition(position);

                mViewId = recipeItem.getId();

                TextView recipeViewTitleText = (TextView) findViewById(R.id.recipeViewTitle);
                recipeViewTitleText.setText(recipeItem.getTitle());

                TextView recipeViewDescriptionText = (TextView) findViewById(R.id.recipeViewDescription);
                recipeViewDescriptionText.setText(recipeItem.getDescription());

                TextView recipeViewIngredientsText = (TextView) findViewById(R.id.recipeViewIngredients);
                recipeViewIngredientsText.setText("Ingredients: " + recipeItem.getIngredients());

                ImageView recipeViewPicture = (ImageView) findViewById(R.id.recipeViewPicture);
                byte[] blob = recipeItem.getPictureBlob();
                if (blob != null) {
                    recipeViewPicture.setImageBitmap(BitmapFactory.decodeByteArray(blob, 0, blob.length));
                } else {
                    recipeViewPicture.setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.taco128));
                }

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

        // set ingredients data for ingredients section
        mIngredientsAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line);
        mRecipeAddIngredients.setAdapter(mIngredientsAdapter);
        mRecipeAddIngredients.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
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
                mRecipeEditorAction = RecipeEditorAction.Create;
                mAddStepAdapter.addAndAdjustHeight(new AddStepItem(""));
                mRecipeAddTitleText.requestFocus();
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

    public void onAddPictureButtonClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_CHOOSE_REQ_CODE);
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        // Decode image size

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 140;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CHOOSE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                try {
                    mPictureBitmap = decodeUri(imageUri);
                    mPictureButton.setImageBitmap(mPictureBitmap);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    Toast.makeText(getApplicationContext(),
                            "Couldn't upload the picture. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void onHideButtonClicked(View view) {
        drawerLayout.closeDrawer(addRecipeRightRL);
    }

    public void onCancelButtonClicked(View view) {
        clearContents();
        mAddStepAdapter.clear();
        drawerLayout.closeDrawer(addRecipeRightRL);
    }

    public void onSubmitButtonClicked(View view) throws RuntimeException {
        if (mRecipeAddTitleText.getText().length() == 0) {
            Toast.makeText(getApplicationContext(),
                    "Recipe title missing",
                    Toast.LENGTH_LONG).show();
            return;
        }

        byte[] compressedPicture = null;
        if (mPictureBitmap != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mPictureBitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            compressedPicture = outputStream.toByteArray();
        }

        long recipeId;
        if (mRecipeEditorAction == RecipeEditorAction.Create) {
            recipeId = mAdapter.getCount();
        } else if (mRecipeEditorAction == RecipeEditorAction.Edit) {
            recipeId = mViewId;
        } else {
            throw new RuntimeException("Trying to save data when action is not specified.");
        }
        RecipeItem recipeItem = new RecipeItem(
                recipeId,
                mRecipeAddTitleText.getText().toString(),
                mRecipeAddDescription.getText().toString(),
                mRecipeAddIngredients.getText().toString(),
                compressedPicture,
                new Date());

        try {
            for (int stepIndex=0; stepIndex<mAddStepAdapter.getCount(); stepIndex++) {
                if (mAddStepAdapter.getItem(stepIndex).getIsSubmitted()) {
                    recipeItem.addStep(mAddStepAdapter.getItem(stepIndex));
                } else {
                    mAddStepAdapter.remove(stepIndex);
                    stepIndex--;
                }
            }
            insertRecipeDataToDb(recipeItem);

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

        // reload items
        mAdapter.clear();
        loadItems();
    }

    public void onRecipeViewCloseButtonClicked(View view) {
        mRecipeEditorAction = RecipeEditorAction.None;
        drawerLayout.closeDrawer(viewRecipeRightRL);
    }

    public void onRecipeViewDeleteButtonClicked(View view) {
        new AlertDialog.Builder(FullscreenActivity.this)
                .setTitle("Delete recipe")
                .setMessage("Are you sure you want to delete this recipe? This action cannot be undone.")
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // delete from db
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();
                        db.delete(RecipeContract.StepEntry.TABLE_NAME,
                                RecipeContract.StepEntry.COLUMN_NAME_RECIPE_ID + " = " + mViewId,
                                null);
                        db.delete(RecipeContract.RecipeEntry.TABLE_NAME,
                                RecipeContract.RecipeEntry.COLUMN_NAME_ENTRY_ID + " = " + mViewId,
                                null);

                        // reload UIs
                        mAdapter.clear();
                        loadItems();

                        drawerLayout.closeDrawer(viewRecipeRightRL);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    public void onRecipeViewEditButtonClicked(View view) {
        drawerLayout.closeDrawer(viewRecipeRightRL);
        mRecipeEditorAction = RecipeEditorAction.Edit;

        // get data from views

        TextView recipeViewTitleText = (TextView)findViewById(R.id.recipeViewTitle);
        mRecipeAddTitleText.setText(recipeViewTitleText.getText());

        TextView recipeViewDescriptionText = (TextView)findViewById(R.id.recipeViewDescription);
        mRecipeAddDescription.setText(recipeViewDescriptionText.getText());

        TextView recipeViewIngredientsText = (TextView)findViewById(R.id.recipeViewIngredients);
        mRecipeAddIngredients.setText(
                stripFormattedIngredients(recipeViewIngredientsText.getText().toString()));

        ImageView recipeViewPicture = (ImageView)findViewById(R.id.recipeViewPicture);
        mPictureButton.setBackgroundResource(0);
        mPictureButton.setImageDrawable(recipeViewPicture.getDrawable());

        ListView viewStepsList = (ListView)findViewById(R.id.recipeViewStepsList);
        ListView addStepsList = (ListView)findViewById(R.id.recipeAddStepsList);
        for (int itemPos=0; itemPos<mViewStepAdapter.getCount(); itemPos++) {
            AddStepItem item = mViewStepAdapter.getItem(itemPos);
            item.setIsEditing(false);
            mAddStepAdapter.add(item);
        }
        mAddStepAdapter.add(new AddStepItem(""));
        AddStepListAdapter.recalculateListViewHeight(viewStepsList, addStepsList);
    }

    private String stripFormattedIngredients(String formatted) {
        // For now, just remove the prefix label "Ingredients:"
        return formatted.substring(13);
    }

    public void insertRecipeDataToDb(RecipeItem recipeItem) {
        // insert recipe data
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_ENTRY_ID, recipeItem.getId());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_TITLE, recipeItem.getTitle());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_DESCRIPTION, recipeItem.getDescription());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_INGREDIENTS, recipeItem.getIngredients());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_IMAGE, recipeItem.getPictureBlob());
        values.put(RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME, recipeItem.getDate().getTime());

        if (mRecipeEditorAction == RecipeEditorAction.Create) {
            db.insert(RecipeContract.RecipeEntry.TABLE_NAME, null, values);
        } else if (mRecipeEditorAction == RecipeEditorAction.Edit) {
            db.update(RecipeContract.RecipeEntry.TABLE_NAME,
                    values,
                    RecipeContract.RecipeEntry.COLUMN_NAME_ENTRY_ID + " = " + recipeItem.getId(),
                    null);

            // delete the existing steps for this recipe
            db.delete(RecipeContract.StepEntry.TABLE_NAME,
                    RecipeContract.StepEntry.COLUMN_NAME_RECIPE_ID + " = " + recipeItem.getId(),
                    null);
        }

        // SQL statement for inserting ingredients data
        String[] ingredients = recipeItem.getIngredients().split("[ ]*,[ ]*");
        String ingredientInsertSqlQuery =
                "INSERT OR IGNORE INTO " + RecipeContract.IngredientsEntry.TABLE_NAME + " (" +
                        RecipeContract.IngredientsEntry.COLUMN_NAME_INGREDIENT +
                        ") VALUES (?);";
        SQLiteStatement ingredientInsertStatement = db.compileStatement(ingredientInsertSqlQuery);

        // SQL statement for inserting steps data
        List<AddStepItem> steps = recipeItem.getSteps();
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
                stepInsertStatement.bindLong(1, recipeItem.getId()); // recipe id
                stepInsertStatement.bindLong(2, index);
                stepInsertStatement.bindString(3, steps.get(index).getStep());
                stepInsertStatement.execute();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void clearContents() {
        mRecipeEditorAction = RecipeEditorAction.None;
        mRecipeAddTitleText.setText("");
        mRecipeAddDescription.setText("");
        mRecipeAddIngredients.setText("");
        mPictureBitmap = null;
        mPictureButton.setImageDrawable(
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.img_upload_icon));
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
                RecipeContract.RecipeEntry.COLUMN_NAME_IMAGE,
                RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME,
        };
        String[] ingrProjection = { RecipeContract.IngredientsEntry.COLUMN_NAME_INGREDIENT };
        mIngredientsAdapter.clear();

        // Sort recipes by decreasing order of date
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
                RecipeItem recipeItem = new RecipeItem(
                        c.getLong(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_ENTRY_ID)),
                        c.getString(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_TITLE)),
                        c.getString(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_DESCRIPTION)),
                        c.getString(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_INGREDIENTS)),
                        c.getBlob(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_IMAGE)),
                        new Date(c.getLong(c.getColumnIndexOrThrow(RecipeContract.RecipeEntry.COLUMN_NAME_UPDATE_TIME)))
                );
                mAdapter.add(recipeItem);
            }

            c = db.query(RecipeContract.IngredientsEntry.TABLE_NAME, ingrProjection,
                    null, null, null, null, null);
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                mIngredientsAdapter.add(c.getString(c.getColumnIndexOrThrow((
                        RecipeContract.IngredientsEntry.COLUMN_NAME_INGREDIENT))));
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
