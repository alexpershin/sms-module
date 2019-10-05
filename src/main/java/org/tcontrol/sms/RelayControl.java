package org.tcontrol.sms;

import com.pi4j.io.gpio.*;

import java.util.Optional;

public class RelayControl {
    void turnOnRelay(
            Optional<Boolean> action,
            Pin controlPin,
            int timeout
    ) {
        System.out.println("GPIO Relay Control started.");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalOutput pin
                = gpio.provisionDigitalOutputPin(controlPin, "Heat Relay", PinState.LOW);

        if (action.isPresent()) {
            PinState pinState = action.get() ? PinState.HIGH : PinState.LOW;

            // set shutdown state for this pin
            pin.setShutdownOptions(true, pinState);

            System.out.println("--> GPIO state was: " + pin.getState());

            // turn off/on gpio pin #17
            if (pinState.isLow()) {
                pin.low();
            } else if (pinState.isHigh()) {
                pin.high();
            }

            try {
                Thread.sleep(timeout * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("--> GPIO state is: " + pin.getState());

        // turn on gpio pin
        System.out.println("--> GPIO shutdown");

    }
}
