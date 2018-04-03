/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.strimzi.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Reducer;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.internals.WindowedDeserializer;
import org.apache.kafka.streams.kstream.internals.WindowedSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates, using the high-level KStream DSL, how to implement an IoT demo application
 * which ingests temperature value processing the maximum value in the latest TEMPERATURE_WINDOW_SIZE seconds (which
 * is 5 seconds) sending a new message if it exceeds the TEMPERATURE_THRESHOLD (which is 20)
 *
 * In this example, the input stream reads from a topic named "iot-temperature", where the values of messages
 * represent temperature values; using a TEMPERATURE_WINDOW_SIZE seconds "tumbling" window, the maximum value is processed and
 * sent to a topic named "iot-temperature-max" if it exceeds the TEMPERATURE_THRESHOLD.
 *
 * Before running this example you must create the input topic for temperature values in the following way :
 *
 * bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic iot-temperature
 *
 * and at same time the output topic for filtered values :
 *
 * bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic iot-temperature-max
 *
 * After that, a console consumer can be started in order to read filtered values from the "iot-temperature-max" topic :
 *
 * bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic iot-temperature-max --from-beginning
 *
 * On the other side, a console producer can be used for sending temperature values (which needs to be integers)
 * to "iot-temperature" typing them on the console :
 *
 * bin/kafka-console-producer.sh --broker-list localhost:9092 --topic iot-temperature
 * > 10
 * > 15
 * > 22
 */
public class TemperatureDemo {

    private static final Logger log = LoggerFactory.getLogger(TemperatureDemo.class);

    private static final String BOOTSTRAP_SERVERS = "BOOTSTRAP_SERVERS";
    private static final String TOPIC_TEMPERATURE = "TOPIC_TEMPERATURE";
    private static final String TOPIC_TEMPERATURE_MAX = "TOPIC_TEMPERATURE_MAX";
    private static final String TEMPERATURE_THRESHOLD = "TEMPERATURE_THRESHOLD";
    private static final String TEMPERATURE_WINDOW_SIZE = "TEMPERATURE_WINDOW_SIZE";

    private static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String DEFAULT_TOPIC_TEMPERATURE = "iot-temperature";
    private static final String DEFAULT_TOPIC_TEMPERATURE_MAX = "iot-temperature-max";
    // threshold used for filtering max temperature values
    private static final int DEFAULT_TEMPERATURE_THRESHOLD = 20;
    // window size within which the filtering is applied
    private static final int DEFAULT_TEMPERATURE_WINDOW_SIZE = 5;

    public static void main(String[] args) throws Exception {

        new TemperatureDemo().run();
    }

    public void run() {

        String bootstrapServers = System.getenv().getOrDefault(BOOTSTRAP_SERVERS, DEFAULT_BOOTSTRAP_SERVERS);
        String topicTemperature = System.getenv().getOrDefault(TOPIC_TEMPERATURE, DEFAULT_TOPIC_TEMPERATURE);
        String topicTemperatureMax = System.getenv().getOrDefault(TOPIC_TEMPERATURE_MAX, DEFAULT_TOPIC_TEMPERATURE_MAX);
        int temperatureThreshold = Integer.parseInt(System.getenv().getOrDefault(TEMPERATURE_THRESHOLD, String.valueOf(DEFAULT_TEMPERATURE_THRESHOLD)));
        int temperatureWindowSize = Integer.parseInt(System.getenv().getOrDefault(TEMPERATURE_WINDOW_SIZE, String.valueOf(DEFAULT_TEMPERATURE_WINDOW_SIZE)));

        log.info("Started with config: bootstrapServers={}, topicTemperature={}, topicTemperatureMax={}, temperatureThreshold={}, temperatureWindowSize={}",
                bootstrapServers, topicTemperature, topicTemperatureMax, temperatureThreshold, temperatureWindowSize);

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-temperature");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> source = builder.stream(topicTemperature);

        KStream<Windowed<String>, String> max = source
                .groupByKey()
                .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(temperatureWindowSize)))
                .reduce(new Reducer<String>() {
                    @Override
                    public String apply(String value1, String value2) {
                        if (Integer.parseInt(value1) > Integer.parseInt(value2))
                            return value1;
                        else
                            return value2;
                    }
                })
                .toStream()
                .filter(new Predicate<Windowed<String>, String>() {
                    @Override
                    public boolean test(Windowed<String> key, String value) {
                        return Integer.parseInt(value) > temperatureThreshold;
                    }
                });

        /*
        WindowedSerializer<String> windowedSerializer = new WindowedSerializer<>(Serdes.String().serializer());
        WindowedDeserializer<String> windowedDeserializer = new WindowedDeserializer<>(Serdes.String().deserializer(), temperatureWindowSize);
        Serde<Windowed<String>> windowedSerde = Serdes.serdeFrom(windowedSerializer, windowedDeserializer);

        // need to override key serde to Windowed<String> type
        max.to(topicTemperatureMax, Produced.with(windowedSerde, Serdes.String()));
        */

        // remapping to a key with user/device-id and removing the window start/end part
        KStream<String, String> max2 = max.map(new KeyValueMapper<Windowed<String>, String, KeyValue<String, String>>() {
            @Override
            public KeyValue<String, String> apply(Windowed<String> stringWindowed, String s) {
                return new KeyValue<>(stringWindowed.key(), s);
            }
        });

        max2.to(topicTemperatureMax);

        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-temperature-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
