package org.tcontrol.sms.commands;

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
public class ThermostatOffCommand implements ISMSCommand {

    private IRelayController relayController;

    private IThermostat thermostat;

    private ThermostatConfig thermostatConfig;

    @Getter
    private Boolean prevThermostatState;

    public ThermostatOffCommand(IRelayController relayController,
                                IThermostat thermostat,
                                ThermostatConfig thermostatConfig) {
        this.relayController = relayController;
        this.thermostat = thermostat;
        this.thermostatConfig = thermostatConfig;
    }

    @Override
    public CommandResult run() {

        thermostat.changeOn(false);
        relayController.turnOffRelay(thermostatConfig.relayPin());

        log.info("Executed");
        return new CommandResult(STATUS.OK, "Termostat is off");
    }
}
