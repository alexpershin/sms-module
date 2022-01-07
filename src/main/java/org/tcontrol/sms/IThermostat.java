package org.tcontrol.sms;

import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;

public interface IThermostat {

  void checkTemperature();

  boolean isHeatingOn();

  boolean isOn();

  float getMediumT();

  void changeOn(boolean v);

  List<String> getHeatingPins();

  String getName();
}
