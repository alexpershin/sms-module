package org.tcontrol.sms.commands;

import com.pi4j.io.gpio.PinState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.ISMSCommand;
import org.tcontrol.sms.IThermostat;

@Component
@Slf4j
@Qualifier("thermostatOnCommand")
public class ThermostatOnCommand implements ISMSCommand {

    private IThermostat thermostat;

    public ThermostatOnCommand(IThermostat thermostat) {
        this.thermostat = thermostat;
    }

    @Override
    public CommandResult run() {

        thermostat.changeOn(true);

        log.info("Executed");
        return new CommandResult(STATUS.OK, "Termostat is on");
    }
}
