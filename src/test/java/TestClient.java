import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.leochel.*;

void main() {
    ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 8080)
            .usePlaintext()
            .build();

    KeyValueAccessServiceGrpc.KeyValueAccessServiceBlockingStub stub =
            KeyValueAccessServiceGrpc.newBlockingStub(channel);

    try {
        IO.println("PUT");
        stub.put(KeyValuePair.newBuilder()
                .setKey("key_a")
                .setValue(ByteString.copyFromUtf8("value_a"))
                .build());
        stub.put(KeyValuePair.newBuilder()
                .setKey("key_b")
                .setValue(ByteString.copyFromUtf8("value_b"))
                .build());
        stub.put(KeyValuePair.newBuilder()
                .setKey("key_c")
                .setValue(ByteString.copyFromUtf8("value_c"))
                .build());
        IO.println("Put 3 keys");

        IO.println("PUT NULL");
        stub.put(KeyValuePair.newBuilder()
                .setKey("key_null")
                .build());
        IO.println("Put key_null with no value");

        IO.println("GET");
        KeyValuePair result = stub.get(Key.newBuilder().setKey("key_b").build());
        IO.println("key_b -> " + result.getValue().toString(StandardCharsets.UTF_8));

        IO.println("GET NULL VALUE");
        result = stub.get(Key.newBuilder().setKey("key_null").build());
        IO.println("key_null hasValue: " + result.hasValue());

        IO.println("GET NOT FOUND");
        try {
            stub.get(Key.newBuilder().setKey("nonexistent").build());
            IO.println("ERROR: should have thrown");
        } catch (StatusRuntimeException e) {
            IO.println("Correctly got: " + e.getStatus().getCode()
                    + " " + e.getStatus().getDescription());
        }

        IO.println("COUNT");
        CountResponse count = stub.count(Empty.getDefaultInstance());
        IO.println("Count: " + count.getRowCount());

        IO.println("DELETE");
        stub.delete(Key.newBuilder().setKey("key_null").build());
        count = stub.count(Empty.getDefaultInstance());
        IO.println("After delete key_null, count: " + count.getRowCount());

        IO.println("RANGE [key_a, key_c)");
        Iterator<KeyValuePair> rangeIter = stub.range(KeyRange.newBuilder()
                .setKeySince("key_a")
                .setKeyTo("key_c")
                .build());
        while (rangeIter.hasNext()) {
            KeyValuePair pair = rangeIter.next();
            String v = pair.hasValue()
                    ? pair.getValue().toString(StandardCharsets.UTF_8)
                    : "NULL";
            IO.println("  " + pair.getKey() + " -> " + v);
        }

        stub.delete(Key.newBuilder().setKey("key_a").build());
        stub.delete(Key.newBuilder().setKey("key_b").build());
        stub.delete(Key.newBuilder().setKey("key_c").build());

        IO.println("RANGE PAGINATION TEST");
        for (int i = 0; i < 2500; i++) {
            stub.put(KeyValuePair.newBuilder()
                    .setKey(String.format("range_%05d", i))
                    .setValue(ByteString.copyFromUtf8("val_" + i))
                    .build());
        }
        IO.println("Inserted 2500 records");

        int[] counter = {0};
        Iterator<KeyValuePair> pageIter = stub.range(KeyRange.newBuilder()
                .setKeySince("range_00000")
                .setKeyTo("range_02500")
                .build());
        while (pageIter.hasNext()) {
            pageIter.next();
            counter[0]++;
        }
        IO.println("Range got: " + counter[0] + " records (expected 2500)");

        IO.println("CLEANUP");
        for (int i = 0; i < 2500; i++) {
            stub.delete(Key.newBuilder().setKey(String.format("range_%05d", i)).build());
        }
        count = stub.count(Empty.getDefaultInstance());
        IO.println("Count after cleanup: " + count.getRowCount());

    } catch (StatusRuntimeException e) {
        System.err.println("RPC failed: " + e.getStatus().getCode()
                + " " + e.getStatus().getDescription());
    } finally {
        channel.shutdown();
    }
}
