package org.tcontrol.sms.commands;

import com.pi4j.io.gpio.PinState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.ISMSCommand;
import org.tcontrol.sms.IThermostat;
import org.tcontrol.sms.Thermostat;

@Component
@Slf4j
@Qualifier("heatingOnCommand")
public class HeatingOnCommand implements ISMSCommand {

    private IRelayController relayController;

    private IThermostat thermostat;

    @Getter
    private Boolean prevThermostatState;

    public HeatingOnCommand(IRelayController relayController, IThermostat thermostat) {
        this.relayController = relayController;
        this.thermostat = thermostat;
    }

    @Override
    public CommandResult run() {
        prevThermostatState = thermostat.isOn();

        PinState result = relayController.turnOnRelay(CommandExecutor.HEATING_PIN);

        thermostat.changeOn(false);

        log.info("Executed");
        return new CommandResult(STATUS.OK, "heating is " + (result.isHigh() ? "ON" : "OFF"));
    }
}
