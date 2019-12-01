package org.tcontrol.sms.commands;

import com.pi4j.io.gpio.PinState;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.ISMSCommand;
import org.tcontrol.sms.IThermostat;

@Component
@Slf4j
@Qualifier("heatingOffCommand")
@AllArgsConstructor
public class HeatingOffCommand implements ISMSCommand {
    private IRelayController relayController;
    private IThermostat thermostat;
    private HeatingOnCommand heatingOnCommand;

    @Override
    public CommandResult run() {
        PinState result = relayController.turnOffRelay(CommandExecutor.HEATING_PIN);
        Boolean prevThermostatState = heatingOnCommand.getPrevThermostatState();
        if(prevThermostatState !=null){
            thermostat.changeOn(prevThermostatState);
        }
        log.info("Executed");
        return new CommandResult(STATUS.OK,"heating is " + (result.isHigh()?"ON":"OFF"));
    }
}
