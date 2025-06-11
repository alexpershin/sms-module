package org.tcontrol.sms.config.props;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;

@Data
@RequiredArgsConstructor
public class ThermostatConfig {

  private float tDay;
  private float tNight;
  private float delta;
  private int nightBegin;
  private int nightEnd;
  private String sensor;
  private List<String> relayPins =  new ArrayList<>();

  @PostConstruct
  public void postConstruct() {
    nightBegin();
    nightEnd();
    relayPins.forEach((pin) -> RaspiPin.getPinByName(pin));
  }

  public LocalTime nightBegin() {
    return LocalTime.ofSecondOfDay(nightBegin);
  }

  public LocalTime nightEnd() {
    return LocalTime.ofSecondOfDay(nightEnd);
  }

  public List<Pin> relayPins() {
    return relayPins.stream().map(RaspiPin::getPinByName).collect(Collectors.toList());
  }
}
