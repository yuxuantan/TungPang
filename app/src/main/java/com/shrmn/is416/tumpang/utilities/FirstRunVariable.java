package com.shrmn.is416.tumpang.utilities;

public class FirstRunVariable {
    private boolean isFirstRun = false;
    private VariableChangeListener variableChangeListener;

    public boolean isFirstRun() {
        return isFirstRun;
    }

    public void firstRun() {
        if(!isFirstRun) {
            isFirstRun = true;
            if(variableChangeListener != null) variableChangeListener.onVariableChanged();
        }
    }

    public void setVariableChangeListener(VariableChangeListener variableChangeListener) {
        this.variableChangeListener = variableChangeListener;
    }

}
