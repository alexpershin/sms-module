package org.tcontrol.sms.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.ISMSCommand;

@Component
@Slf4j
@Qualifier("heatingOffCommand")
public class HeatingOffCommand implements ISMSCommand {
    @Override
    public CommandResult run() {
        log.info("Executed");
        CommandResult result = new CommandResult();
        result.setStatus(STATUS.FAILURE);
        return result;
    }
}
