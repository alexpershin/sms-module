package org.tcontrol.sms;

import com.pi4j.io.gpio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.dao.SensorValue;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class RelayControler implements IRelayController {

    @Autowired
    CSVStatisticsWriter statisticsWriter;

    private GpioController gpio;
    final private Map<String, GpioPinDigitalOutput> pins = new HashMap<>();

    @PostConstruct
    void init() {
        // create gpio controller
        try {
            gpio = GpioFactory.getInstance();
        } catch (Throwable e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public PinState turnOnRelay(Pin controlPin) {
        return changeStateOfRelay(true, controlPin);
    }

    @Override
    public PinState turnOffRelay(Pin controlPin) {
        return changeStateOfRelay(false, controlPin);
    }

    @Override
    public PinState getPinState(Pin controlPin) {
        if (gpio == null) {
            return null;
        }

        init(controlPin);

        GpioPinDigitalOutput gpioPinDigitalOutput = pins.get(controlPin.getName());

        return gpioPinDigitalOutput.getState();
    }

    private PinState changeStateOfRelay(boolean action, Pin controlPin) {
        if (gpio == null) {
            return null;
        }

        init(controlPin);

        PinState pinState = action ? PinState.HIGH : PinState.LOW;

        GpioPinDigitalOutput pin = pins.get(controlPin.getName());

        // set shutdown state for this pin
        pin.setShutdownOptions(true, pinState);

        log.info("GPIO state was: " + pin.getState());

        SensorValue sensorValue = new SensorValue();
        sensorValue.setSensorId(pin.getName());

        // turn off/heatingOn gpio pin #17
        if (pinState.isLow()) {
            pin.low();
            sensorValue.setValue(1.0);
        } else if (pinState.isHigh()) {
            pin.high();
            sensorValue.setValue(0.0);
        }
        statisticsWriter.write(new SensorValue[]{sensorValue});

        return pin.getState();
    }

    private void init(Pin controlPin) {
        GpioPinDigitalOutput gpioPinDigitalOutput = pins.get(controlPin.getName());
        if (gpioPinDigitalOutput == null) {
            GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(controlPin, "Heat Relay", PinState.LOW);
            pins.put(controlPin.getName(), pin);
        }
    }
}
