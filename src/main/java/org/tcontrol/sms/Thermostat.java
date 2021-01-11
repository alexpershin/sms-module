package org.tcontrol.sms;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.tcontrol.sms.config.props.ThermostatConfig;
import org.tcontrol.sms.dao.SensorValue;

@Slf4j
public class Thermostat implements IThermostat {

  private final ThermostatConfig thermostatConfig;

  private final ITemperatureMonitor temperatureMonitor;

  private final IRelayController relayController;

  private final ITimer timer;

  @Getter
  private final String name;

  @Getter
  boolean heatingOn;

  @Getter
  boolean on = true;

  @Getter
  float mediumT;

  public Thermostat(ThermostatConfig thermostatConfig,
      ITemperatureMonitor temperatureMonitor,
      IRelayController relayController,
      ITimer timer,
      String name
  ) {
    this.thermostatConfig = thermostatConfig;
    this.temperatureMonitor = temperatureMonitor;
    this.relayController = relayController;
    this.timer = timer;
    this.name = name;
  }


  @Override
  @Scheduled(cron = "${thermostat.schedule}")
  public void checkTemperature() {
    log.info(name + "> checkTemperature");
    if (!on) {
      return;
    }

    mediumT = night() ? thermostatConfig.getTNight() : thermostatConfig.getTDay();

    SensorValue sensorValue = temperatureMonitor.getSensorValueMap()
        .get(thermostatConfig.getSensor());
    if (sensorValue != null) {

      float currentT = (float) sensorValue.getValue();

      float delta = thermostatConfig.getDelta();

      if (heatingOn) {
        heatingOn = currentT <= mediumT + delta;
      } else {
        heatingOn = currentT <= mediumT - delta;
      }

      Pin heatingPin = thermostatConfig.relayPin();

      PinState oldState = relayController.getPinState(heatingPin);
      if (oldState == null || oldState.isHigh() != heatingOn) {
        if (heatingOn) {
          relayController.turnOnRelay(heatingPin);
        } else {
          relayController.turnOffRelay(heatingPin);
        }
      }

      log.info(name + "> Check temperature between({},{}), current:{}, state:{}", mediumT - delta,
          mediumT + delta,
          currentT, heatingOn);
    } else {
      log.error("temperature sensor value is null");
    }
  }

  @Override
  public void changeOn(boolean v) {
    if (!v && on) {
      this.on = false;
      relayController.turnOffRelay(thermostatConfig.relayPin());
      log.info(name + "> termostat's relay switched off");
    } else {
      this.on = v;
    }
    log.info(name + "> termostat is " + (this.on ? "ON" : "OFF"));
  }

  @Override
  public String getHeatingPin() {
    return thermostatConfig.getRelayPin();
  }

  private boolean night() {

    LocalDateTime currentLocalTime = timer.getCurrentTime();
    LocalDate currentDate = currentLocalTime.toLocalDate();
    LocalTime currentTme = currentLocalTime.toLocalTime();

    LocalTime nightBegin = thermostatConfig.nightBegin();
    LocalDateTime begin = LocalDateTime.of(currentDate, nightBegin);

    LocalTime nightEnd = thermostatConfig.nightEnd();
    LocalDateTime end = LocalDateTime.of(currentDate, nightEnd);

    if (thermostatConfig.getNightBegin() > thermostatConfig.getNightEnd()) {
      if (currentTme.isAfter(nightBegin)) {
        end = end.plusDays(1);
      } else if (currentTme.isBefore(nightBegin)) {
        begin = begin.minusDays(1);
      }
    }

    return currentLocalTime.isAfter(begin) && currentLocalTime.isBefore(end);
  }

}
