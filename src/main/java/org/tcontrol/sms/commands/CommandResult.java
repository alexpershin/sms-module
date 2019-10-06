package org.tcontrol.sms.commands;

import lombok.Data;

@Data
public class CommandResult {
    private STATUS status;
    private String message;
}
