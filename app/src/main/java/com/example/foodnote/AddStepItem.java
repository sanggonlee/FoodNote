package com.example.foodnote;

/**
 * Created by Sanggon on 2015-09-11.
 */
public class AddStepItem {
    public String step;
    private boolean isEditing;
    private int height;

    public AddStepItem(String step) {
        this.step = step;
        this.isEditing = true;
        this.height = 400;
    }

    public boolean getIsEditing() {
        return isEditing;
    }

    public void setIsEditing(boolean editing) {
        this.isEditing = editing;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int h) {
        this.height = h;
    }
}
