package vertx.web.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.*;
import io.vertx.core.json.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;

public class Main extends AbstractVerticle {

    @Override
    public void start() {
        final EventBus eb = vertx.eventBus();
        final Router router = Router.router(vertx);

        router.route().handler(LoggerHandler.create());

        router.route().handler(ctx -> {
            ctx.response().end("Hello World!");
        });

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8000);
    }
}
