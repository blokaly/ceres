- Stop all containers:

    `> docker stop $(docker ps -a -q)`

- Remove all containers:

    `> docker rm $(docker ps -a -q)`

- List all images:

    `> docker imamges`

- Remove image:

    `> docker rmi <image_id>`

- Create a volume:

    `> docker volume create --driver local --opt device=/opt/data/docker/logs/<name> <name>`

- Remove a volume:

    `> docker volume rm <name>`
    
- Run an image and create a container using host network:

    `> docker run -d --name ceres-gdaxfh --net="host" -v /opt/logs/gdax:/gdaxfh/logs blokaly/ceres-gdaxfh:1.0`

- Run an image and create a container with env variables:

    `> docker run -d --name ceres-gdaxfh -e KAFKA_BOOTSTRAP_SERVERS='10.208.0.120:9092' -v /opt/logs/gdax:/gdaxfh/logs blokaly/ceres-gdaxfh:1.0`    
    
- Remove a container
    
    `> docker rm <name>`
    
- Star/Stop a container    
    
    `> docker start/stop <name>`
    
- Connect to a running container (if bash supported)

    `> docker exec -it <name> bash`   
    
- Connect to an image
    
    `> docker run -i -t <image> /bin/bash`
    
- Remove all unused images
    
    `> docker rmi $(docker images -a|grep "<none>"|awk '$2=="<none>" {print $3}')`
    
- upgrade an image with the container

    `> docker stop <container>`    
         
    `> docker rm <container>` 
    
    `> docker pull <image>` 
           
    `> docker rmi $(docker images -a|grep "<none>"|awk '$2=="<none>" {print $3}')`
    
    `> docker run -d --name <container> -e <env config> -v <volume config> <image>`
