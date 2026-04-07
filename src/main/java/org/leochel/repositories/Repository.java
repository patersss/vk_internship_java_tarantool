package org.leochel.repositories;

import org.leochel.KeyValuePair;

import java.util.function.Consumer;

public interface Repository {
    void put(String key, byte[] value);

    void delete(String key);

    byte[] get(String key);

    int count();

    void range(String keySince, String keyTo, Consumer<KeyValuePair> consumer);
}
