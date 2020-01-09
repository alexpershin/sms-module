package org.tcontrol.sms.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class VoltageMonitorConfig {
    private List<PinConfiguration> pins = new ArrayList<>();

    @Data
    @RequiredArgsConstructor
    public static class PinConfiguration
    {
        private int id;
        private double ratio;
        private int precision;
        private String unit;
    }
}
