package com.blokaly.ceres.activemq;

import com.blokaly.ceres.common.Configs;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;

@Singleton
public class SessionProvider implements Provider<Session>, ExceptionListener {
  private static Logger LOGGER = LoggerFactory.getLogger(SessionProvider.class);
  private final ActiveMQConnectionFactory connectionFactory;
  private final Boolean transact;
  private Connection connection;
  private Session session;

  @Inject
  public SessionProvider(Config config) {
    Config mqConf = config.getConfig("activemq");
    connectionFactory = new ActiveMQConnectionFactory(mqConf.getString("broker"));
    transact = Configs.getOrDefault(mqConf, "transact", Configs.BOOLEAN_EXTRACTOR, false);

  }

  @PostConstruct
  public void start() throws JMSException {
    connection = connectionFactory.createConnection();
    connection.setExceptionListener(this);
    connection.start();
    session = connection.createSession(transact, Session.AUTO_ACKNOWLEDGE);
  }

  @PreDestroy
  public void stop() {
    if (session != null) {
      try {
        session.close();
      } catch (JMSException e) {
        LOGGER.warn("JMS session closing exception", e);
      }
    }
    if (connection!=null) {
      try {
        connection.close();
      } catch (JMSException e) {
        LOGGER.warn("JMS connection closing exception", e);
      }
    }
  }

  @Override
  public Session get() {
    return session;
  }

  @Override
  public void onException(JMSException exception) {
    LOGGER.error("JMS connection exception", exception);
  }
}
