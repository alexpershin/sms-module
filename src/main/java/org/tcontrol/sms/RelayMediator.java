package org.tcontrol.sms;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class RelayMediator implements IRelayMediator{

  private IRelayController relayController;

  @Override
  public PinState turnOnRelay(Pin controlPin) {
    return null;
  }

  @Override
  public PinState turnOffRelay(Pin controlPin) {
    return null;
  }

  @Override
  public PinState getPinState(Pin controlPin) {
    return null;
  }
}
