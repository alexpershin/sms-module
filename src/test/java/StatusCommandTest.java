import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import org.junit.Before;
import org.junit.Test;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.ITemperatureMonitor;
import org.tcontrol.sms.ITimer;
import org.tcontrol.sms.Thermostat;
import org.tcontrol.sms.commands.CommandResult;
import org.tcontrol.sms.commands.StatusCommand;
import org.tcontrol.sms.config.SensorConfig;
import org.tcontrol.sms.config.ThermostatConfig;
import org.tcontrol.sms.dao.SensorValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class StatusCommandTest {

    private static final String GPIO_00 = "GPIO_00";
    private static final String SENSOR = "abc";
    private Thermostat thermostat;
    private ThermostatConfig thermostatConfig;
    private ITemperatureMonitor temperatureMonitor;
    private IRelayController relayController;
    private ITimer timer;
    private StatusCommand statusCommand;
    private SensorConfig sensorConfig;

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

        sensorConfig = new SensorConfig();

        SensorConfig.SensorConfiguration sensorConfiguration = new SensorConfig.SensorConfiguration();
        sensorConfiguration.setName("дом");
        sensorConfiguration.setId(SENSOR);

        sensorConfig.setSensors(Arrays.asList(sensorConfiguration));

        statusCommand = new StatusCommand(temperatureMonitor, sensorConfig, relayController, thermostat);

    }

    @Test
    public void testStatus() {
        timer = () -> LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0));
        thermostat.setTimer(timer);

        thermostat.checkTemperature();

        CommandResult result = statusCommand.run();
        assertTrue(result.getMessage().contains("Thermostat(12.0): ON"));
        //System.out.println(result.getMessage());
    }
}
