package org.tcontrol.sms.commands;

import com.pi4j.io.gpio.PinState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.ISMSCommand;
import org.tcontrol.sms.IThermostat;
import org.tcontrol.sms.Thermostat;
import org.tcontrol.sms.config.SMSConfig;

@Component
@Slf4j
@Qualifier("heatingOnCommand")
public class HeatingOnCommand implements ISMSCommand {

    private IRelayController relayController;

    private IThermostat thermostat;

    private SMSConfig smsConfig;

    @Getter
    private Boolean prevThermostatState;

    public HeatingOnCommand(IRelayController relayController,
                            IThermostat thermostat,
                            SMSConfig smsConfig) {
        this.relayController = relayController;
        this.thermostat = thermostat;
        this.smsConfig = smsConfig;
    }

    @Override
    public CommandResult run() {
        prevThermostatState = thermostat.isOn();

        thermostat.changeOn(false);

        PinState result = relayController.turnOnRelay(smsConfig.heatingPin());


        log.info("Executed");
        return new CommandResult(STATUS.OK, "heating is " + (result.isHigh() ? "ON" : "OFF"));
    }
}
