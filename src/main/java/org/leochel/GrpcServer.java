package org.leochel;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.tarantool.client.box.TarantoolBoxClient;
import org.leochel.configs.TarantoolConfig;
import org.leochel.repositories.TarantoolRepository;
import org.leochel.services.KeyValueAccessService;

import java.util.logging.Logger;

public class GrpcServer {
    private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());

    static void main() throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("GRPC_PORT", "8090"));

        TarantoolConfig tarantoolConfig = new TarantoolConfig();
        TarantoolBoxClient tarantoolClient = tarantoolConfig.createClient();

        TarantoolRepository repository = new TarantoolRepository(tarantoolClient);
        KeyValueAccessService service = new KeyValueAccessService(repository);

        Server server = ServerBuilder.forPort(port)
                .addService(service)
                .build()
                .start();

        logger.info("server started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server");
            server.shutdown();
            try {
                tarantoolClient.close();
            } catch (Exception e) {
                logger.warning("Exception while closing Tarantool client: " + e.getMessage());
            }
            logger.info("Server stopped");
        }));

        server.awaitTermination();
    }
}
