package com.ibm.ta.sdk.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadStream implements Runnable {
    String name;
    InputStream is;
    Thread thread;
    StringBuffer sb;
    public ReadStream(String name, InputStream is) {
        this.name = name;
        this.is = is;
        this.sb = new StringBuffer();
    }
    public void start () {
        thread = new Thread (this);
        thread.start ();
    }
    public void run () {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            try {
                is.close ();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getMsg() {
        return sb.toString();
    }
}

