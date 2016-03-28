package com.example.foodnote;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.foodnote.backend.apis.recipeApi.model.Recipe;

public class RecipeListAdapter extends BaseAdapter {
	private List<Recipe> mItems = new ArrayList<Recipe>();
	private final Context mContext;

	private static final String TAG = "RecipeListAdapter";

	public RecipeListAdapter(Context context) {
		mContext = context;
	}

	// Add a RecipeItem to the adapter
	// Notify observers that the data set has changed
	public void add(Recipe item) {
		mItems.add(item);
		notifyDataSetChanged();
	}

	public void setRecipes(List<Recipe> recipes) {
		mItems = recipes;
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
		final Recipe recipeItem = mItems.get(position);

		// Inflate the View for this RecipeItem from recipe_item.xml
		RelativeLayout itemLayout = convertView != null ? (RelativeLayout) convertView :
					(RelativeLayout) LayoutInflater.from(mContext)
				.inflate(R.layout.recipe_item, parent, false);

		final ImageView imageView = (ImageView)itemLayout.getChildAt(0);
		if (recipeItem.getImageData() != null) {
			imageView.setImageBitmap(BitmapFactory.decodeByteArray(
					recipeItem.getImageData().getBytes(), 0, recipeItem.getImageData().getBytes().length));
		} else {
			// If there's no image set, set as the placeholder image
			imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.taco128));
		}

		final TextView titleView = (TextView)((RelativeLayout)itemLayout.getChildAt(1)).getChildAt(0);
		titleView.setText(recipeItem.getTitle());

		final TextView descriptionView = (TextView)((RelativeLayout)itemLayout.getChildAt(1)).getChildAt(1);
		descriptionView.setText(recipeItem.getDescription());

		final TextView dateView = (TextView)((RelativeLayout)itemLayout.getChildAt(1)).getChildAt(2);
		dateView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(
				new Date(recipeItem.getDate().getValue())));

		// Return the View you just created
		return itemLayout;

	}
}
