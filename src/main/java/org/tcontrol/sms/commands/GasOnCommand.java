package org.tcontrol.sms.commands;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.ISMSCommand;
import org.tcontrol.sms.IThermostat;

@Component
@Slf4j
@Qualifier("thermostatOnCommand")
@AllArgsConstructor
public class GasOnCommand implements ISMSCommand {

    private IThermostat thermostatGas;

    @Override
    public CommandResult run() {

        thermostatGas.changeOn(true);

        log.info("Executed");
        return new CommandResult(STATUS.OK, "Gas thermostat is on");
    }
}
