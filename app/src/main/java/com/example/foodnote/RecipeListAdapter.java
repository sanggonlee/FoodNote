package com.example.foodnote;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class RecipeListAdapter extends BaseAdapter {
	private final List<RecipeItem> mItems = new ArrayList<RecipeItem>();
	private final Context mContext;

	private static final String TAG = "RecipeListAdapter";

	public RecipeListAdapter(Context context) {
		mContext = context;
	}

	// Add a RecipeItem to the adapter
	// Notify observers that the data set has changed
	public void add(RecipeItem item) {
		mItems.add(item);
		notifyDataSetChanged();
	}

	// Clears the list adapter of all items.
	public void clear() {
		mItems.clear();
		notifyDataSetChanged();
	}

	// Returns the number of RecipeItems
	@Override
	public int getCount() {
		return mItems.size();
	}

	// Retrieve the number of RecipeItems
	@Override
	public Object getItem(int pos) {
		return mItems.get(pos);
	}

	// Get the ID for the RecipeItem
	// In this case it's just the position
	@Override
	public long getItemId(int pos) {
		return pos;
	}

	// Create a View for the RecipeItem at specified position
	// Remember to check whether convertView holds an already allocated View
	// before created a new View.
	// TODO: Consider using the ViewHolder pattern to make scrolling more efficient
	// See: http://developer.android.com/training/improving-layouts/smooth-scrolling.html
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final RecipeItem recipeItem = mItems.get(position);

		// Inflate the View for this RecipeItem from recipe_item.xml
		RelativeLayout itemLayout = convertView != null ? (RelativeLayout) convertView :
					(RelativeLayout) LayoutInflater.from(mContext)
				.inflate(R.layout.recipe_item, parent, false);

		// Display Title in TextView
		final TextView titleView = (TextView)itemLayout.getChildAt(0);
		titleView.setText(recipeItem.getTitle());

		final TextView descriptionView = (TextView)itemLayout.getChildAt(1);
		descriptionView.setText(recipeItem.getDescription());

		// Display Time and Date.
		final TextView dateView = (TextView)itemLayout.getChildAt(2);
		dateView.setText(RecipeItem.FORMAT.format(recipeItem.getDate()));

		// Return the View you just created
		return itemLayout;

	}
}
