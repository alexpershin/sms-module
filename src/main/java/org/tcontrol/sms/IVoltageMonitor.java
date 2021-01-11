package org.tcontrol.sms;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

public interface IVoltageMonitor {

  List<VoltageResult> getVoltageResults();

  @Data
  @AllArgsConstructor
  public static final class VoltageResult {

    String name;
    Double value;
    String unit;
  }
}
