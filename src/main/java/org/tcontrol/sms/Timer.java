package org.tcontrol.sms;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.config.ThermostatConfig;

import java.time.*;

@Component
@AllArgsConstructor
public class Timer implements ITimer {
    private ThermostatConfig thermostatConfig;

    @Override
    public LocalDateTime getCurrentTime(){
        return LocalDateTime.now();
    }

}
