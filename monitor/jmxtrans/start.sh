JMXTRANS_OPTS="-Dport1=55555 -Durl1=localhost -DinfluxUrl=http://localhost:8086/ -DinfluxDb=kafka -DinfluxUser=admin -DinfluxPwd=admin" \
JAR_FILE=jmxtrans-269-all.jar \
./jmxtrans.sh start kafka.json

KAFKA_JMX_OPTS=-Dcom.sun.management.jmxremote -javaagent:/opt/spm/spm-monitor/lib/spm-monitor-kafka.jar=YOUR_SPM_TOKEN:kafka-broker:default -Dcom.sun.management.jmxremote -javaagent:/opt/spm/spm-monitor/lib/spm-monitor-kafka.jar=YOUR_SPM_TOKEN:kafka-producer:default -Dcom.sun.management.jmxremote -javaagent:/opt/spm/spm-monitor/lib/spm-monitor-kafka.jar=YOUR_SPM_TOKEN:kafka-consumer:default -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
