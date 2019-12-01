package org.tcontrol.sms.commands;

import com.pi4j.io.gpio.PinState;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.ISMSCommand;
import org.tcontrol.sms.ITemperatureMonitor;
import org.tcontrol.sms.IThermostat;
import org.tcontrol.sms.config.SMSConfig;
import org.tcontrol.sms.config.SensorConfig;
import org.tcontrol.sms.dao.SensorValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;


@Component
@Slf4j
@Qualifier("statusCommand")
@AllArgsConstructor
public class StatusCommand implements ISMSCommand {

    private ITemperatureMonitor temperatureMonitor;

    private SensorConfig sensorConfig;

    private IRelayController relayController;

    private IThermostat thermostat;

    private SMSConfig smsConfig;

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    @Override
    public CommandResult run() {
        Optional<String> res = temperatureMonitor.getSensorValueMap().entrySet().stream().map(t -> {
            SensorValue value = t.getValue();
            String sensorId = value.getSensorId();
            Optional<SensorConfig.SensorConfiguration> sc = sensorConfig.getSensors().stream().filter(sensor -> sensor.getId().equals(sensorId)).findFirst();
            return sc.map(sensorConfiguration -> sensorConfiguration.getName() + ": " + value.getValue()).orElse("");
        }).reduce((a, b) -> a + "\n" + b);

        PinState heatingState = relayController.getPinState(smsConfig.heatingPin());
        if (heatingState != null) {
            res = Optional.of(res.orElse("") + "\nHeating: " + (heatingState.isHigh() ? "ON" : "OFF"));
        }
        res = Optional.of(
                res.orElse("")
                        + "\nThermostat(" + thermostat.getMediumT() + "): "
                        + (thermostat.isOn() ? "ON" : "OFF")
                        + "\n" + simpleDateFormat.format(new Date()));

        String text = res.orElse("status not ready");
        log.info("Executed");
        return new CommandResult(res.isPresent() ? STATUS.OK : STATUS.FAILURE, text);
    }
}
