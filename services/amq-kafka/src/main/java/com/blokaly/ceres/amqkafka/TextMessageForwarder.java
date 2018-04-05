package com.blokaly.ceres.amqkafka;

import com.blokaly.ceres.kafka.TextProducer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.activemq.command.ActiveMQDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Singleton
public class TextMessageForwarder implements MessageListener {

  private static Logger LOGGER = LoggerFactory.getLogger(TextMessageForwarder.class);
  private final TextProducer producer;

  @Inject
  public TextMessageForwarder(TextProducer producer) {
    this.producer = producer;
  }

  @Override
  public void onMessage(Message message) {
    LOGGER.info("mq message: {}", message);
    if (message instanceof TextMessage) {
      TextMessage text = (TextMessage) message;
      try {
        ActiveMQDestination destination = ActiveMQDestination.transform(text.getJMSDestination());
        producer.publish(destination.getPhysicalName(), text.getText());
      } catch (Exception e) {
       LOGGER.error("Error forwarding AMQ text message", e);
      }
    }
  }
}
