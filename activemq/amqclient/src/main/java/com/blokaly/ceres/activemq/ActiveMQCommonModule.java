package com.blokaly.ceres.activemq;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import javax.jms.Session;

public class ActiveMQCommonModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Session.class).toProvider(SessionProvider.class).in(Singleton.class);
  }
}
