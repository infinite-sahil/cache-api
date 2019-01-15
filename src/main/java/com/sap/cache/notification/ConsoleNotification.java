package com.sap.cache.notification;

public class ConsoleNotification implements INotification{

    @Override
    public void notify(final Object evictedValue) {
        // do work
        System.out.println("Evicted " + evictedValue + " from the cache.");
    }
}
