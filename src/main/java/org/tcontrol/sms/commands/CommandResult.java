package org.tcontrol.sms.commands;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommandResult {
    private STATUS status;
    private String message;
}
