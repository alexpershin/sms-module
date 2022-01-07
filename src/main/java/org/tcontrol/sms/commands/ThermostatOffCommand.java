package org.tcontrol.sms.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
public class ThermostatOffCommand implements ISMSCommand {

    private final IRelayController relayController;

    private final IThermostat thermostatElectro;

    private final ThermostatConfig thermostatElectroConfig;

    @Override
    public CommandResult run() {

        thermostatElectro.changeOn(false);

        thermostatElectroConfig.relayPins().forEach(pin -> {
            relayController.turnOffRelay(pin);
        });

        log.info("Executed");
        return new CommandResult(STATUS.OK, "Termostat is off");
    }
}
