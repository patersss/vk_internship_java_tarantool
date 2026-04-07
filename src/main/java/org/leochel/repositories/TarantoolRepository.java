package org.leochel.repositories;

import com.google.protobuf.ByteString;
import io.tarantool.client.box.TarantoolBoxClient;
import org.leochel.KeyValuePair;
import org.leochel.exceptions.TarantoolOperationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class TarantoolRepository implements Repository {
    private static final Logger logger = Logger.getLogger(TarantoolRepository.class.getName());
    private final TarantoolBoxClient client;
    private static final int PAGE_SIZE = 1000;

    public TarantoolRepository(TarantoolBoxClient client) {
        this.client = client;
    }

    @Override
    public void put(String key, byte[] value) {
        try {
            if (value == null) {
                client.call("kv_put", Arrays.asList(key, null)).join();
            } else {
                client.call("kv_put", List.of(key, value)).join();
            }
        } catch (CompletionException e) {
            logger.severe("Exception while put method with key: " + key
                    + ", valueSize: " + (value == null ? "null" : value.length)
                    + " " + e.getCause());
            throw new TarantoolOperationException("put", e.getCause());
        }
    }

    @Override
    public void delete(String key) {
        try {
            client.call("kv_delete", List.of(key)).join();
        } catch (CompletionException e) {
            logger.severe("Exception while delete method with key: " + key +
                    " " + e.getCause());
            throw new TarantoolOperationException("delete", e.getCause());
        }
    }

    @Override
    public KeyValuePair get(String key) {
        Object resultTuple;
        try {
            var result = client.call("kv_get", List.of(key)).join().get();
            if (result == null || result.isEmpty()) {
                return null;
            }
            resultTuple = result.getFirst();
            if (resultTuple == null) {
                return null;
            }

        } catch (CompletionException e) {
            logger.severe("Exception in get method with key: " + key +
                    " " + e.getCause());
            throw new TarantoolOperationException("get", e.getCause());
        }
        return tupleToKeyValuePair(resultTuple);
    }

    @Override
    public long count() {
        try {
            var resultList = client.call("kv_count", List.of()).join().get();

            if (resultList == null || resultList.isEmpty()) {
                return 0;
            }

            Object countResult = resultList.getFirst();
            if (countResult instanceof Number number) {
                return number.longValue();
            }

            throw new IllegalStateException("Unexpected count result type: "
                            + countResult.getClass().getName());
        } catch (CompletionException e) {
            logger.severe("Exception in count method " + e.getCause());
            throw new TarantoolOperationException("count", e.getCause());
        }
    }

    @Override
    public void range(String keySince, String keyTo, Consumer<KeyValuePair> consumer) {
        String afterKey = null;

        while (true) {
            List<Object> args = new ArrayList<>();
            args.add(keySince);
            args.add(keyTo);
            args.add(PAGE_SIZE);
            args.add(afterKey);

            List<?> page;
            try {
                var callResult = client.call("kv_range_page", args).join().get();

                if (callResult == null || callResult.isEmpty()) {
                    break;
                }

                Object inner = callResult.getFirst();
                if (!(inner instanceof List<?> innerList) || innerList.isEmpty()) {
                    break;
                }
                page = innerList;

            } catch (CompletionException e) {
                throw new TarantoolOperationException("range", e.getCause());
            }

            String lastKey = null;
            for (Object item : page) {
                KeyValuePair pair = tupleToKeyValuePair(item);
                if (pair != null) {
                    consumer.accept(pair);
                    lastKey = pair.getKey();
                }
            }

            if (page.size() < PAGE_SIZE || lastKey == null) {
                break;
            }

            afterKey = lastKey;
        }
    }

    private KeyValuePair tupleToKeyValuePair(Object tupleObj) {
        if (tupleObj == null) {
            return null;
        }

        if (!(tupleObj instanceof List<?> tuple)) {
            throw new IllegalStateException("Unexpected tuple type: "
                            + tupleObj.getClass().getName());
        }

        if (tuple.isEmpty()) {
            return null;
        }

        String key = tuple.getFirst().toString();
        KeyValuePair.Builder builder = KeyValuePair.newBuilder().setKey(key);

        if (tuple.size() > 1 && tuple.get(1) != null) {
            Object rawValue = tuple.get(1);
            if (rawValue instanceof byte[] bytes) {
                builder.setValue(ByteString.copyFrom(bytes));
            } else if (rawValue instanceof String str) {
                builder.setValue(ByteString.copyFromUtf8(str));
            }
        }
        return builder.build();
    }
}
