package org.tcontrol.sms;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.commands.CommandExecutor;
import org.tcontrol.sms.config.TermostatConfig;
import org.tcontrol.sms.dao.SensorValue;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class Termostat {
    @Autowired
    @Setter
    private TermostatConfig termostatConfig;

    @Autowired
    @Setter
    private ITemperatureMonitor temperatureMonitor;

    @Autowired
    @Setter
    IRelayController relayController;

    @Autowired
    @Setter
    private ITimer timer;

    @Getter
    boolean heatingOn;

    @Getter
    boolean on = true;

    @Scheduled(cron = "${termostat.schedule}")
    public void checkTemperature() {

        if(!on){
            return;
        }

        float mediana = night() ? termostatConfig.getTNight() : termostatConfig.getTDay();

        SensorValue sensorValue = temperatureMonitor.getSensorValueMap().get(termostatConfig.getSensor());
        if (sensorValue != null) {

            float currentT = (float) sensorValue.getValue();

            float delta = termostatConfig.getDelta();

            if (heatingOn) {
                heatingOn = currentT <= mediana + delta;
            } else {
                heatingOn = currentT <= mediana - delta;
            }


            Pin heatingPin = CommandExecutor.HEATING_PIN;

            PinState oldState = relayController.getPinState(heatingPin);
            if (oldState == null || oldState.isHigh() != heatingOn) {
                if (heatingOn) {
                    relayController.turnOnRelay(heatingPin);
                } else {
                    relayController.turnOffRelay(heatingPin);
                }
            }

            log.info("Check temperature between({},{}), current:{}, state:{}", mediana - delta, mediana + delta,
                    currentT, heatingOn);
        }
    }

    public void changeOn(boolean v) {
        if (!v && on) {
            this.on = false;
            Pin relayPin = RaspiPin.getPinByName(termostatConfig.getRelayPin());
            relayController.turnOffRelay(relayPin);
            log.info("Termostat's relay switched off");
        } else {
            this.on = v;
        }
        log.info("Termostat is " + (this.on?"ON":"OFF"));
    }

    public boolean night() {

        LocalDateTime time = timer.getCurrentTime();
        LocalDate date = time.toLocalDate();

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
