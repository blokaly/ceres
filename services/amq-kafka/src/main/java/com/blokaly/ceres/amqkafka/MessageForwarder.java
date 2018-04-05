package com.blokaly.ceres.amqkafka;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;
import javax.jms.MessageListener;

@Singleton
public class MessageForwarder implements MessageListener {

  private static Logger LOGGER = LoggerFactory.getLogger(MessageForwarder.class);

  @Override
  public void onMessage(Message message) {
    LOGGER.info("mq message: {}", message);

  }
}
