import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import org.junit.Before;
import org.junit.Test;
import org.tcontrol.sms.*;
import org.tcontrol.sms.config.TermostatConfig;
import org.tcontrol.sms.dao.SensorValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class TermostatTest {

    public static final String GPIO_00 = "GPIO_00";
    public static final String SENSOR = "abc";
    private Termostat termostat;
    private TermostatConfig termostatConfig;
    private ITemperatureMonitor temperatureMonitor;
    private IRelayController relayController;
    private ITimer timer;

    @Before
    public void before() {
        termostatConfig = new TermostatConfig();
        termostatConfig.setDelta(0.5f);
        termostatConfig.setNightBegin(60 * 60 * 23);
        termostatConfig.setNightEnd(60 * 60 * 7);
        termostatConfig.setSensor(SENSOR);
        termostatConfig.setTDay(12.0f);
        termostatConfig.setTNight(16.0f);
        termostatConfig.setRelayPin(GPIO_00);

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


        termostat = new Termostat();
        termostat.setRelayController(relayController);
        termostat.setTemperatureMonitor(temperatureMonitor);
        termostat.setTermostatConfig(termostatConfig);

    }

    @Test
    public void testMidDay() {
        timer = () -> LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0));
        termostat.setTimer(timer);

        termostat.checkTemperature();

        assertFalse(termostat.isHeatingOn());
    }

    @Test
    public void testMidNight() {
        timer = () -> LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0));
        termostat.setTimer(timer);

        termostat.checkTemperature();

        assertTrue(termostat.isHeatingOn());
    }

    @Test
    public void testSwitch() {
        termostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0)));
        Pin heatingPin = RaspiPin.getPinByName(termostatConfig.getRelayPin());

        termostat.checkTemperature();
        assertTrue(termostat.isHeatingOn());
        assertTrue(relayController.getPinState(heatingPin).isHigh());

        SensorValue v = temperatureMonitor.getSensorValueMap().get(SENSOR);
        v.setSensorId(SENSOR);
        v.setValue(15.0);

        termostat.checkTemperature();
        assertTrue(termostat.isHeatingOn());
        assertTrue(relayController.getPinState(heatingPin).isHigh());

        v.setValue(16.6);

        termostat.checkTemperature();
        assertFalse(termostat.isHeatingOn());
        assertFalse(relayController.getPinState(heatingPin).isHigh());

        termostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(4, 0)));
        v.setValue(16.2);
        termostat.checkTemperature();
        assertFalse(termostat.isHeatingOn());
        assertFalse(relayController.getPinState(heatingPin).isHigh());

        termostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 0)));
        v.setValue(15.6);
        termostat.checkTemperature();
        assertFalse(termostat.isHeatingOn());
        assertFalse(relayController.getPinState(heatingPin).isHigh());

        termostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 30)));
        v.setValue(15.5);
        termostat.checkTemperature();
        assertTrue(termostat.isHeatingOn());
        assertTrue(relayController.getPinState(heatingPin).isHigh());

        termostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 00)));
        v.setValue(15.5);
        termostat.checkTemperature();
        assertFalse(termostat.isHeatingOn());

        termostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(15, 00)));
        v.setValue(11.5);
        termostat.checkTemperature();
        assertTrue(termostat.isHeatingOn());
        assertTrue(relayController.getPinState(heatingPin).isHigh());

        termostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 00)));
        v.setValue(12.8);
        termostat.checkTemperature();
        assertFalse(termostat.isHeatingOn());
        assertFalse(relayController.getPinState(heatingPin).isHigh());
    }

    @Test
    public void testOnOff() {
        termostat.setTimer(() -> LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0)));
        Pin heatingPin = RaspiPin.getPinByName(termostatConfig.getRelayPin());

        termostat.checkTemperature();
        assertTrue(termostat.isHeatingOn());
        assertTrue(relayController.getPinState(heatingPin).isHigh());

        termostat.changeOn(false);
        assertFalse(relayController.getPinState(heatingPin).isHigh());

        termostat.checkTemperature();
        assertFalse(relayController.getPinState(heatingPin).isHigh());

        termostat.changeOn(true);
        assertFalse(relayController.getPinState(heatingPin).isHigh());

        termostat.checkTemperature();
        assertTrue(relayController.getPinState(heatingPin).isHigh());
    }
}
