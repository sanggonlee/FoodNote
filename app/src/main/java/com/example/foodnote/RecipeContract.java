package com.example.foodnote;

import android.provider.BaseColumns;

public final class RecipeContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public RecipeContract() {}

    public static abstract class RecipeEntry implements BaseColumns {
    	public static final String TABLE_NAME = "recipe_entry";
    	public static final String COLUMN_NAME_ENTRY_ID = "entry_id";
    	public static final String COLUMN_NAME_TITLE = "title";
    	public static final String COLUMN_NAME_DESCRIPTION = "description";
    	public static final String COLUMN_NAME_INGREDIENTS = "ingredients";
    	public static final String COLUMN_NAME_UPDATE_TIME = "update_time";
    }
}
