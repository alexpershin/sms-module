import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import org.junit.Before;
import org.junit.Test;
import org.tcontrol.sms.*;
import org.tcontrol.sms.config.ThermostatConfig;
import org.tcontrol.sms.dao.SensorValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class ThermostatTest {

    private static final String GPIO_00 = "GPIO_00";
    private static final String SENSOR = "abc";
    private Thermostat thermostat;
    private ThermostatConfig thermostatConfig;
    private ITemperatureMonitor temperatureMonitor;
    private IRelayController relayController;
    private ITimer timer;

    @Before
    public void before() {
        thermostatConfig = new ThermostatConfig();
        thermostatConfig.setDelta(0.5f);
        thermostatConfig.setNightBegin(60 * 60 * 23);
        thermostatConfig.setNightEnd(60 * 60 * 7);
        thermostatConfig.setSensor(SENSOR);
        thermostatConfig.setTDay(12.0f);
        thermostatConfig.setTNight(16.0f);
        thermostatConfig.setRelayPin(GPIO_00);

        temperatureMonitor = new ITemperatureMonitor() {

            Map<String, SensorValue> map = new HashMap<>();

            {
                SensorValue v = new SensorValue();
                v.setSensorId(SENSOR);
                v.setValue(13.0);
                map.put(SENSOR, v);

            }

            @Override
            public Map<String, SensorValue> getSensorValueMap() {
                return map;
            }

            @Override
            public void readSensors() {

            }
        };

        relayController = new IRelayController() {
            PinState currentPinState = PinState.LOW;

            @Override
            public PinState turnOnRelay(Pin controlPin) {
                currentPinState = PinState.HIGH;
                return currentPinState;
            }

            @Override
            public PinState turnOffRelay(Pin controlPin) {
                currentPinState = PinState.LOW;
                return currentPinState;
            }

            @Override
            public PinState getPinState(Pin controlPin) {
                return currentPinState;
            }
        };


        thermostat = new Thermostat(
                thermostatConfig,
                temperatureMonitor,
                relayController,
                () -> LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0))
       );

    }

    @Test
    public void testMidDay() {
        timer = () -> LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0));
        thermostat.setTimer(timer);

        thermostat.checkTemperature();

        assertFalse(thermostat.isHeatingOn());
    }

    @Test
    public void testMidNight() {
        timer = () -> LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0));
        thermostat.setTimer(timer);

        thermostat.checkTemperature();

        assertTrue(thermostat.isHeatingOn());
    }

    @Test
    public void testSwitch() {
        thermostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0)));
        Pin heatingPin = RaspiPin.getPinByName(thermostatConfig.getRelayPin());

        thermostat.checkTemperature();
        assertTrue(thermostat.isHeatingOn());
        assertTrue(relayController.getPinState(heatingPin).isHigh());

        SensorValue v = temperatureMonitor.getSensorValueMap().get(SENSOR);
        v.setSensorId(SENSOR);
        v.setValue(15.0);

        thermostat.checkTemperature();
        assertTrue(thermostat.isHeatingOn());
        assertTrue(relayController.getPinState(heatingPin).isHigh());

        v.setValue(16.6);

        thermostat.checkTemperature();
        assertFalse(thermostat.isHeatingOn());
        assertFalse(relayController.getPinState(heatingPin).isHigh());

        thermostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(4, 0)));
        v.setValue(16.2);
        thermostat.checkTemperature();
        assertFalse(thermostat.isHeatingOn());
        assertFalse(relayController.getPinState(heatingPin).isHigh());

        thermostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 0)));
        v.setValue(15.6);
        thermostat.checkTemperature();
        assertFalse(thermostat.isHeatingOn());
        assertFalse(relayController.getPinState(heatingPin).isHigh());

        thermostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 30)));
        v.setValue(15.5);
        thermostat.checkTemperature();
        assertTrue(thermostat.isHeatingOn());
        assertTrue(relayController.getPinState(heatingPin).isHigh());

        thermostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0)));
        v.setValue(15.5);
        thermostat.checkTemperature();
        assertFalse(thermostat.isHeatingOn());

        thermostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(15, 0)));
        v.setValue(11.5);
        thermostat.checkTemperature();
        assertTrue(thermostat.isHeatingOn());
        assertTrue(relayController.getPinState(heatingPin).isHigh());

        thermostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0)));
        v.setValue(12.8);
        thermostat.checkTemperature();
        assertFalse(thermostat.isHeatingOn());
        assertFalse(relayController.getPinState(heatingPin).isHigh());

        thermostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0)));
        v.setValue(12.1);
        thermostat.checkTemperature();
        assertFalse(thermostat.isHeatingOn());
        assertFalse(relayController.getPinState(heatingPin).isHigh());

        thermostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(00, 10)));
        v.setValue(12.1);
        thermostat.checkTemperature();
        assertEquals(16.0, thermostat.getMediumT(), 0.01);
        assertTrue(thermostat.isHeatingOn());
        assertTrue(relayController.getPinState(heatingPin).isHigh());

        thermostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(06, 50)));
        v.setValue(15.2);
        thermostat.checkTemperature();
        assertEquals(16.0, thermostat.getMediumT(), 0.01);
        assertTrue(thermostat.isHeatingOn());
        assertTrue(relayController.getPinState(heatingPin).isHigh());

        thermostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(07, 10)));
        v.setValue(15.3);
        thermostat.checkTemperature();
        assertEquals(12.0, thermostat.getMediumT(), 0.01);
        assertFalse(thermostat.isHeatingOn());
        assertFalse(relayController.getPinState(heatingPin).isHigh());
    }

    @Test
    public void testOnOff() {
        thermostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0)));
        Pin heatingPin = RaspiPin.getPinByName(thermostatConfig.getRelayPin());

        thermostat.checkTemperature();
        assertTrue(thermostat.isHeatingOn());
        assertTrue(relayController.getPinState(heatingPin).isHigh());

        thermostat.changeOn(false);
        assertFalse(relayController.getPinState(heatingPin).isHigh());

        thermostat.checkTemperature();
        assertFalse(relayController.getPinState(heatingPin).isHigh());

        thermostat.changeOn(true);
        assertFalse(relayController.getPinState(heatingPin).isHigh());

        thermostat.checkTemperature();
        assertTrue(relayController.getPinState(heatingPin).isHigh());
    }
}
