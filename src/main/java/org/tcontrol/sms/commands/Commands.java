package org.tcontrol.sms.commands;

import org.tcontrol.sms.ISMSCommand;
import org.tcontrol.sms.commands.HeatingOnCommand;
import org.tcontrol.sms.commands.HeationgOffCommand;
import org.tcontrol.sms.commands.StatusCommand;

public enum Commands {
    STATUS(new StatusCommand()),
    HEATING_ON(new HeatingOnCommand()),
    HEATING_OFF(new HeationgOffCommand());
    ISMSCommand command;

    Commands(ISMSCommand command) {
        this.command = command;
    }

    public STATUS run() {
        return command.run();
    }
}
