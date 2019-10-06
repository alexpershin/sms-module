package org.tcontrol.sms.dao;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class SensorValue {

    private String sensorId;
    private Timestamp timestamp;
    private double value;
    private Double gradient;
    /**
     * State of the sensor. Are not stored in the database. It should be
     * calculated in accordance with the sensor value and the low and hight
     * threshold. For example when temperature inside a house is lower than low
     * threshold set to 12 degrees, the sensor state must be ALARM, also if the
     * temperature is higher than 30 degrees it must be ALARM, when temperature
     * is between low and high thresholds the state is NORMAL.
     */
    private SensorState state;


    public SensorValue(String sensorId, long time,
                       double value) {
        this.sensorId = sensorId;
        this.timestamp = new Timestamp(time);
        this.value = value;
    }


    public enum SensorState {

        /**
         * alarm on the sensor.
         */
        ALARM,
        /**
         * pre alarm state.
         */
        WARNING,
        /**
         * no alarm, everything is OK with sensor's state.
         */
        NORMAL,
        /**
         * sensor is intentionally disabled by user, right now it is not
         * implemented yet.
         */
        DISABLED,
        /**
         * sensor is in ON state.
         */
        ON,
        /**
         * sensor is in ON state.
         */
        OFF

    }
}

