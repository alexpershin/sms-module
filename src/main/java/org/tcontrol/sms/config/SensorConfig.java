package org.tcontrol.sms.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class SensorConfig {
    private List<SensorConfiguration> sensors = new ArrayList<>();

    @Data
    @RequiredArgsConstructor
    public static class SensorConfiguration
    {
        private String id;
        private String name;
    }
}
