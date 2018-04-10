package com.blokaly.ceres.common;

import com.blokaly.ceres.binding.Utils;
import com.blokaly.ceres.health.HealthChecker;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import com.netflix.governator.InjectorBuilder;
import com.netflix.governator.LifecycleInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Services {
  private static final Logger LOGGER = LoggerFactory.getLogger(Services.class);

  public static void start(Module module) {
    LifecycleInjector injector = InjectorBuilder.fromModules(module).combineWith(new ServicesModule())
        .createInjector();
    injector.getInstance(ServiceManager.class).startAsync().awaitHealthy();
  }

  private static class ServicesModule extends AbstractModule {
    @Override
    protected void configure() {
      install(new CommonModule());
      Multibinder<Service> binder = Multibinder.newSetBinder(binder(), Service.class);
      Set<Class<? extends Service>> services = Utils.getAllCeresServices();
      for (Class<? extends Service> service : services) {
        binder.addBinding().to(service);
      }
      bind(HealthCheckRegister.class).asEagerSingleton();
    }

    @Provides
    @Singleton
    private ServiceManager provideServiceManager(Set<Service> services) {
      ServiceManager manager = new ServiceManager(services);
      manager.addListener(new ServiceManager.Listener() {
                            @Override
                            public void healthy() {
                              LOGGER.info("All services started");
                            }

                            @Override
                            public void stopped() {
                              LOGGER.info("All services stopped");
                            }

                            public void failure(Service service) { System.exit(1); }
                          },
          MoreExecutors.directExecutor());
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
          manager.stopAsync().awaitStopped(5, TimeUnit.SECONDS);
        } catch (TimeoutException timeout) {
          LOGGER.error("Service manager stop timeout", timeout);
        }
      }));
      return manager;
    }

    @Singleton
    private static class HealthCheckRegister {
      @Inject
      private HealthCheckRegister(HealthCheckRegistry registry, Set<Service> services) {
        for (Service service : services) {
          if (service instanceof HealthChecker) {
            registry.register(service.getClass().getSimpleName(), ((HealthChecker) service).getChecker());
          }
        }
      }
    }
  }
}
