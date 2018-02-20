package stepdefs;

import com.blokaly.ceres.bitfinex.*;
import com.google.gson.Gson;
import com.netflix.governator.InjectorBuilder;
import com.netflix.governator.LifecycleInjector;
import com.netflix.governator.ShutdownHookModule;
import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.api.java8.En;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;

public class BitfinexSteps implements En {

    private LifecycleInjector injector;
    private final Gson gson = new Gson();

    public BitfinexSteps() {

        Before((Scenario scenario) -> {
            injector = InjectorBuilder
                    .fromModules(new ShutdownHookModule(), new BitfinexApp.BitfinexModule())
                    .overrideWith(new MockModule())
                    .createInjector();
        });

        After((Scenario scenario) -> {
            injector.close();
            try {
                injector.awaitTermination();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Given("^System started and connected to Bitfinex$", () -> {

            Service service = injector.getInstance(Service.class);
            try {
                service.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        When("^System received the following Info message:$", (DataTable table) -> {
            HashMap<String, String> jsonMap = new HashMap<>();
            jsonMap.put("event", "info");
            jsonMap.putAll(table.asMap(String.class, String.class));
            String json = gson.toJson(jsonMap);
            BitfinexClient client = injector.getInstance(BitfinexClient.class);
            client.onMessage(json);

        });

        Then("^The Subscribe to Channel message for ([A-Z]{6}) should be sent out$", (String pair) -> {
            HashMap<String, String> jsonMap = new HashMap<>();
            jsonMap.put("event", "subscribe");
            jsonMap.put("channel", "book");
            jsonMap.put("pair", pair);
            jsonMap.put("prec", "R0");
            String json = gson.toJson(jsonMap);
            OutgoingMessageQueue messageQueue = injector.getInstance(OutgoingMessageQueue.class);
            assertThat("failed to send out message: " + json, messageQueue.messageReceived(json));
        });

    }

}
