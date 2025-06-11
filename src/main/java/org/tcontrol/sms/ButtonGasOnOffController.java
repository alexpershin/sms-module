package org.tcontrol.sms;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tcontrol.sms.config.props.ButtonConfig;


@Service
@Slf4j
public class ButtonGasOnOffController {

    private GpioController gpio;

    public ButtonGasOnOffController(ButtonConfig buttonConfig, IThermostat thermostatGas) {
        this.buttonConfig = buttonConfig;
        this.thermostatGas = thermostatGas;
    }

    private final ButtonConfig buttonConfig;
    private final IThermostat thermostatGas;

    @PostConstruct
    public void init() {
        try {
            gpio = GpioFactory.getInstance();

            GpioPinListenerDigital listener = event -> {
                PinState value = event.getState();

                // display output
                log.info("BUTTON state: " + value.isHigh());
                if(value.isHigh()){
                    boolean thetmostatNewState = !thermostatGas.isOn();
                    log.info("Thermostat new state: " + thetmostatNewState);
                    thermostatGas.changeOn(thetmostatNewState);
                }
            };

            final GpioPinDigitalInput pin =
                gpio.provisionDigitalInputPin(
                    buttonConfig.pin(),
                    "Thermostat on/of",
                    PinPullResistance.PULL_UP);

            gpio.addListener(listener, pin);
            log.info("Button listener added: " + pin.getPin().getName());
        } catch (Throwable e) {
            log.error(e.getMessage());
        }
    }
}
