package com.example.foodnote;

/**
 * Created by Sanggon on 2015-09-11.
 */
public class AddStepItem {
    private String step;
    private boolean isEditing;
    private int height;

    public AddStepItem(String step) {
        this.step = step;
        this.isEditing = true;
        this.height = 200;
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

    public String getStep() {
        return step;
    }

    public void setStep(String s) {
        step = s;
    }
}
