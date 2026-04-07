package org.leochel.repositories;

import org.leochel.KeyValuePair;

import java.util.function.Consumer;

public class TarantoolRepository implements Repository{
    @Override
    public void put(String key, byte[] value) {

    }

    @Override
    public void delete(String key) {

    }

    @Override
    public byte[] get(String key) {
        return new byte[0];
    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public void range(String keySince, String keyTo, Consumer<KeyValuePair> consumer) {

    }
}
