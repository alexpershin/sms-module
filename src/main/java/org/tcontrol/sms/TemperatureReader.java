package org.tcontrol.sms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.dao.SensorValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class TemperatureReader {
    @Getter
    private Map<String, SensorValue> sensorValueMap = new HashMap<>();

    @Autowired
    private MonitorInterface temperatureMonitor;

    @Autowired
    private SensorConfig config;

    @Scheduled(cron = "0/30 * * * * ?")
    void inputSmsScan() {
        log.info("Reading temperature sensors...");

        int successCount = 0;
        for (SensorConfig.SensorConfiguration sensor : config.getSensors()) {
            try {
                String id = sensor.getId();
                SensorValue value = temperatureMonitor.loadValue(id);
                sensorValueMap.put(id, value);
                successCount++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.info("Reading temperature completed(read {} sensors)", successCount);
    }
}
