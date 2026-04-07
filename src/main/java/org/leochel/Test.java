package org.leochel;

import org.leochel.configs.TarantoolConfig;

import java.util.Arrays;

public class Test {
    static void main() throws Exception {
        TarantoolConfig config = new TarantoolConfig();
        try (var client = config.createClient();) {
            var space = client.space("KV");
            byte[] testString = "test".getBytes();
            space.replace(Arrays.asList("Test", testString)).join();
            var result = space.select("Test").join();
            var tuple = result.get().getFirst().get();
            var key = tuple.getFirst();
            var value = tuple.get(1);


            System.out.println(key + " " + new String((byte[]) value));
        }
    }
}
