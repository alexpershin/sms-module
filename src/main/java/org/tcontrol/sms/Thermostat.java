package org.tcontrol.sms;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.commands.CommandExecutor;
import org.tcontrol.sms.config.ThermostatConfig;
import org.tcontrol.sms.dao.SensorValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@Slf4j
public class Thermostat {
    @Setter
    private ThermostatConfig thermostatConfig;

    @Setter
    private ITemperatureMonitor temperatureMonitor;

    @Setter
    IRelayController relayController;

    @Setter
    private ITimer timer;

    @Getter
    boolean heatingOn;

    @Getter
    boolean on = true;

    public Thermostat(ThermostatConfig thermostatConfig,
                      ITemperatureMonitor temperatureMonitor,
                      IRelayController relayController,
                      ITimer timer) {
        this.thermostatConfig = thermostatConfig;
        this.temperatureMonitor = temperatureMonitor;
        this.relayController = relayController;
        this.timer = timer;
    }

    @Scheduled(cron = "${thermostat.schedule}")
    public void checkTemperature() {

        if(!on){
            return;
        }

        float mediumT = night() ? thermostatConfig.getTNight() : thermostatConfig.getTDay();

        SensorValue sensorValue = temperatureMonitor.getSensorValueMap().get(thermostatConfig.getSensor());
        if (sensorValue != null) {

            float currentT = (float) sensorValue.getValue();

            float delta = thermostatConfig.getDelta();

            if (heatingOn) {
                heatingOn = currentT <= mediumT + delta;
            } else {
                heatingOn = currentT <= mediumT - delta;
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

            log.info("Check temperature between({},{}), current:{}, state:{}", mediumT - delta, mediumT + delta,
                    currentT, heatingOn);
        }
    }

    public void changeOn(boolean v) {
        if (!v && on) {
            this.on = false;
            Pin relayPin = RaspiPin.getPinByName(thermostatConfig.getRelayPin());
            relayController.turnOffRelay(relayPin);
            log.info("Termostat's relay switched off");
        } else {
            this.on = v;
        }
        log.info("Termostat is " + (this.on?"ON":"OFF"));
    }

    private boolean night() {

        LocalDateTime time = timer.getCurrentTime();
        LocalDate date = time.toLocalDate();

        int dayShift = 0;
        if (thermostatConfig.getNightBegin() > thermostatConfig.getNightEnd()) {
            dayShift = 1;
        }

        LocalTime nightBegin = thermostatConfig.nightBegin();
        LocalDateTime begin = LocalDateTime.of(date, nightBegin);
        begin = begin.minusDays(dayShift);

        LocalTime nightEnd = thermostatConfig.nightEnd();
        LocalDateTime end = LocalDateTime.of(date, nightEnd);


        return time.isAfter(begin) && time.isBefore(end);
    }

}
