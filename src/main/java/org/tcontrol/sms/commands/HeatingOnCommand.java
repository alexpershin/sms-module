package org.tcontrol.sms.commands;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.ISMSCommand;
import org.tcontrol.sms.IThermostat;
import org.tcontrol.sms.config.props.SMSConfig;

@Component
@Slf4j
@Qualifier("heatingOnCommand")
public class HeatingOnCommand implements ISMSCommand {

    private final IRelayController relayController;

    private final IThermostat thermostat;

    private final SMSConfig smsConfig;

    @Getter
    private Boolean prevThermostatState;

    public HeatingOnCommand(IRelayController relayController,
                            IThermostat thermostatElectro,
                            SMSConfig smsConfig) {
        this.relayController = relayController;
        this.thermostat = thermostatElectro;
        this.smsConfig = smsConfig;
    }

    @Override
    public CommandResult run() {
        prevThermostatState = thermostat.isOn();

        thermostat.changeOn(false);
        String heatingPin = thermostat.getHeatingPin();
        Pin pin = RaspiPin.getPinByName(heatingPin);
        PinState result = relayController.turnOnRelay(pin);

        log.info("Executed");
        return new CommandResult(STATUS.OK, "heating is " + (result.isHigh() ? "ON" : "OFF"));
    }
}
