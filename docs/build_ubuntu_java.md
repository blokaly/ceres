`$ sudo systemctl enable docker`

`$ sudo systemctl start docker`

`$ docker pull ubuntu:16.04`

`$ docker run -i -t ubuntu:16.04 /bin/bash`

docker image connected:

`# apt-get update`

`# apt-get install software-properties-common`

`# add-apt-repository -y ppa:webupd8team/java`

`# apt-get update`

`# apt-get install -y oracle-java8-installer`

`# apt-get autoremove; apt-get clean; apt-get autoclean`

`# rm -rf /var/lib/apt/lists/*`

`# rm -rf /var/cache/oracle-jdk8-installer`

ctrl-d to quit docker image

`$ docker ps -a` (check the *CONTAINER ID*)

`$ docker commit <container_id> blokaly/java8`

`$ docker push blokaly/java8` (need docker hub login first: `$ docker login --username=xxx`)