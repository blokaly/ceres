package stepdefs;

import com.blokaly.ceres.bitfinex.BitfinexApp;
import com.blokaly.ceres.bitfinex.MockModule;
import com.blokaly.ceres.bitfinex.Service;
import com.netflix.governator.InjectorBuilder;
import com.netflix.governator.LifecycleInjector;
import com.netflix.governator.ShutdownHookModule;
import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.api.java8.En;
import gherkin.formatter.model.DataTableRow;

import java.util.List;

public class BitfinexSteps implements En {

    public BitfinexSteps() {
        Before((Scenario scenario) -> {
            LifecycleInjector injector = InjectorBuilder
                    .fromModules(new ShutdownHookModule(), new BitfinexApp.BitfinexModule())
                    .overrideWith(new MockModule())
                    .createInjector();
            Service service = injector.getInstance(Service.class);
            try {
                service.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        After((Scenario scenario) -> {});

        Given("^Open http://en.wikipedia.org$", () -> {});

        Given("^Do login$", () -> {});

        Given("^a web browser is on the Google page$", () -> {

        });

        When("^the search phrase \"([^\"]*)\" is entered$", (String phrase) -> {

        });

        Then("^results for \"([^\"]*)\" are shown$", (String phrase) -> {

        });

        When("User enters Credentials to LogIn", (DataTable table) -> {
            List<DataTableRow> rows = table.getGherkinRows();
            for (DataTableRow row : rows) {
                System.out.println(row);
            }
        });
    }
}
