`$ docker network create influxdb`

`$ docker run -d --name=influxdb --net=influxdb influxdb`

`$ docker run -p 8888:8888 --net=influxdb chronograf --influxdb-url=http://influxdb:8086`

- start-kafka.sh

`export JMX_PORT=${JMX_PORT:-9999}`

- start kafkamon docker

`docker run -i -t --net=influxdb blokaly/ceres-kafkamon /bin/bash`

`export JMXTRANS_OPTS="-Dport1=55555 -Durl1=10.208.0.120 -DinfluxUrl=http://influxdb:8086/ -DinfluxDb=kafka -DinfluxUser=admin -DinfluxPwd=admin"`



- Install Docker CE on CentOS
```
$ sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-selinux \
                  docker-engine-selinux \
                  docker-engine
```

```                  
$ sudo yum install -y yum-utils \
  device-mapper-persistent-data \
  lvm2
```

```  
$ sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo      
```   
    
`$ sudo yum-config-manager --enable docker-ce-edge`     

`$ sudo yum install docker-ce`

`$ sudo systemctl start docker`      

- Install Portainer

`$ docker volume create portainer_data`

`$ docker run -d -p 9000:9000 -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer`


## Setup Kafka Monitoring

docker network create monitoring-network
      
docker run -d -p 8083:8083 -p 8086:8086 --net=monitoring-network --name=influxdb influxdb

docker run -d -p 8888:8888 --net=monitoring-network --name=chronograf chronograf --influxdb-url=http://influxdb:8086

use Chronograf to create a database 'kafka'

docker run -d -p 3000:3000 --net=monitoring-network --name=grafana grafana/grafana

create grafana data source:

{"name":"influx","type":"influxdb","url":"http://influxdb:8086",
"access":"proxy","isDefault":true,"database":"kafka","user":"admin","password":"admin"}

`JMXTRANS_OPTS="-Dport1=55555 -Durl1=10.208.0.120 -DinfluxUrl=http://influxdb:8086/ -DinfluxDb=kafka -DinfluxUser=admin -DinfluxPwd=admin"`
     