package com.shrmn.is416.tumpang.utilities;

public class FirstRunVariable {
    private boolean isFirstRun = false;
    private VariableChangeListener variableChangeListener;

    public boolean isFirstRun() {
        return isFirstRun;
    }

    public void setFirstRun(boolean firstRun) {
        isFirstRun = firstRun;
        if(variableChangeListener != null) variableChangeListener.onVariableChanged(isFirstRun);
    }

    public void setVariableChangeListener(VariableChangeListener variableChangeListener) {
        this.variableChangeListener = variableChangeListener;
    }

}
