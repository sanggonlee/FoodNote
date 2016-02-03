package com.example.foodnote;

public class StepItem {
    private String step;
    private boolean isEditing;
    private boolean isSubmitted;

    public StepItem(String step) {
        this.step = step;
        this.isEditing = true;
        this.isSubmitted = false;
    }

    public boolean getIsEditing() {
        return isEditing;
    }

    public void setIsEditing(boolean editing) {
        this.isEditing = editing;
    }

    public boolean getIsSubmitted() {
        return isSubmitted;
    }

    public void setIsSubmitted(boolean submitted) {
        this.isSubmitted = submitted;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String s) {
        step = s;
    }
}
