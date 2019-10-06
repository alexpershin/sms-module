package org.tcontrol.sms.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.ISMSCommand;

@Component
public class CommandExecutor {
    public enum CommandName {
        STATUS,
        HEATING_ON,
        HEATING_OFF;
    }

    @Autowired
    ISMSCommand heatingOnCommand;

    @Autowired
    ISMSCommand heatingOffCommand;

    @Autowired
    ISMSCommand statusCommand;


    public CommandResult run(CommandName commandName) {
        ISMSCommand command = null;
        switch (commandName){
            case STATUS:command=statusCommand;break;
            case HEATING_ON:command=heatingOnCommand;break;
            case HEATING_OFF:command=heatingOffCommand;break;
        }
        return command.run();
    }
}
