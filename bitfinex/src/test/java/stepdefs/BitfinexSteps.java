package stepdefs;

import com.blokaly.ceres.bitfinex.*;
import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.orderbook.OrderBasedOrderBook;
import com.blokaly.ceres.orderbook.OrderBook;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.netflix.governator.InjectorBuilder;
import com.netflix.governator.LifecycleInjector;
import com.netflix.governator.ShutdownHookModule;
import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.api.java8.En;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

        Given("^system started and connected to Bitfinex$", () -> {

            Service service = injector.getInstance(Service.class);
            try {
                service.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        When("^system received the Info message:$", (DataTable table) -> {
            HashMap<String, String> jsonMap = new HashMap<>();
            jsonMap.put("event", "info");
            jsonMap.putAll(table.asMap(String.class, String.class));
            String json = gson.toJson(jsonMap);
            BitfinexClient client = injector.getInstance(BitfinexClient.class);
            client.onMessage(json);

        });

        Then("^the Subscribe to Channel message for ([A-Z]{6}) should be sent out$", (String pair) -> {
            HashMap<String, String> jsonMap = new HashMap<>();
            jsonMap.put("event", "subscribe");
            jsonMap.put("channel", "book");
            jsonMap.put("pair", pair);
            jsonMap.put("prec", "R0");
            String json = gson.toJson(jsonMap);
            OutgoingMessageQueue messageQueue = injector.getInstance(OutgoingMessageQueue.class);
            assertThat("failed to send out message: " + json, messageQueue.messageReceived(json));
        });

        When("^system received the Snapshot message for channel (\\d+):$", (Integer channelId, DataTable table) -> {
            JsonArray snapshot = new JsonArray();
            snapshot.add(channelId);
            List<Map<String, String>> orders = table.asMaps(String.class, String.class);
            JsonArray orderArray = new JsonArray(orders.size());
            for (Map<String, String> order : orders) {
                JsonArray ord = new JsonArray(3);
                ord.add(Long.parseLong(order.get("OrderId")));
                ord.add(order.get("Price"));
                ord.add(order.get("Amount"));
                orderArray.add(ord);
            }
            snapshot.add(orderArray);
            String json = gson.toJson(snapshot).replaceAll("\"", "");
            BitfinexClient client = injector.getInstance(BitfinexClient.class);
            client.onMessage(json);
        });

        When("^system received the Subscribed message:$", (DataTable table) -> {
            HashMap<String, String> jsonMap = new HashMap<>();
            jsonMap.put("event", "subscribed");
            jsonMap.put("channel", "book");
            jsonMap.putAll(table.asMap(String.class, String.class));
            String json = gson.toJson(jsonMap);
            BitfinexClient client = injector.getInstance(BitfinexClient.class);
            client.onMessage(json);
        });

        Then("^the orderbook for channel (\\d+) should be as:$", (Integer channelId, DataTable book) -> {
            OrderBookKeeper keeper = injector.getInstance(OrderBookKeeper.class);
            OrderBasedOrderBook orderBook = keeper.get(channelId);

            HashMap<Integer, OrderBook.Level> bidLevels = new HashMap<>();
            Collection<? extends OrderBook.Level> bids = orderBook.getBids();
            int level = 0;
            for (OrderBook.Level bid : bids) {
                bidLevels.put(++level, bid);
            }

            HashMap<Integer, OrderBook.Level> askLevels = new HashMap<>();
            Collection<? extends OrderBook.Level> asks = orderBook.getAsks();
            level = 0;
            for (OrderBook.Level ask : asks) {
                askLevels.put(++level, ask);
            }

            HashMap<String, Map<Integer, OrderBook.Level>> map = new HashMap<>();
            map.put("BID", bidLevels);
            map.put("ASK", askLevels);
            List<Map<String, String>> levels = book.asMaps(String.class, String.class);
            for (Map<String, String> sideLevel : levels) {
                String[] levIdx = sideLevel.get("Level").split("_");
                Map<Integer, OrderBook.Level> levelMap = map.get(levIdx[0]);
                OrderBook.Level obLevel = levelMap.remove(Integer.valueOf(levIdx[1]));
                assertNotNull(obLevel);
                assertTrue(obLevel.getPrice().equals(DecimalNumber.fromStr(sideLevel.get("Price"))));
                assertTrue(obLevel.getQuantity().equals(DecimalNumber.fromStr(sideLevel.get("Quantity"))));
            }
            assertTrue(bidLevels.isEmpty());
            assertTrue(askLevels.isEmpty());
        });

    }

}
