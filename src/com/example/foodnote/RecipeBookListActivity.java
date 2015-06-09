package com.example.foodnote;

import java.util.Date;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.example.foodnote.RecipeContract.RecipeEntry;

public class RecipeBookListActivity extends ListActivity {
	private static final int ADD_RECIPE_ITEM_REQUEST = 0;
	private static final String TAG = "RecipeBookListActivity";

	RecipeListAdapter mAdapter;
	RecipeDbHelper mDbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAdapter = new RecipeListAdapter(getApplicationContext());
		mDbHelper = new RecipeDbHelper(getApplicationContext());
		
		getListView().setFooterDividersEnabled(true);
		
		LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
				  (Context.LAYOUT_INFLATER_SERVICE);
		TextView footerView = (TextView)inflater.inflate(R.layout.footer_view,null);
		
		if (null == footerView) {
			return;
		}
		
		// Add footerView to ListView
		getListView().addFooterView(footerView);
		
		footerView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Log.i(TAG,"Entered footerView.OnClickListener.onClick()");

				Intent intent = new Intent(RecipeBookListActivity.this, AddRecipeActivity.class);
				startActivityForResult(intent, ADD_RECIPE_ITEM_REQUEST);
			}
		});

		getListView().setAdapter(mAdapter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG,"Entered onActivityResult()");

		// Check result code and request code if user submitted a new RecipeItem
		// Create a new RecipeItem from the data Intent and then add it to the adapter
		if (resultCode == RESULT_OK && requestCode == ADD_RECIPE_ITEM_REQUEST) {
			RecipeItem recipeItem = new RecipeItem(data);
			insertRecipeDataToDb(recipeItem);
			mAdapter.add(recipeItem);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();

		// Load saved RecipeItems, if necessary
		if (mAdapter.getCount() == 0) {
			//mDbHelper = new RecipeDbHelper(getApplicationContext());
			loadItems();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Save RecipeItems

		//saveItems();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.food_note_manager, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		//if (id == R.id.action_settings) {
		//	return true;
		//}
		return super.onOptionsItemSelected(item);
	}
	
	public void insertRecipeDataToDb(RecipeItem recipeItem) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(RecipeEntry.COLUMN_NAME_ENTRY_ID, mAdapter.getCount());
		values.put(RecipeEntry.COLUMN_NAME_TITLE, recipeItem.getTitle());
		values.put(RecipeEntry.COLUMN_NAME_DESCRIPTION, recipeItem.getDescription());
		values.put(RecipeEntry.COLUMN_NAME_INGREDIENTS, recipeItem.getIngredients());
		values.put(RecipeEntry.COLUMN_NAME_UPDATE_TIME, recipeItem.getDate().getTime());
		
		Log.i(TAG, "Inserting a Recipe entry data " + values.toString() + " to DB");
		db.insert(RecipeEntry.TABLE_NAME, null, values);
	}

	// Load stored RecipeItems
	private void loadItems() {
    	// HACK! Uncomment the below line when schema is changed (only once)
		// TODO: To be removed when the schema is finalized
		//getApplicationContext().deleteDatabase(RecipeDbHelper.DATABASE_NAME);
		
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		// Specifies which columns to use from the database
		String[] projection = {
		    RecipeEntry._ID,
		    RecipeEntry.COLUMN_NAME_TITLE,
		    RecipeEntry.COLUMN_NAME_DESCRIPTION,
		    RecipeEntry.COLUMN_NAME_INGREDIENTS,
		    RecipeEntry.COLUMN_NAME_UPDATE_TIME,
		};

		// Sort by decreasing order of date
		String sortOrder = RecipeEntry.COLUMN_NAME_UPDATE_TIME + " DESC";

		Cursor c = db.query(
		    RecipeEntry.TABLE_NAME,
		    projection,
		    null,
		    null,
		    null,
		    null,
		    sortOrder
		);
		
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			mAdapter.add(new RecipeItem(
					c.getString(c.getColumnIndexOrThrow(RecipeEntry.COLUMN_NAME_TITLE)),
					c.getString(c.getColumnIndexOrThrow(RecipeEntry.COLUMN_NAME_DESCRIPTION)),
					c.getString(c.getColumnIndexOrThrow(RecipeEntry.COLUMN_NAME_INGREDIENTS)),
					new Date(c.getLong(c.getColumnIndexOrThrow(RecipeEntry.COLUMN_NAME_UPDATE_TIME)))
			));
		}
	}
}
