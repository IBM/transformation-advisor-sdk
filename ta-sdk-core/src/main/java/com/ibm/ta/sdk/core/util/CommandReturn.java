package com.ibm.ta.sdk.core.util;

public class CommandReturn {
    private int returnCode;
    private String output;
    private String error;

    public CommandReturn(int returnCode, String output, String error) {
        this.returnCode = returnCode;
        this.output = output;
        this.error = error;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public String getOutput() {
        return output;
    }

    public String getError() {
        return error;
    }
}
