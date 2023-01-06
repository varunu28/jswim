package com.varun.swim.logging;

public class CustomLogger {

    public void logDebug(String log) {
        System.out.println("\u001B[34m" + log + "\u001B[0m");
    }

    public void logInfo(String log) {
        System.out.println("\u001B[32m" + log + "\u001B[0m");
    }

    public void logError(String log) {
        System.out.println("\u001B[31m" + log + "\u001B[0m");
    }
}
