package org.tcontrol.sms.config.props;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ButtonConfig {

  private String buttonPinName;

  @PostConstruct
  public void postConstruct() {
    if (pin() == null) {
      throw new IllegalArgumentException("pin '" + buttonPinName + "' doesn't exist");
    }
  }

  public Pin pin() {
    return RaspiPin.getPinByName(buttonPinName);
  }
}
