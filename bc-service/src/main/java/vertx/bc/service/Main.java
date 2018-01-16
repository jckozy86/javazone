package vertx.bc.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.*;
import io.vertx.core.logging.*;

public class Main extends AbstractVerticle {

    private final Logger LOG = LoggerFactory.getLogger(Main.class);

    @Override
    public void start() {
        final EventBus eb = vertx.eventBus();
        final BlockchainClient client = new BlockchainClient(vertx);

        client.exceptionHandler(e -> LOG.error("ERROR", e));

        client.connect("wss://ws.blockchain.info/inv", v-> {
            client.subscribeUnconfirmed(json -> {
                LOG.info("RECEIVED UNCONFIRMED");
                eb.send("javazone.storage.save", json);
            });
        });
    }
}