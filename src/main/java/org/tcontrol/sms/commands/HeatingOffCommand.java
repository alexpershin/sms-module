package org.tcontrol.sms.commands;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.ISMSCommand;
import org.tcontrol.sms.IThermostat;
import org.tcontrol.sms.config.props.SMSConfig;

@Component
@Slf4j
@Qualifier("heatingOffCommand")
@AllArgsConstructor
public class HeatingOffCommand implements ISMSCommand {
    private final IRelayController relayController;
    private final IThermostat thermostatElectro;
    private final HeatingOnCommand heatingOnCommand;

    @Override
    public CommandResult run() {
        String resultString = " ";
        for(String heatingPin: thermostatElectro.getHeatingPins()){
            Pin pin = RaspiPin.getPinByName(heatingPin);
            PinState result = relayController.turnOffRelay(pin);
            resultString+=(result.isHigh()?"ON":"OFF");
        }

        Boolean prevThermostatState = heatingOnCommand.getPrevThermostatState();
        if(prevThermostatState !=null){
            thermostatElectro.changeOn(prevThermostatState);
        }

        log.info("Executed");
        return new CommandResult(STATUS.OK,"heating is" + resultString);
    }
}
