package com.sap.cache.api;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class CacheItem implements Delayed {

    private String key;
    private Object value;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final long expiryTime;

    public long getDelay(final TimeUnit unit) {
        return unit.convert(expiryTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public int compareTo(final Delayed item) {
        return Long.valueOf(expiryTime - ((CacheItem) item).expiryTime).intValue();
    }
}
