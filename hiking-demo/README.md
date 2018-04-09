# Simple CRUD Demo

A simple CRUD application using Java EE (JPA, JAX-RS, CDI etc.), based on top of WildFly and MySQL.
Used as an example for streaming changes out of a database using Debezium.

To run the app, follow these steps:

    mvn clean package
    docker build --no-cache -t debezium-examples/hike-manager:latest -f Dockerfile .

    docker run -it --rm --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=debezium -e MYSQL_USER=mysqluser -e MYSQL_PASSWORD=mysqlpw debezium/example-mysql:0.7

    docker run -it --rm -p 8080:8080 --link mysql debezium-examples/hike-manager:latest

Then visit the application in a browser at http://localhost:8080/hikr-1.0-SNAPSHOT/hikes.html.

## Deployment on OpenShift

    # Start MySQL (using Debezium's example image)
    oc new-app --name=mysql debezium/example-mysql:0.7
    oc env dc/mysql MYSQL_ROOT_PASSWORD=debezium  MYSQL_USER=mysqluser MYSQL_PASSWORD=mysqlpw

    # Build a WildFly image with the application
    oc new-app --name=myapp wildfly~https://github.com/strimzi/strimzi-lab.git \
        --context-dir=hiking-demo \
        -e MYSQL_DATABASE=inventory \
        -e MYSQL_PASSWORD=mysqlpw \
        -e MYSQL_USER=mysqluser \
        -e MYSQL_DATASOURCE=HikingDS
    oc expose svc myapp

Then visit the application in a browser at http://myapp-myproject.<OS_IP>.nip.io/hikr-1.0-SNAPSHOT/hikes.html/.
