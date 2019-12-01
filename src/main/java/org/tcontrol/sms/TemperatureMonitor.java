package org.tcontrol.sms;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.config.SensorConfig;
import org.tcontrol.sms.dao.SensorValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@AllArgsConstructor
public class TemperatureMonitor implements ITemperatureMonitor {
    final private Map<String, SensorValue> sensorValueMap = new HashMap<>();

    private ITemperatureReader temperatureReader;

    private SensorConfig config;

    private CSVStatisticsWriter csvWriter;

    @Scheduled(cron = "${sensor-config.schedule}")
    public void readSensors() {
        log.info("Reading temperature sensors...");

        int successCount = 0;
        List<SensorConfig.SensorConfiguration> sensors = config.getSensors();
        SensorValue values[] = new SensorValue[sensors.size()];
        for (SensorConfig.SensorConfiguration sensor : sensors) {
            try {
                String id = sensor.getId();
                SensorValue value = temperatureReader.loadValue(id);
                value.setSensorId(id);
                sensorValueMap.put(id, value);
                values[successCount] = value;
                successCount++;
            } catch (IOException e) {
                log.warn(e.getMessage());
            }
        }
        csvWriter.write(values);
        log.info("Reading temperature completed(read {} sensors)", successCount);
    }

    @Override
    public Map<String, SensorValue> getSensorValueMap() {
        return sensorValueMap;
    }
}
