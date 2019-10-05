package org.tcontrol.sms;

import org.tcontrol.sms.dao.SensorValue;

import java.io.IOException;

public interface MonitorInterface {
    SensorValue loadValue(String sensorUUID) throws IOException;
}
