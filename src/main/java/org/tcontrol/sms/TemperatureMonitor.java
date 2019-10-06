package org.tcontrol.sms;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.config.SensorConfig;
import org.tcontrol.sms.dao.SensorValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class TemperatureMonitor implements ITemperatureMonitor {
    private Map<String, SensorValue> sensorValueMap = new HashMap<>();

    @Autowired
    private ITemperatureReader temperatureReader;

    @Autowired
    private SensorConfig config;

    @Scheduled(cron = "0/30 * * * * ?")
    void readSensors() {
        log.info("Reading temperature sensors...");

        int successCount = 0;
        for (SensorConfig.SensorConfiguration sensor : config.getSensors()) {
            try {
                String id = sensor.getId();
                SensorValue value = temperatureReader.loadValue(id);
                value.setSensorId(id);
                sensorValueMap.put(id, value);
                successCount++;
            } catch (IOException e) {
                log.warn(e.getMessage());
            }
        }
        log.info("Reading temperature completed(read {} sensors)", successCount);
    }

    @Override
    public Map<String, SensorValue> getSensorValueMap() {
        return sensorValueMap;
    }
}
