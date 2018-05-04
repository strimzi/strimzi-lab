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

![topics](images/topics.png)

In order to check that the topics are properly created on the Kafka cluster, it's possible to use the `kafka-topics.sh` script 
(distributed with Kafka) running it on one of the broker.

```
oc exec -it my-cluster-kafka-0 -- bin/kafka-topics.sh --zookeeper my-cluster-zookeeper:2181 --list
```

The output of the above command should be something like the following showing the created topics.

```
iot-temperature
iot-temperature-max
```

## Deploy the consumer application

The consumer application uses Kafka client in order to get messages from the `iot-temperature-max` topic and showing them 
in a Web UI.
It's deployed running following command :

```
oc create -f ./consumer-app/resources/consumer-app.yml
```

A route is provided in order to access the related Web UI.

![route](images/route.png)

![web ui](images/web_ui.png)

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

Once deployed, it starts just one pod simulating one device.

![one device gauge](images/one_device_gauge.png)

it's possible to scale up the number of pods in order to simulate more devices sending temperature values (each one with 
a different and randomly generated id).

![scale up device](images/scale_up_device.png)

Opening the consumer Web UI it's possible to see the "gauges" charts showing the processed max temperature values for all the 
active devices on the left side. The right side is useful to see the log of the incoming messages from devices, showing the 
device id alongside the max temperature value processed by the stream application for such a device.

![more device gauges](images/more_device_gauges.png)

## Clean up

If you want it could be useful to clean up the current deployment deleting all the related resources in terms of Pods, Services, Routes and Deployments.

```
oc delete all -l app=iot-demo
```

And finally the topic config maps

```
oc delete cm -l strimzi.io/kind=topic
```