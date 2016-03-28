package com.example.foodnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Base64;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.foodnote.backend.apis.recipeApi.RecipeApi;
import com.example.foodnote.backend.apis.recipeApi.model.Recipe;
import com.google.api.client.util.DateTime;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<List<Recipe>> {
    String TAG = "MainActivity";

    private static final int IMAGE_CHOOSE_REQ_CODE = 0;
    private static final int SIGN_IN_REQ_CODE = 1;

    SharedPreferences sharedPreferences;

    Loader<List<Recipe>> mLoader;

    RelativeLayout addRecipeRightRL;
    RelativeLayout viewRecipeRightRL;
    DrawerLayout drawerLayout;

    EditText mRecipeAddTitleText;
    EditText mRecipeAddDescription;
    MultiAutoCompleteTextView mRecipeAddIngredients;

    ImageButton mPictureButton;
    @Nullable Bitmap mPictureBitmap;

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

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);

        // transparent action bar
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        if (getActionBar() != null) {
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen);

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
                if (ActionStateSingleton.getInstance().getEditorAction() == ActionStateSingleton.EditorAction.Edit
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
                // Send the entered ingredients whenever focus leaves the Ingredients section
                if (!hasFocus) {
                    mAddStepAdapter.setIngredients(
                            mRecipeAddIngredients.getText().toString().split("[ ]*,[ ]*"));
                }
            }
        });

        mPictureButton = (ImageButton)findViewById(R.id.recipeAddPicture);
        mPictureBitmap = null;

        /** Preferences call from last time before app close    By. CHAN */
        unsavedTitle = sharedPreferences.getString("unsavedTitle", "");
        unsavedDescription = sharedPreferences.getString("unsavedDescription", "");
        unsavedIngredients = sharedPreferences.getString("unsavedIngredients", "");
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
                Recipe recipeItem = (Recipe)parent.getItemAtPosition(position);

                if (recipeItem == null) {
                    Log.i(TAG, "The selected recipe item is null! Investigate!");
                    return;
                }

                ActionStateSingleton.getInstance().setViewId(recipeItem.getId());

                TextView recipeViewTitleText = (TextView) findViewById(R.id.recipeViewTitle);
                recipeViewTitleText.setText(recipeItem.getTitle());

                TextView recipeViewDescriptionText = (TextView) findViewById(R.id.recipeViewDescription);
                recipeViewDescriptionText.setText(recipeItem.getDescription());

                TextView recipeViewIngredientsText = (TextView) findViewById(R.id.recipeViewIngredients);
                recipeViewIngredientsText.setText(
                        getResources().getString(R.string.ingredients_prefix_string)
                                + " " + recipeItem.getIngredients());

                ImageView recipeViewPicture = (ImageView) findViewById(R.id.recipeViewPicture);
                String blobData = recipeItem.getImageData();
                if (blobData != null) {
                    recipeViewPicture.setImageBitmap(BitmapFactory
                            .decodeByteArray(blobData.getBytes(), 0, blobData.getBytes().length));
                } else {
                    // If no picture was set yet, set the placeholder image
                    recipeViewPicture.setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.taco128));
                }

                mViewStepAdapter.clear();
                new StepLoadAsyncTask().execute(recipeItem.getId());

                drawerLayout.openDrawer(viewRecipeRightRL);
            }
        });

        // set ingredients data for ingredients section
        mIngredientsAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line);
        mRecipeAddIngredients.setAdapter(mIngredientsAdapter);
        mRecipeAddIngredients.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        final CheckBox cloudUploadCheckbox = (CheckBox)findViewById(R.id.recipeAddUploadToCloudCheckbox);
        cloudUploadCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Show dialog just in case starting activity takes longer than usual
                    ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Checking user information..");
                    progressDialog.show();

                    Intent signInIntent = new Intent(MainActivity.this, SignInActivity.class);
                    startActivityForResult(signInIntent, SIGN_IN_REQ_CODE);

                    progressDialog.dismiss();
                }
            }
        });

        mLoader = getSupportLoaderManager().initLoader(1, null, this);
        mLoader.startLoading();
    }

    @Override
    public Loader<List<Recipe>> onCreateLoader(int id, Bundle args) {
        return new RecipeLoader(MainActivity.this);
    }
    @Override
    public void onLoadFinished(Loader<List<Recipe>> loader, List<Recipe> data) {
        mAdapter.setRecipes(data);
        mAdapter.notifyDataSetChanged();
    }
    @Override
    public void onLoaderReset(Loader<List<Recipe>> loader) {
        mAdapter.notifyDataSetChanged();
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
                ActionStateSingleton.getInstance().setEditorAction(ActionStateSingleton.EditorAction.Create);
                if (mAddStepAdapter.getCount() == 0) {
                    mAddStepAdapter.addAndAdjustHeight(new StepItem(""));
                }
                mRecipeAddTitleText.requestFocus();
                drawerLayout.openDrawer(addRecipeRightRL);
                return true;
            case R.id.recipe_book:
                return true;
            case R.id.recipe_world:
                Intent signInIntent = new Intent(MainActivity.this, SignInActivity.class);
                startActivityForResult(signInIntent, SIGN_IN_REQ_CODE);
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

    /*
     *  Decode the image from Uri, fix its size and prevent from eating
     *  too much memory
     */
    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        final int FIXED_IMAGE_WIDTH = 300;
        final int FIXED_IMAGE_HEIGHT = 225;

        try {
            return Bitmap.createScaledBitmap(
                    MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage),
                    FIXED_IMAGE_WIDTH, FIXED_IMAGE_HEIGHT, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CHOOSE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                try {
                    mPictureBitmap = decodeUri(imageUri);
                    Log.i(TAG, "bitmap size = "+ mPictureBitmap.getByteCount());
                    mPictureButton.setBackgroundResource(0);
                    mPictureButton.setImageBitmap(mPictureBitmap);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    Toast.makeText(getApplicationContext(),
                            "Couldn't upload the picture. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == SIGN_IN_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                // could save user info, but not needed at this scope for now
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
        Log.i(TAG, "submit button clicked");

        // Do not submit with empty title
        if (mRecipeAddTitleText.getText().length() == 0) {
            Toast.makeText(getApplicationContext(),
                    "Recipe title missing",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Compress the uploaded picture to insert into db
        byte[] compressedPicture = null;
        if (mPictureBitmap != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mPictureBitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            compressedPicture = outputStream.toByteArray();
        }

        Long recipeId;
        if (ActionStateSingleton.getInstance().getEditorAction() == ActionStateSingleton.EditorAction.Create) {
            recipeId = null; // let SQLite generate its id
        } else if (ActionStateSingleton.getInstance().getEditorAction() == ActionStateSingleton.EditorAction.Edit) {
            recipeId = ActionStateSingleton.getInstance().getViewId();
        } else {
            // better to crash than corrupting db..
            throw new RuntimeException("Trying to save data when action is not specified.");
        }

        Recipe recipe = new Recipe();
        recipe.setId(recipeId);
        recipe.setTitle(mRecipeAddTitleText.getText().toString());
        recipe.setDescription(mRecipeAddDescription.getText().toString());
        recipe.setIngredients(mRecipeAddIngredients.getText().toString());
        recipe.setDate(new DateTime(new Date()));
        if (compressedPicture != null) {
            recipe.setImageData(Base64.encodeToString(compressedPicture, Base64.DEFAULT));
        }

        List<String> steps = new ArrayList<>();
        // Remove all empty steps before inserting to db
        for (int stepIndex=0; stepIndex<mAddStepAdapter.getCount(); stepIndex++) {
            if (mAddStepAdapter.getItem(stepIndex).getIsSubmitted()) {
                steps.add(mAddStepAdapter.getItem(stepIndex).getStep());
            } else {
                mAddStepAdapter.remove(stepIndex);
                stepIndex--;
            }
        }
        recipe.setSteps(steps);

        CheckBox cloudUploadCheckbox = (CheckBox)findViewById(R.id.recipeAddUploadToCloudCheckbox);
        // Start saving task
        RecipeSaveAsyncTask task = new RecipeSaveAsyncTask(cloudUploadCheckbox.isChecked());
        task.execute(recipe);

        mLoader.reset();
    }

    public void onRecipeViewCloseButtonClicked(View view) {
        ActionStateSingleton.getInstance().setEditorAction(ActionStateSingleton.EditorAction.None);
        drawerLayout.closeDrawer(viewRecipeRightRL);
    }

    public void onRecipeViewDeleteButtonClicked(View view) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Delete recipe")
                .setMessage("Are you sure you want to delete this recipe? This action cannot be undone.")
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new RecipeDeleteAsyncTask().execute(
                                ActionStateSingleton.getInstance().getViewId());
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
        ActionStateSingleton.getInstance().setEditorAction(ActionStateSingleton.EditorAction.Edit);

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
            StepItem item = mViewStepAdapter.getItem(itemPos);
            item.setIsEditing(false);
            mAddStepAdapter.add(item);
        }
        mAddStepAdapter.add(new StepItem(""));
        AddStepListAdapter.recalculateListViewHeight(viewStepsList, addStepsList);
    }

    private String stripFormattedIngredients(String formatted) {
        // For now, just remove the prefix label "Ingredients:"
        return formatted.substring(
                getResources().getString(R.string.ingredients_prefix_string).length());
    }

    private void clearContents() {
        ActionStateSingleton.getInstance().setEditorAction(ActionStateSingleton.EditorAction.None);
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
        mIngredientsAdapter.clear();

        mLoader.startLoading();
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

        SharedPreferences.Editor editor = sharedPreferences.edit();
        unsavedTitle = mRecipeAddTitleText.getText().toString();
        unsavedDescription = mRecipeAddDescription.getText().toString();
        unsavedIngredients = mRecipeAddIngredients.getText().toString();
        editor.putString("unsavedTitle", unsavedTitle);
        editor.putString("unsavedDescription", unsavedDescription);
        editor.putString("unsavedIngredients", unsavedIngredients);
        editor.commit();
    }
    /***/

    private class RecipeSaveAsyncTask extends AsyncTask<Recipe, Integer, Recipe> {
        private final String TAG = RecipeSaveAsyncTask.class.getSimpleName();

        Boolean saveToServer;
        String message;
        ProgressDialog progressDialog;

        public RecipeSaveAsyncTask(Boolean saveToServer) {
            this.saveToServer = saveToServer;
            this.progressDialog = new ProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Recipe doInBackground(Recipe... param) {
            Recipe recipe = param[0];

            if (saveToServer) {
                progressDialog.setMessage("Saving recipe to the server...");

                // Request backend service
                RecipeApi recipeApi = CloudEndpointBuilderHelper.getRecipeEndpoints();
                try {
                    Log.i(TAG, "going into api");
                    recipe = recipeApi.insert(recipe).execute();    // obtain id generated by server
                } catch (IOException e) {
                    e.printStackTrace();
                    message = "There was an error uploading to Recipe World. Please try again.";
                }
            }

            publishProgress();  // update progress dialog

            try {
                // Insert to local db
                mDbHelper.insertRecipeDataToDb(recipe);
            } catch (Exception e) {
                e.printStackTrace();
                message = "Could not save to Recipe Book. Please try again.";
                return null;
            }

            if (message == null) {  // means nothing bad occurred
                message = "Successfully saved the recipe for \"" + recipe.getTitle() + "\"";
            }

            return recipe;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate();
            progressDialog.setMessage("Saving recipe to the local database...");
        }

        @Override
        protected void onPostExecute(Recipe result) {
            progressDialog.dismiss();

            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

            if (result != null) {   // save to local db successful, reset contents
                clearContents();
                mAddStepAdapter.clear();
                drawerLayout.closeDrawer(addRecipeRightRL);

                // reload items
                mAdapter.clear();
                loadItems();
            }
        }
    }

    public class RecipeDeleteAsyncTask extends AsyncTask<Long, Integer, Void> {
        private final String TAG = RecipeDeleteAsyncTask.class.getSimpleName();

        String message;
        ProgressDialog progressDialog;

        public RecipeDeleteAsyncTask() {
            this.progressDialog = new ProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Long... recipeId) {
            try {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                db.delete(RecipeContract.StepEntry.TABLE_NAME,
                        RecipeContract.StepEntry.COLUMN_NAME_RECIPE_ID + " = "
                                + Long.toString(recipeId[0]),
                        null);
                db.delete(RecipeContract.RecipeEntry.TABLE_NAME,
                        RecipeContract.RecipeEntry._ID + " = " + Long.toString(recipeId[0]),
                        null);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                message = "Could not delete the recipe. Please try again.";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            progressDialog.dismiss();

            if (message == null) {
                message = "The recipe got deleted succesfully.";

                // reload UIs
                mAdapter.clear();
                loadItems();

                drawerLayout.closeDrawer(viewRecipeRightRL);
            }

            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public class StepLoadAsyncTask extends AsyncTask<Long, Integer, Void> {
        private final String TAG = StepLoadAsyncTask.class.getSimpleName();

        List<StepItem> steps = new ArrayList<>();

        @Override
        protected Void doInBackground(Long... recipeId) {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            Cursor c = null;
            try {
                // retrieve step data
                String[] projection = {
                        RecipeContract.StepEntry.COLUMN_NAME_RECIPE_ID,
                        RecipeContract.StepEntry.COLUMN_NAME_STEP_NUM,
                        RecipeContract.StepEntry.COLUMN_NAME_STEP_DESCRIPTION,
                };

                // Sort by step numbers
                String sortOrder = RecipeContract.StepEntry.COLUMN_NAME_STEP_NUM + " ASC";
                c = db.query(
                        RecipeContract.StepEntry.TABLE_NAME,
                        projection,
                        RecipeContract.StepEntry.COLUMN_NAME_RECIPE_ID + " = "
                                + Long.toString(recipeId[0]),
                        null,
                        null,
                        null,
                        sortOrder
                );

                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    steps.add(new StepItem(c.getString(c.getColumnIndexOrThrow(
                            RecipeContract.StepEntry.COLUMN_NAME_STEP_DESCRIPTION))));
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            mViewStepAdapter.setItems(steps);
        }
    }

    public class SwipeDetector implements View.OnTouchListener {
        private Activity activity;
        static final int MIN_DISTANCE = 60;
        private float downX, upX;

        public SwipeDetector(Activity activity) {
            this.activity = activity;
        }

        public void onSwipeRight() {
            ((MainActivity) activity).onSwipeRight();
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
