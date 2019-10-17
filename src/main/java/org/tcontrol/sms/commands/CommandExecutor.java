package org.tcontrol.sms.commands;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.ISMSCommand;

@Component
public class CommandExecutor {
    public enum CommandName {
        STATUS,
        HEATING_ON,
        HEATING_OFF
    }

    public static final Pin HEATING_PIN = RaspiPin.GPIO_00;

    @Autowired
    ISMSCommand heatingOnCommand;

    @Autowired
    ISMSCommand heatingOffCommand;

    @Autowired
    ISMSCommand statusCommand;


    public CommandResult run(String name) {
        final CommandName commandName;
        try {
            commandName = CommandName.valueOf(name);
        }catch(IllegalArgumentException e){
            return new CommandResult(STATUS.FAILURE, "unknown command: " + name);
        }
        ISMSCommand command = null;
        switch (commandName) {
            case STATUS:
                command = statusCommand;
                break;
            case HEATING_ON:
                command = heatingOnCommand;
                break;
            case HEATING_OFF:
                command = heatingOffCommand;
                break;

        }
        return command.run();
    }
}
