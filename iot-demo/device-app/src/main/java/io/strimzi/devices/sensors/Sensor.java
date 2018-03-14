package io.strimzi.devices.sensors;


import java.util.Properties;

/**
 * Base interface for all the sensors
 */
public interface Sensor {

    /**
     * Sensor initialization
     *
     * @param config properties bag with sensor configuration parameters
     */
    void init(Properties config);
}
