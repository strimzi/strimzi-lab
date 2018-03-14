package io.strimzi.devices;

import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        DeviceConfig config = DeviceConfig.fromMap(System.getenv());
        Device device = new Device(config);

        vertx.deployVerticle(device);
    }
}
