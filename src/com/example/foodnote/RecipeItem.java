package com.example.foodnote;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Intent;

public class RecipeItem {

	public static final String ITEM_SEP = System.getProperty("line.separator");

	public enum Priority {
		LOW, MED, HIGH
	};

	public final static String TITLE = "title";
	public final static String DESCRIPTION = "description";
	public final static String INGREDIENTS = "ingredients";
	public final static String DATE = "date";
	public final static String FILENAME = "filename";

	public final static SimpleDateFormat FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.US);

	private String mTitle = new String();
	private String mDescription = new String();
	private String mIngredients = new String();
	private Date mDate = new Date();

	RecipeItem(String title, String description, String ingredients, Date date) {
		this.mTitle = title;
		this.mDescription = description;
		this.mIngredients = ingredients;
		this.mDate = date;
	}

	// Create a new RecipeItem from data packaged in an Intent
	RecipeItem(Intent intent) {
		mTitle = intent.getStringExtra(RecipeItem.TITLE);
		mDescription = intent.getStringExtra(RecipeItem.DESCRIPTION);
		mIngredients = intent.getStringExtra(RecipeItem.INGREDIENTS);

		try {
			mDate = RecipeItem.FORMAT.parse(intent.getStringExtra(RecipeItem.DATE));
		} catch (Exception e) {
			mDate = new Date();
		}
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public String getIngredients() {
		return mIngredients;
	}

	public void setIngredients(String ingredients) {
		mIngredients = ingredients;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date date) {
		mDate = date;
	}

	// Take a set of String data values and package them for transport in an Intent
	public static void packageIntent(Intent intent, String title,
			String description, String ingredients, String date) {
		intent.putExtra(RecipeItem.TITLE, title);
		intent.putExtra(RecipeItem.DESCRIPTION, description);
		intent.putExtra(RecipeItem.INGREDIENTS, ingredients);
		intent.putExtra(RecipeItem.DATE, date);
	}

	public String toString() {
		return mTitle + ITEM_SEP + mDescription + ITEM_SEP + mIngredients + ITEM_SEP
				+ FORMAT.format(mDate);
	}

	public String toLog() {
		return "Title:" + mTitle + ITEM_SEP + "Description:" + mDescription
				+ ITEM_SEP + "Ingredients:" + mIngredients + ITEM_SEP + "Date:"
				+ FORMAT.format(mDate);
	}

}