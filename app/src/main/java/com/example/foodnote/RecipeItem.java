package com.example.foodnote;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Intent;

/*
 * Simple Pojo class for RecipeItem
 */
public class RecipeItem {

	public static final String ITEM_SEP = System.getProperty("line.separator");

	public final static String TITLE = "title";
	public final static String DESCRIPTION = "description";
	public final static String INGREDIENTS = "ingredients";
	public final static String DATE = "date";

	public final static SimpleDateFormat FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.US);

	private long mId;	// id generated by db
	private String mTitle;
	private String mDescription;
	private String mIngredients;
	private byte[] mPictureBlob;
	private Date mDate = new Date();
	private List<StepItem> mSteps;

	RecipeItem(long id, String title, String description, String ingredients, byte[] blob, Date date) {
		this.mId = id;
		this.mTitle = title;
		this.mDescription = description;
		this.mIngredients = ingredients;
		this.mPictureBlob = blob;
		this.mDate = date;
		this.mSteps = new ArrayList<>();
	}

	public long getId() {
		return mId;
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

	public byte[] getPictureBlob() {
		return mPictureBlob;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date date) {
		mDate = date;
	}

	public List<StepItem> getSteps() {
		return mSteps;
	}

	public void addStep(StepItem step) {
		mSteps.add(step);
	}

	public String toString() {
		return mTitle + ITEM_SEP + mDescription + ITEM_SEP + mIngredients + ITEM_SEP
				+ FORMAT.format(mDate);
	}
}
