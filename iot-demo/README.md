You can find [here](https://youtu.be/MljQnXZE1oY) a video of this demo running.

# Prerequisites

In order to run this demo, an OpenShift running cluster is needed. If you don't have it, you can use the oc tools from 
[here](https://github.com/openshift/origin/releases) running the `oc cluster up` command or MiniShift from 
[here](https://github.com/minishift/minishift) running `minishift start`.

# Deploy Kafka cluster

The needed Kafka cluster is deployed on OpenShift using the [Strimzi](http://strimzi.io/) project.

## The Cluster Controller

Download the latest Strimzi release from [here](https://github.com/strimzi/strimzi/releases) and unpack it.
As described in the official documentation, to deploy the Cluster Controller on OpenShift, the following commands 
should be executed:

```
oc create -f examples/install/cluster-controller
oc create -f examples/templates/cluster-controller
```

> NOTE : the current user needs to have the rights for creating service accounts in the cluster. The simpler way for that 
is to login as a system administrator with command `oc login -u system:admin`

## The Kafka cluster and the Topic Controller

In order to deploy a Kafka cluster, the provided template can be used. For this demo, running the `ephemeral` one is enough.
More information about that can be found on the official Strimzi documentation.
The following command will deploy such a cluster and the Topic Controller with the default configuration.

```
oc new-app strimzi-ephemeral
```

# Deploy the applications

## Create topics

This demo uses a couple of topics. The first one named `iot-temperature` is used by the device simulator for sending 
temperature values and by the stream application for getting such values and processing them. The second one is the 
`iot-temperature-max` topic where the stream application puts the max temperature value processed in the specified time 
window.
In order to create these topics in the Kafka cluster, the Topic Controller can be used. Running the following command, a 
file containing two topic ConfigMaps is deployed to the OpenShift cluster and used by the Topic Controller for creating 
such topics.

```
oc create -f ./stream-app/resources/topics.yml
```

## Start a consumer

Before running the needed application, it's useful to run a simple Kafka consumer for getting final messages from the 
`iot-temperature-max` topic. One of the Kafka brokers can be used for that, running the console consumer on it.

```
oc exec -it my-cluster-kafka-0 -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka:9092 --topic iot-temperature-max --from-beginning
```

## Deploy the stream application

The stream application uses Kafka Streams API reading from the `iot-temperature` topic, processing its values and then 
putting the max temperature value in the specified time window into the `iot-temperature-max` topic.
It's deployed running following command :

```
oc create -f ./stream-app/resources/stream-app.yml
```

## Deploy the device application

The device application provides a device simulator which sends temperature values to the `iot-temperature` topic.

```
oc create -f ./device-app/resources/device-app.yml
```
