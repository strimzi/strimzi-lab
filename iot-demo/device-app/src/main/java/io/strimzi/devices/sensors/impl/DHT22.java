package io.strimzi.devices.sensors.impl;

import io.strimzi.devices.sensors.HumiditySensor;
import io.strimzi.devices.sensors.TemperatureSensor;

import java.util.Properties;
import java.util.Random;

/**
 * Simulated DHT22 temperature and humidity sensor
 * @see <a href="https://learn.adafruit.com/dht/overview">DHTxx family</a>
 */
public class DHT22 implements TemperatureSensor, HumiditySensor {

    public static final String MIN_TEMPERATURE = "min";
    public static final String MAX_TEMPERATURE = "max";

    private int min;
    private int max;
    private Random random = new Random();

    @Override
    public int getHumidity() {
        return 0;
    }

    @Override
    public int getTemperature() {
        int temp = this.min + random.nextInt(this.max - this.min);
        return temp;
    }

    @Override
    public void init(Properties config) {
        this.min = Integer.valueOf(config.getProperty(DHT22.MIN_TEMPERATURE));
        this.max = Integer.valueOf(config.getProperty(DHT22.MAX_TEMPERATURE));
    }
}
