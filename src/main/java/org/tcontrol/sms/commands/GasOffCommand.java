package org.tcontrol.sms.commands;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.ISMSCommand;
import org.tcontrol.sms.IThermostat;
import org.tcontrol.sms.config.props.ThermostatConfig;

@Component
@Slf4j
@Qualifier("thermostatOffCommand")
@AllArgsConstructor
public class GasOffCommand implements ISMSCommand {

    private final IRelayController relayController;

    private final IThermostat thermostatGas;

    private final ThermostatConfig thermostatGasConfig;

    @Override
    public CommandResult run() {

        thermostatGas.changeOn(false);
        relayController.turnOffRelay(thermostatGasConfig.relayPin());

        log.info("Executed");
        return new CommandResult(STATUS.OK, "Gas thermostat is off");
    }
}
