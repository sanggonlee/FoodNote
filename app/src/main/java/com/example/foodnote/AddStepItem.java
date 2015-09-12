package com.example.foodnote;

/**
 * Created by Sanggon on 2015-09-11.
 */
public class AddStepItem {
    public String step;
    private boolean isEditing;

    public AddStepItem(String step) {
        this.step = step;
        this.isEditing = true;
    }

    public boolean getIsEditing() {
        return isEditing;
    }

    public void setIsEditing(boolean editing) {
        this.isEditing = editing;
    }
}
