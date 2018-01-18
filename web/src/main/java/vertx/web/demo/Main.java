package vertx.web.demo;

import io.prometheus.client.Counter;
import io.prometheus.client.hotspot.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.*;
import io.vertx.core.json.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class Main extends AbstractVerticle {

    @Override
    public void start() {

        //this is for Prometheus monitoring, API call 
    		DefaultExports.initialize();
        //The below is a prometheus counter.
        //for every call, we increase counter
        final Counter counter = Counter.build()
                .name("api_calls").help("api_calls").register();

        final EventBus eb = vertx.eventBus();
        final Router router = Router.router(vertx);
        
        //allow web client to connect to event bus
        //this is for the webpack, js, code
        BridgeOptions opts = new BridgeOptions()
        		.addOutboundPermitted(
        				new PermittedOptions().setAddress("javazone.data.updates")
        				);
        
        router.route("/eventbus/*").handler(
        		SockJSHandler.create(vertx).bridge(opts));
        
        
        router.route().handler(LoggerHandler.create());

        
        /*
        //commenting out as now we will add the Junit testing side
        //this is getting replaced by router.get(***)
        router.route().handler(ctx -> {
            ctx.response().end("Hello World!");
        });
        */

        router.get("/utx/:skip/:limit").handler(ctx -> {
        	//incrementing counter for monitoring using prometheus
        	//comment out if prometheus is not used
        	counter.inc();
            try {
                JsonObject options = new JsonObject()
                        .put("skip", Integer.parseInt(ctx.request().getParam("skip")))
                        .put("limit", Integer.parseInt(ctx.request().getParam("limit")));

                eb.send("javazone.storage.find", options, send -> {
                    if (send.failed()) {
                        ctx.fail(send.cause());
                    } else {
                        ctx.response()
                                .putHeader("content-type", "application/json")
                                .end(((JsonArray) send.result().body()).encode());
                    }
                });
            } catch (NumberFormatException e) {
                ctx.fail(e);
            }
        });

        router.route().handler(StaticHandler.create());

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8000);
    }
}
