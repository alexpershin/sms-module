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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
@Slf4j
public class VoltageMonitor {

    private GpioController gpio;

    private AdcGpioProvider provider;

    GpioPinAnalogInput inputs[];

    @PostConstruct
    void init() {
        // create gpio controller
        try {
            gpio = GpioFactory.getInstance();
            provider = new MCP3008GpioProvider(SpiChannel.CS0);

            inputs = new GpioPinAnalogInput[]{
                    gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH0, "MyAnalogInput-CH0"),
                    gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH1, "MyAnalogInput-CH1"),
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
                System.out.println(">" + input.getName() + ": " + input.getValue());
            }
        }
    }
}
