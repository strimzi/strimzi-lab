package io.strimzi.devices;

import java.util.Map;

public class DeviceConfig {

    private static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final int DEFAULT_DELAY = 1000;
    private static final int DEFAULT_MIN_TEMPERATURE = 15;
    private static final int DEFAULT_MAX_TEMPERATURE = 25;
    private static final String DEFAULT_TOPIC_TEMPERATURE = "iot-temperature";

    private static final String BOOTSTRAP_SERVERS = "BOOTSTRAP_SERVERS";
    private static final String DELAY = "DELAY";
    private static final String MIN_TEMPERATURE = "MIN_TEMPERATURE";
    private static final String MAX_TEMPERATURE = "MAX_TEMPERATURE";
    private static final String TOPIC_TEMPERATURE = "TOPIC_TEMPERATURE";

    private final String bootstrapServers;
    private final long delay;
    private final int minTemperature;
    private final int maxTemperature;
    private final String topicTemperature;

    public DeviceConfig(String bootstrapServers, long delay, int minTemperature, int maxTemperature, String topicTemperature) {
        this.bootstrapServers = bootstrapServers;
        this.delay = delay;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.topicTemperature = topicTemperature;
    }

    /**
     * Loads device configuration from a related map
     *
     * @param map map from which loading configuration parameters
     * @return Device configuration instance
     */
    public static DeviceConfig fromMap(Map<String, String> map) {

        String bootstrapServers = map.getOrDefault(DeviceConfig.BOOTSTRAP_SERVERS, DEFAULT_BOOTSTRAP_SERVERS);
        long delay = Long.parseLong(map.getOrDefault(DeviceConfig.DELAY, String.valueOf(DEFAULT_DELAY)));
        int minTemperature = Integer.parseInt(map.getOrDefault(DeviceConfig.MIN_TEMPERATURE, String.valueOf(DEFAULT_MIN_TEMPERATURE)));
        int maxTemperature = Integer.parseInt(map.getOrDefault(DeviceConfig.MAX_TEMPERATURE, String.valueOf(DEFAULT_MAX_TEMPERATURE)));
        String topicTemperature = map.getOrDefault(DeviceConfig.TOPIC_TEMPERATURE, DEFAULT_TOPIC_TEMPERATURE);

        return new DeviceConfig(bootstrapServers, delay, minTemperature, maxTemperature, topicTemperature);
    }

    public String bootstrapServers() {
        return this.bootstrapServers;
    }

    public long delay() {
        return this.delay;
    }

    public int minTemperature() {
        return this.minTemperature;
    }

    public int maxTemperature() {
        return this.maxTemperature;
    }

    public String topicTemperature() {
        return this.topicTemperature;
    }

    @Override
    public String toString() {
        return "DeviceConfig(" +
                "bootstrapServers=" + bootstrapServers +
                "delay=" + delay +
                "minTemperature=" + minTemperature +
                "maxTemperature=" + maxTemperature +
                "topicTemperature=" + topicTemperature +
                ")";
    }
}
