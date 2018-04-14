JMXTRANS_OPTS="-Dport1=55555 -Durl1=localhost -DinfluxUrl=http://localhost:8086/ -DinfluxDb=kafka -DinfluxUser=admin -DinfluxPwd=admin" \
JAR_FILE=jmxtrans-269-all.jar \
./jmxtrans.sh start kafka.json

