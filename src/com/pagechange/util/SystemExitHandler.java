package com.pagechange.util;

public class SystemExitHandler implements ExitHandler {
    @Override
    public void exit(int code) {
        System.exit(code);
    }
}

