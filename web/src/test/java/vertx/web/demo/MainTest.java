package vertx.web.demo;

import guru.nidi.ramltester.*;
import guru.nidi.ramltester.jaxrs.CheckingWebTarget;
import guru.nidi.ramltester.junit.RamlMatchers;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.*;
import org.jboss.resteasy.client.jaxrs.*;
import org.junit.*;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class MainTest {
    private static final RamlDefinition api = RamlLoaders.fromClasspath()
            .load("/webroot/api/utx.raml")
            .assumingBaseUri("http://localhost:8000");

    private ResteasyClient client = new ResteasyClientBuilder().build();
    private CheckingWebTarget checking;

    private static final Vertx vertx = Vertx.vertx();

    @BeforeClass
    public static void bootApp() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        vertx.deployVerticle(Main.class.getName(), res -> {
            latch.countDown();
        });

        latch.await();
    }

    @Before
    public void createTarget() {
        checking = api.createWebTarget(
                client.target("http://localhost:8000")
        );

        // mock the eventbus service
        // will need to use/change to dependency injection to test at real time the implementation
        vertx.eventBus().consumer("javazone.storage.find", msg -> {
            msg.reply(new JsonArray());
        });
    }

    @Test
    public void testHelloEndpoint() {
        checking.path("/utx/10/0").request().get();
        assertThat(checking.getLastReport(),
                RamlMatchers.hasNoViolations());
    }
}