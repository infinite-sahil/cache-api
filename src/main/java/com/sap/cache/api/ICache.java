package com.sap.cache.api;

public interface ICache {

    void add(String key, Object value);

    void remove(String key);

    Object get(String key);

    void clear();

    long size();
}
