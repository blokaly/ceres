package com.blokaly.ceres.amqkafka;

import com.blokaly.ceres.activemq.ActiveMQCommonModule;
import com.blokaly.ceres.activemq.QueueConsumer;
import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.kafka.KafkaCommonModule;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;

public class AmqKafkaService extends AbstractService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AmqKafkaService.class);

  private final QueueConsumer consumer;

  @Inject
  public AmqKafkaService(QueueConsumer consumer) {
    this.consumer = consumer;
  }

  @Override
  protected void doStart() {
    try {
      consumer.start();
    } catch (JMSException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  protected void doStop() {
    consumer.stop();
  }


  private static class AmqKafkaModule extends AbstractModule {

    @Override
    protected void configure() {
      install(new CommonModule());
      install(new KafkaCommonModule());
      install(new ActiveMQCommonModule());

      bind(MessageListener.class).to(MessageForwarder.class).in(Singleton.class);
      bind(Service.class).to(AmqKafkaService.class);
    }

    @Provides
    @Singleton
    public QueueConsumer provideQueueConsumer(Config config, Session session, MessageListener listener) {
      String queue = config.getString("activemq.queue");
      return new QueueConsumer(session, queue, listener);
    }
  }

  public static void main(String[] args) {
    Services.start(new AmqKafkaModule());
  }
}
