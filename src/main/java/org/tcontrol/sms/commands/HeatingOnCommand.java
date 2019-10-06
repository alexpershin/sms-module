package org.tcontrol.sms.commands;

import lombok.extern.slf4j.Slf4j;
import org.tcontrol.sms.ISMSCommand;

@Slf4j
public class HeatingOnCommand implements ISMSCommand {
    @Override
    public STATUS run() {
        log.info("Executed");
        return STATUS.FAILURE;
    }
}
