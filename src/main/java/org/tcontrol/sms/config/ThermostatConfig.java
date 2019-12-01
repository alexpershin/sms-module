package org.tcontrol.sms.config;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;
import java.time.LocalTime;

@Data
@RequiredArgsConstructor
public class ThermostatConfig {
    private float tDay;
    private float tNight;
    private float delta;
    private int nightBegin;
    private int nightEnd;
    private String sensor;
    private String relayPin;

    @PostConstruct
    public void postConstruct(){
        nightBegin();
        nightEnd();
        relayPin();
    }

    public LocalTime nightBegin() {
        return LocalTime.ofSecondOfDay(nightBegin);
    }

    public LocalTime nightEnd(){
        return LocalTime.ofSecondOfDay(nightEnd);
    }

    public Pin relayPin(){
        return RaspiPin.getPinByName(relayPin);
    }
}
