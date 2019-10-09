package org.tcontrol.sms.commands;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.ISMSCommand;

@Component
@Slf4j
@Qualifier("heatingOffCommand")
public class HeatingOffCommand implements ISMSCommand {
    @Autowired
    private IRelayController relayController;

    @Override
    public CommandResult run() {
        PinState result = relayController.turnOffRelay(CommandExecutor.HEATING_PIN);
        log.info("Executed");
        return new CommandResult(STATUS.OK,"heating is " + (result.isHigh()?"ON":"OFF"));
    }
}