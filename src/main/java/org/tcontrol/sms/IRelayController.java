package org.tcontrol.sms;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public interface IRelayController {
    PinState turnOnRelay(Pin controlPin);

    PinState turnOffRelay(Pin controlPin);

    PinState getPinState(Pin controlPin);
}
