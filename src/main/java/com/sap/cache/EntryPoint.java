package com.sap.cache;


import com.sap.cache.api.ConcurrentExpiryCache;
import com.sap.cache.notification.ConsoleNotification;
import java.util.concurrent.TimeUnit;

// Driver class which uses the cache api
public class EntryPoint {

    public static final int SLEEP_MILLIS = 1000;

    public static void main(String[] args) throws InterruptedException {
        ConcurrentExpiryCache cache = ConcurrentExpiryCache.builder()
                .withCapacity(5)
                .expireAfter(5)
                .timeUnit(TimeUnit.SECONDS)
                .withNotifier(new ConsoleNotification())
                .build();

        cache.add("1", "batman");
        Thread.sleep(SLEEP_MILLIS);
        cache.add("2", "ironman");
        Thread.sleep(SLEEP_MILLIS);
        cache.add("3", "panther");
        Thread.sleep(SLEEP_MILLIS);
        cache.add("4", "hulk");
        Thread.sleep(SLEEP_MILLIS);
        cache.add("5", "flash");
        Thread.sleep(SLEEP_MILLIS);

        System.out.println(cache.showCache());

        new Thread(() -> {
            while (true) {
                System.out.println(cache.showCache());
                try {
                    Thread.sleep(SLEEP_MILLIS);
                } catch (InterruptedException ignore) {
                }
                cache.get("3");
            }
        }).start();
    }

}
