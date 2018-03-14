package io.strimzi.devices;

import io.strimzi.devices.sensors.impl.DHT22;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class Device extends AbstractVerticle {

    private DeviceConfig config;
    private KafkaProducer<String, String> producer;
    private long timerId;
    private DHT22 dht22;

    public Device(DeviceConfig config) {
        this.config = config;
    }

    @Override
    public void start(Future<Void> start) throws Exception {

        this.dht22 = new DHT22();
        Properties dht22Config = new Properties();
        dht22Config.put(DHT22.MIN_TEMPERATURE, String.valueOf(this.config.minTemperature()));
        dht22Config.put(DHT22.MAX_TEMPERATURE, String.valueOf(this.config.maxTemperature()));
        this.dht22.init(dht22Config);

        this.producer = createProducer();

        this.timerId = vertx.setPeriodic(this.config.delay(), timerId -> {

            int temperature = this.dht22.getTemperature();

            KafkaProducerRecord<String, String> record =
                    KafkaProducerRecord.create(this.config.topicTemperature(), null, String.valueOf(temperature));

            producer.write(record);
        });

        start.complete();
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {

        this.producer.close();
        this.vertx.cancelTimer(this.timerId);

        stop.complete();
    }

    private <K, V> KafkaProducer<K, V> createProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.config.bootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "1");

        KafkaProducer<K, V> producer = KafkaProducer.create(this.vertx, config);

        return producer;
    }
}
