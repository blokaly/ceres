1. Start zookeeper

    `$ bin/zookeeper-server-start.sh config/zookeeper.properties`

2. Start Kafka

    `$ bin/kafka-server-start.sh config/server.properties`
    

3. Create a topic
    
    `$ bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic md.bitfinex`
    
4. Start consumer
    
    `$ bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --from-beginning --formatter kafka.tools.DefaultMessageFormatter --property print.key=true --property print.value=true --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer --topic md.bitfinex`
    
## References:
http://cloudurable.com/blog/kafka-architecture/index.html
http://cloudurable.com/blog/kafka-architecture-topics/index.html
http://cloudurable.com/blog/kafka-architecture-producers/index.html   
http://cloudurable.com/blog/kafka-architecture-consumers/index.html 
http://cloudurable.com/blog/kafka-tutorial-kafka-producer/index.html
http://cloudurable.com/blog/kafka-tutorial-kafka-producer-advanced-java-examples/index.html
https://www.confluent.io/blog/stream-data-platform-2/