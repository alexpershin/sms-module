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
import org.tcontrol.sms.IVoltageMonitor;
import org.tcontrol.sms.IVoltageMonitor.VoltageResult;
import org.tcontrol.sms.config.props.SensorConfig;


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

  private final IThermostat thermostatVeranda;

  private final IVoltageMonitor voltageMonitor;

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
    res = thermostatStatus(res, thermostatVeranda, relayController);
    res = voltageStatus(res, voltageMonitor);

    res = Optional.of(res.orElse("") + "\n" + simpleDateFormat.format(new Date()));

    String text = res.orElse("status not ready");
    log.info("Executed");
    return new CommandResult(res.isPresent() ? STATUS.OK : STATUS.FAILURE, text);
  }

  private Optional<String> voltageStatus(Optional<String> res, IVoltageMonitor voltageMonitor) {
    for (VoltageResult vr : voltageMonitor.getVoltageResults()) {
      res = Optional.of(
          res.orElse("")
              + "\n" + vr.getName() + ": " + vr.getValue() + vr.getUnit());
    }
    return res;
  }

  private static Optional<String> thermostatStatus(Optional<String> res,
      IThermostat thermostat, IRelayController relayController) {
    String heatingPin = thermostat.getHeatingPin();
    Pin pin = RaspiPin.getPinByName(heatingPin);

    PinState heatingState = relayController.getPinState(pin);
    res = Optional.of(
        res.orElse("")
            + "\n" + thermostat.getName() + "(" + thermostat.getMediumT() + "): "
            + (thermostat.isOn() ? "ON" : "OFF") + ", heating: " + (heatingState.isHigh() ? "ON" : "OFF"));
    return res;
  }
}
