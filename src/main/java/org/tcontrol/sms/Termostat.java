package org.tcontrol.sms;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.commands.CommandExecutor;
import org.tcontrol.sms.config.TermostatConfig;
import org.tcontrol.sms.dao.SensorValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@Slf4j
public class Termostat {
    @Autowired
    private TermostatConfig termostatConfig;

    @Autowired
    private ITemperatureMonitor temperatureMonitor;

    @Autowired IRelayController relayController;

    @Autowired
    private ITimer timer;

    @Getter
    boolean on;


    @Scheduled(cron = "${termostat.schedule}")
    public void checkTemperature() {

        float mediana = night() ? termostatConfig.getTNight() : termostatConfig.getTDay();

        SensorValue sensorValue = temperatureMonitor.getSensorValueMap().get(termostatConfig.getSensor());
        if (sensorValue != null) {

            float currentT = (float) sensorValue.getValue();

            float delta = termostatConfig.getDelta();

            if (on) {
                on = currentT >= mediana + delta;
            } else {
                on = currentT <= mediana - delta;
            }


            Pin heatingPin = CommandExecutor.HEATING_PIN;

            PinState oldState = relayController.getPinState(heatingPin);
            if(oldState!=null && oldState.isHigh()!=on){
                if(on){
                    relayController.turnOnRelay(heatingPin);
                }else{
                    relayController.turnOnRelay(heatingPin);
                }
            }

            log.info("Check temperature between({},{}), current:{}, state:{}", mediana - delta, mediana + delta,
                    currentT, on);
        }
    }

    public boolean night() {

        LocalDateTime time = timer.getCurrentTime();
        LocalDate date = LocalDate.now();

        int dayShift = 0;
        if (termostatConfig.getNightBegin() > termostatConfig.getNightEnd()) {
            dayShift = 1;
        }

        LocalTime nightBegin = termostatConfig.nightBegin();
        LocalDateTime begin = LocalDateTime.of(date, nightBegin);
        begin = begin.minusDays(dayShift);

        LocalTime nightEnd = termostatConfig.nightEnd();
        LocalDateTime end = LocalDateTime.of(date, nightEnd);


        return time.isAfter(begin) && time.isBefore(end);
    }

}
