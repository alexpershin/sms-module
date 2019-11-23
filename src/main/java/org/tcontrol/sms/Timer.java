package org.tcontrol.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.config.TermostatConfig;

import java.time.*;

@Component
public class Timer implements ITimer {
    @Autowired
    private TermostatConfig termostatConfig;

    @Override
    public LocalDateTime getCurrentTime(){
        return LocalDateTime.now();
    }

}
