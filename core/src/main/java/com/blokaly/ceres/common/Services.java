package com.blokaly.ceres.common;

import com.blokaly.ceres.binding.Utils;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.netflix.governator.InjectorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Services {
  private static final Logger LOGGER = LoggerFactory.getLogger(Services.class);

  public static void start(Module module) {
    InjectorBuilder.fromModules(module).combineWith(new ServicesModule())
        .createInjector()
        .getInstance(ServiceManager.class)
        .startAsync().awaitHealthy();
  }

  private static class ServicesModule extends AbstractModule {
    @Override
    protected void configure() {
      Multibinder<Service> binder = Multibinder.newSetBinder(binder(), Service.class);
      Set<Class<? extends Service>> services = Utils.getAllCeresServices();
      for (Class<? extends Service> service : services) {
        binder.addBinding().to(service);
      }
    }

    @Provides
    @Singleton
    public ServiceManager provideServiceManager(Set<Service> services) {
      ServiceManager manager = new ServiceManager(services);
      manager.addListener(new ServiceManager.Listener() {
                            public void failure(Service service) {
                              System.exit(1);
                            }
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
  }
}
