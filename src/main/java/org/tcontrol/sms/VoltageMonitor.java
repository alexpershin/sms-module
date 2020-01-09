package org.tcontrol.sms;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Examples
 * FILENAME      :  MCP3008GpioExample.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2019 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.pi4j.gpio.extension.base.AdcGpioProvider;
import com.pi4j.gpio.extension.mcp.MCP3008GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP3008Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.spi.SpiChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.config.SensorConfig;
import org.tcontrol.sms.config.VoltageMonitorConfig;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;


@Component
@Slf4j
public class VoltageMonitor {

    public static final int DEFAULT_PRECISION = 3;
    private GpioController gpio;

    private AdcGpioProvider provider;

    GpioPinAnalogInput inputs[];

    public VoltageMonitor(VoltageMonitorConfig voltageMonitorConfig) {
        this.voltageMonitorConfig = voltageMonitorConfig;
    }

    private VoltageMonitorConfig voltageMonitorConfig;

    @PostConstruct
    void init() {
        // create gpio controller
        try {
            gpio = GpioFactory.getInstance();
            provider = new MCP3008GpioProvider(SpiChannel.CS0);

            inputs = new GpioPinAnalogInput[]{
                    gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH0, "battery-voltage"),
                    gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH1, "power-voltage"),
                    gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH2, "MyAnalogInput-CH2"),
                    gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH3, "MyAnalogInput-CH3"),
                    gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH4, "MyAnalogInput-CH4"),
                    gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH5, "MyAnalogInput-CH5"),
                    gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH6, "MyAnalogInput-CH6"),
                    gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH7, "MyAnalogInput-CH7")
            };
            provider.setEventThreshold(100, inputs);
            provider.setMonitorInterval(250);

            GpioPinListenerAnalog listener = new GpioPinListenerAnalog() {
                @Override
                public void handleGpioPinAnalogValueChangeEvent(GpioPinAnalogValueChangeEvent event) {
                    // get RAW value
                    double value = event.getValue();

                    // display output
                    System.out.println("<CHANGED VALUE> [" + event.getPin().getName() + "] : RAW VALUE = " + value);
                }
            };

            // Register the gpio analog input listener for all input pins
            gpio.addListener(listener, inputs);
        } catch (Throwable e) {
            log.error(e.getMessage());
        }
    }

    @Scheduled(cron = "${voltage-monitor.schedule}")
    public void monitor() throws Exception {

        System.out.println("MCP3008 ADC:");
        if (provider != null) {
            // Print current analog input conversion values from each input channel
            for (GpioPinAnalogInput input : inputs) {
                Optional<VoltageMonitorConfig.PinConfiguration> pin =
                        voltageMonitorConfig.getPins().stream()
                                .filter(p -> p.getId() == input.getPin().getAddress()).findFirst();
                double ratio = pin.isPresent() ? pin.get().getRatio() : 1.0;
                String unit = pin.isPresent() ? pin.get().getUnit() : "";
                double value = input.getValue() * ratio;
                BigDecimal decimal = new BigDecimal(value);
                value = decimal.setScale(pin.isPresent() ? pin.get().getPrecision(): DEFAULT_PRECISION,
                        RoundingMode.HALF_UP).doubleValue();
                log.info(">" + input.getName() + ": " + value + " " + unit);
            }
        }
    }
}
