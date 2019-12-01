package org.tcontrol.sms;

import org.springframework.scheduling.annotation.Scheduled;

public interface IThermostat {
    @Scheduled(cron = "${thermostat.schedule}")
    void checkTemperature();

    boolean isHeatingOn();

    boolean isOn();

    float getMediumT();

    void changeOn(boolean v);
}
