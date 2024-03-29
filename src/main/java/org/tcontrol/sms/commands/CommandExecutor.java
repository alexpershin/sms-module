package org.tcontrol.sms.commands;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.ISMSCommand;

@Component
@AllArgsConstructor
public class CommandExecutor {
    public enum CommandName {
        STATUS,
        HEATING_ON,
        HEATING_OFF,
        THERM_ON,
        THERM_OFF,
        GAS_ON,
        GAS_OFF,
        SET_TEMP
    }

    private ISMSCommand heatingOnCommand;

    private ISMSCommand heatingOffCommand;

    private ISMSCommand statusCommand;

    private ISMSCommand thermostatOnCommand;

    private ISMSCommand thermostatOffCommand;

    private ISMSCommand gasOnCommand;

    private ISMSCommand gasOffCommand;

    public CommandResult run(String name) {
        final CommandName commandName;
        try {
            commandName = CommandName.valueOf(name);
        } catch (IllegalArgumentException e) {
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
            case THERM_ON:
                command = thermostatOnCommand;
                break;
            case THERM_OFF:
                command = thermostatOffCommand;
                break;
            case GAS_ON:
                command = gasOnCommand;
                break;
            case GAS_OFF:
                command = gasOffCommand;
                break;
        }

        CommandResult result = command != null ? command.run() : null;

        return result;
    }
}
