package org.tcontrol.sms;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.dao.SensorValue;

@Component
@Slf4j
public class RelayController implements IRelayController {

  private CSVStatisticsWriter statisticsWriter;

  private GpioController gpio;
  final private Map<String, GpioPinDigitalOutput> pins = new HashMap<>();

  public RelayController(CSVStatisticsWriter statisticsWriter) {
    this.statisticsWriter = statisticsWriter;
  }

  @PostConstruct
  void init() {
    log.info("Initializing relay controller...");
    // create gpio controller
    try {
      gpio = GpioFactory.getInstance();
      log.info("Initializing relay controller COMPLETE");
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
    sensorValue.setSensorId("" + controlPin.getAddress());
    sensorValue.setTimestamp(new Timestamp(System.currentTimeMillis()));

    // turn off/heatingOn gpio pin #17
    if (pinState.isLow()) {
      pin.low();
      sensorValue.setValue(0.0);
    } else if (pinState.isHigh()) {
      pin.high();
      sensorValue.setValue(1.0);
    }
    statisticsWriter.write(new SensorValue[]{sensorValue});

    return pin.getState();
  }

  private void init(Pin controlPin) {
    log.info("Initializing output pin: " + controlPin.getName());
    GpioPinDigitalOutput gpioPinDigitalOutput = pins.get(controlPin.getName());
    if (gpioPinDigitalOutput == null) {
      GpioPinDigitalOutput pin = gpio
          .provisionDigitalOutputPin(controlPin, "heat_relay_" + controlPin.getAddress(),
              PinState.LOW);
      pins.put(controlPin.getName(), pin);
      log.info("Initializing output pin (" + controlPin.getName() + ") COMPLETE");
    }
  }
}
