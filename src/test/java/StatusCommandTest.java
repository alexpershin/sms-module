import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.ITemperatureMonitor;
import org.tcontrol.sms.ITimer;
import org.tcontrol.sms.IVoltageMonitor;
import org.tcontrol.sms.IVoltageMonitor.VoltageResult;
import org.tcontrol.sms.Thermostat;
import org.tcontrol.sms.VoltageMonitor;
import org.tcontrol.sms.commands.CommandResult;
import org.tcontrol.sms.commands.StatusCommand;
import org.tcontrol.sms.config.props.SMSConfig;
import org.tcontrol.sms.config.props.SensorConfig;
import org.tcontrol.sms.config.props.ThermostatConfig;
import org.tcontrol.sms.dao.SensorValue;

public class StatusCommandTest {

  private static final String GPIO_00 = "GPIO_00";
  private static final String GPIO_01 = "GPIO_01";
  private static final String SENSOR = "abc";
  private Thermostat thermostat1;
  private Thermostat thermostat2;
  private ThermostatConfig thermostatConfig;
  private ThermostatConfig thermostatConfig2;
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

    thermostatConfig2 = new ThermostatConfig();
    thermostatConfig2.setDelta(0.5f);
    thermostatConfig2.setNightBegin(60 * 60 * 23);
    thermostatConfig2.setNightEnd(60 * 60 * 7);
    thermostatConfig2.setSensor(SENSOR);
    thermostatConfig2.setTDay(18.0f);
    thermostatConfig2.setTNight(20.0f);
    thermostatConfig2.setRelayPin(GPIO_01);

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

    timer = mock(ITimer.class);
    when(timer.getCurrentTime()).thenReturn(LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0)));

    thermostat1 = new Thermostat(
        thermostatConfig,
        temperatureMonitor,
        relayController,
        timer,
        "termo1"
    );

    thermostat2 = new Thermostat(
        thermostatConfig2,
        temperatureMonitor,
        relayController,
        timer,
        "termo2"
    );

    sensorConfig = new SensorConfig();

    SensorConfig.SensorConfiguration sensorConfiguration = new SensorConfig.SensorConfiguration();
    sensorConfiguration.setName("дом");
    sensorConfiguration.setId(SENSOR);

    sensorConfig.setSensors(Collections.singletonList(sensorConfiguration));

    final IVoltageMonitor voltageMonitor = ()->Arrays.asList(new VoltageResult("socket", 220.1, "V"));

    statusCommand = new StatusCommand(temperatureMonitor, sensorConfig,
        relayController, thermostat1, thermostat2, voltageMonitor);

  }

  @Test
  public void testStatus() {
    when(timer.getCurrentTime()).thenReturn(LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0)));

    thermostat1.checkTemperature();
    thermostat2.checkTemperature();

    CommandResult result = statusCommand.run();
    assertTrue(result.getMessage().contains("termo1(12.0): ON, heating: ON"));
    assertTrue(result.getMessage().contains("termo2(18.0): ON, heating: ON"));
    assertTrue(result.getMessage().contains("socket: 220.1V"));
  }
}
