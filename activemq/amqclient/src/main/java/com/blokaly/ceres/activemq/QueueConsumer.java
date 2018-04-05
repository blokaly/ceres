package com.blokaly.ceres.activemq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class QueueConsumer {
  private static Logger LOGGER = LoggerFactory.getLogger(QueueConsumer.class);
  private final Session session;
  private final String queue;
  private final MessageListener listener;
  private volatile boolean stop;
  private MessageConsumer consumer;

  public QueueConsumer(Session session, String queue, MessageListener listener) {
    this.session = session;
    this.queue = queue;
    this.listener = listener;
    stop = false;
  }

  public void start() throws JMSException {
    Destination destination = session.createQueue(queue);
    consumer = session.createConsumer(destination);
    while (!stop) {
      try {
        Message msg = consumer.receive();
        listener.onMessage(msg);
      } catch (Exception ex) {
        LOGGER.error("Error processing JMS message", ex);
      }
    }
  }

  public void stop() {
    stop = true;
    if (consumer != null) {
      try {
        consumer.close();
      } catch (JMSException e) {
        LOGGER.warn("Failed to close consumer", e);
      }
    }
  }
}
