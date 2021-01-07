package org.tcontrol.sms.commands;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.ISMSCommand;
import org.tcontrol.sms.ITemperatureMonitor;
import org.tcontrol.sms.IThermostat;
import org.tcontrol.sms.config.props.SMSConfig;
import org.tcontrol.sms.config.props.SensorConfig;
import org.tcontrol.sms.dao.SensorValue;


@Component
@Slf4j
@Qualifier("statusCommand")
@AllArgsConstructor
public class StatusCommand implements ISMSCommand {

  private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
      "yyyy.MM.dd HH:mm:ss");

  private final ITemperatureMonitor temperatureMonitor;

  private final SensorConfig sensorConfig;

  private final IRelayController relayController;

  private final IThermostat thermostatElectro;

  private final IThermostat thermostatGas;

  private final SMSConfig smsConfig;

  @Override
  public CommandResult run() {
    Optional<String> res = temperatureMonitor.getSensorValueMap().values().stream().map(value -> {
      String sensorId = value.getSensorId();
      Optional<SensorConfig.SensorConfiguration> sc = sensorConfig.getSensors().stream()
          .filter(sensor -> sensor.getId().equals(sensorId)).findFirst();
      return sc.map(sensorConfiguration -> sensorConfiguration.getName() + ": " + value.getValue())
          .orElse("");
    }).reduce((a, b) -> a + "\n" + b);

    res = thermostatStatus(res, thermostatElectro, relayController);
    res = thermostatStatus(res, thermostatGas, relayController);

    String text = res.orElse("status not ready");
    log.info("Executed");
    return new CommandResult(res.isPresent() ? STATUS.OK : STATUS.FAILURE, text);
  }

  private static Optional<String> thermostatStatus(Optional<String> res,
      IThermostat thermostat, IRelayController relayController) {
    String heatingPin = thermostat.getHeatingPin();
    Pin pin = RaspiPin.getPinByName(heatingPin);

    PinState heatingState = relayController.getPinState(pin);
    if (heatingState != null) {
      res = Optional.of(res.orElse("") + "\nHeating: " + (heatingState.isHigh() ? "ON" : "OFF"));
    }
    res = Optional.of(
        res.orElse("")
            + "\n"+ thermostat.getName() + "(" + thermostat.getMediumT() + "): "
            + (thermostat.isOn() ? "ON" : "OFF")
            + "\n" + simpleDateFormat.format(new Date()));
    return res;
  }
}
