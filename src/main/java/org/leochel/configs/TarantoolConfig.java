package org.leochel.configs;


import io.tarantool.client.box.TarantoolBoxClient;
import io.tarantool.client.factory.TarantoolBoxClientBuilder;
import io.tarantool.client.factory.TarantoolFactory;
import io.tarantool.pool.InstanceConnectionGroup;

import java.util.Collections;
import java.util.logging.Logger;

public class TarantoolConfig {
    private static final Logger logger = Logger.getLogger(TarantoolConfig.class.getName());
    private final String host;
    private final int port;
    private final String user;
    private final String password;

    public TarantoolConfig() {
        this.host = env("TARANTOOL_HOST", "localhost");
        this.port = Integer.parseInt(env("TARANTOOL_PORT", "3301"));
        this.user = env("TARANTOOL_USERNAME", "test_user");
        this.password = env("TARANTOOL_PASSWORD", "test_password");
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    public TarantoolBoxClient createClient() throws Exception {
        System.out.println(this.host + " " + port + " " + user + " " + password);
        InstanceConnectionGroup connectionGroup = InstanceConnectionGroup.builder()
                .withHost(host)
                .withPort(port)
                .withUser(user)
                .withPassword(password)
                .build();
        TarantoolBoxClientBuilder builder = TarantoolFactory.box()
                .withGroups(Collections.singletonList(connectionGroup));

        return builder.build();
    }
}
