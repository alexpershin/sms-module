package org.tcontrol.sms;

import org.springframework.scheduling.annotation.Scheduled;

public interface IThermostat {

  void checkTemperature();

  boolean isHeatingOn();

  boolean isOn();

  float getMediumT();

  void changeOn(boolean v);

  String getHeatingPin();

  String getName();
}
