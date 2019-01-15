package com.sap.cache;

import com.sap.cache.api.ConcurrentExpiryCache;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class ConcurrentCacheTest {
    ConcurrentExpiryCache cache;

    private void loadCache() {
        for (int i = 0; i < 30; i++) {
            cache.add(String.valueOf(i), "test" + i);
        }
    }

    @Test
    public void testCacheSize() {
        int expectedCapacity = 30;
        cache = ConcurrentExpiryCache.builder()
                .withCapacity(expectedCapacity)
                .expireAfter(2)
                .timeUnit(TimeUnit.SECONDS)
                .build();
        loadCache();
        assert cache.size() == expectedCapacity;
    }

    @Test
    public void testCacheExpiryOnWrite() throws InterruptedException {
        cache = ConcurrentExpiryCache.builder()
                .withCapacity(5)
                .expireAfter(2)
                .timeUnit(TimeUnit.SECONDS)
                .build();
        cache.add("1", "test1");
        Thread.sleep(2500);
        cache.add("2", "test2");
        cache.add("3", "test3");

        assert cache.size() == 2;
        assert cache.get("1") == null;
    }

    @Test
    public void testCacheExpiryOnRead() throws InterruptedException {
        cache = ConcurrentExpiryCache.builder()
                .withCapacity(5)
                .expireAfter(2)
                .timeUnit(TimeUnit.SECONDS)
                .build();
        cache.add("1", "test1");
        Thread.sleep(1000);
        cache.add("2", "test2");
        Thread.sleep(1000);
        cache.add("3", "test3");
        cache.get("2");
        Thread.sleep(1000);

        assert cache.size() == 2;
        assert cache.get("2") == "test2";
    }

    @Test
    public void testNotification() throws InterruptedException {
        cache = ConcurrentExpiryCache.builder()
                .withCapacity(5)
                .expireAfter(2)
                .timeUnit(TimeUnit.SECONDS)
                .withNotifier(value -> {
                    assert value == "3";
                })
                .build();
        cache.add("1", "test1");
        Thread.sleep(1000);
        cache.add("2", "test2");
        Thread.sleep(1000);
        cache.add("3", "test3");
        Thread.sleep(3000);
    }

}
