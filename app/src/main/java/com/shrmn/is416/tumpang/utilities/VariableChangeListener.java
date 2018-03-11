package com.shrmn.is416.tumpang.utilities;

// Adapted from https://stackoverflow.com/questions/7157123/in-android-how-do-i-take-an-action-whenever-a-variable-changes/7157281#7157281
public interface VariableChangeListener {
    public void onVariableChanged(Object... variableThatHasChanged);
}