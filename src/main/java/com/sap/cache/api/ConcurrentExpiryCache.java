package com.sap.cache.api;

import com.sap.cache.notification.INotification;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

public class ConcurrentExpiryCache implements ICache {
    private final static int DEFAULT_CAPACITY = 10;
    private final static int DEFAULT_EXPIRY_MILLIS = 3000;

    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
    private final DelayQueue<CacheItem> delayQueue = new DelayQueue<>();

    private final long expiryDurationInMillis;
    private final int capacity;

    private ConcurrentExpiryCache(int capacity, long expiryTime, TimeUnit timeUnit, INotification notifier) {
        this.capacity = capacity;
        this.expiryDurationInMillis = timeUnit.toMillis(expiryTime);
        final Runnable cacheCleanerTask = new CacheCleanerTask(cache, delayQueue, notifier);

        Thread expirationCollector = new Thread(cacheCleanerTask);
        expirationCollector.setDaemon(true);
        expirationCollector.start();
    }

//    public ConcurrentExpiryCache(int capacity, long expiryTime, TimeUnit timeUnit, INotification notifier) {
//        this(capacity, timeUnit.toMillis(expiryTime), notifier);
//    }
//
//    public ConcurrentExpiryCache(int capacity, long expiryTime, TimeUnit timeUnit) {
//        this(capacity, expiryTime, timeUnit, null);
//    }

    public static CacheBuilder builder() {
        return new CacheBuilder();
    }

    public void add(final String key, final Object value) {
        if (isCacheFull()) {
            System.out.println("Cache full, please wait for the expiry period.");
            return;
        }
        if (Objects.isNull(key)) {
            return;
        }
        cache.put(key, value);
        addToDelayQueue(key, value);
    }

    private void addToDelayQueue(final String key, final Object value) {
        long expiryTime = System.currentTimeMillis() + expiryDurationInMillis;
        CacheItem cacheItem = new CacheItem(key, value, expiryTime);
        delayQueue.remove(cacheItem);
        delayQueue.put(cacheItem);
    }

    public Object get(final String key) {
        if (Objects.isNull(key)) {
            return null;
        }
        Object value = cache.get(key);
        if (value != null) {
            addToDelayQueue(key, value);
            return value;
        }
        return null;
    }

    private boolean isCacheFull() {
        return size() == capacity;
    }

    public void remove(final String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    public long size() {
        return cache.size();
    }

    public Map<String, Object> showCache() {
        return cache;
    }

    public static class CacheBuilder {

        private Integer capacity;
        private long expiryTime;
        private TimeUnit timeUnit;
        private INotification notifier;

        CacheBuilder() {
        }

        public CacheBuilder withCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public CacheBuilder expireAfter(long expiryTime) {
            this.expiryTime = expiryTime;
            return this;
        }

        public CacheBuilder timeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        public CacheBuilder withNotifier(INotification notifier) {
            this.notifier = notifier;
            return this;
        }

        public ConcurrentExpiryCache build() {
            int capacityLimit = capacity == null ? DEFAULT_CAPACITY : capacity;
            long expiryTime = this.expiryTime <= 0 ? DEFAULT_EXPIRY_MILLIS : this.expiryTime;
            TimeUnit unit = this.timeUnit == null ? TimeUnit.MILLISECONDS: timeUnit;

            return new ConcurrentExpiryCache(capacityLimit, expiryTime, unit, notifier);
        }

    }
}
