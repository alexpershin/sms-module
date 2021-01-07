import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.ITemperatureMonitor;
import org.tcontrol.sms.IThermostat;
import org.tcontrol.sms.ITimer;
import org.tcontrol.sms.Thermostat;
import org.tcontrol.sms.config.props.SensorConfig;
import org.tcontrol.sms.config.props.ThermostatConfig;
import org.tcontrol.sms.dao.SensorValue;

@Configuration
public class CommandConfiguration {

  private static final String GPIO_00 = "GPIO_00";
  private static final String SENSOR = "abc";

  @Bean
  public ThermostatConfig thermostatConfig() {
    ThermostatConfig thermostatConfig = new ThermostatConfig();
    thermostatConfig.setDelta(0.5f);
    thermostatConfig.setNightBegin(60 * 60 * 23);
    thermostatConfig.setNightEnd(60 * 60 * 7);
    thermostatConfig.setSensor(SENSOR);
    thermostatConfig.setTDay(12.0f);
    thermostatConfig.setTNight(16.0f);
    thermostatConfig.setRelayPin(GPIO_00);
    return thermostatConfig;
  }

  @Bean
  public ThermostatConfig thermostatConfig2() {
    ThermostatConfig thermostatConfig = new ThermostatConfig();
    thermostatConfig.setDelta(0.5f);
    thermostatConfig.setNightBegin(60 * 60 * 23);
    thermostatConfig.setNightEnd(60 * 60 * 7);
    thermostatConfig.setSensor(SENSOR);
    thermostatConfig.setTDay(18.0f);
    thermostatConfig.setTNight(20.0f);
    thermostatConfig.setRelayPin(GPIO_00);
    return thermostatConfig;
  }

  @Bean
  public ITemperatureMonitor temperatureMonitor() {
    return new ITemperatureMonitor() {

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
  }

  @Bean
  public IRelayController relayController() {
    return new IRelayController() {
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
  }

  @Bean
  public ITimer timer() {
    return () -> LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0));
  }

  @Bean
  @Qualifier("thermostatElectro")
  public IThermostat thermostat(ThermostatConfig thermostatConfig,
      ITemperatureMonitor temperatureMonitor,
      IRelayController relayController,
      ITimer timer) {
    return new Thermostat(
        thermostatConfig,
        temperatureMonitor,
        relayController,
        timer,
        "elect"
    );
  }

  @Bean
  @Qualifier("thermostatGas")
  public IThermostat thermostat2(ThermostatConfig thermostatConfig2,
      ITemperatureMonitor temperatureMonitor,
      IRelayController relayController,
      ITimer timer) {
    return new Thermostat(
        thermostatConfig2,
        temperatureMonitor,
        relayController,
        timer,
        "gast"
    );
  }

  @Bean
  public SensorConfig sensorConfig() {
    SensorConfig sensorConfig = new SensorConfig();

    SensorConfig.SensorConfiguration sensorConfiguration = new SensorConfig.SensorConfiguration();
    sensorConfiguration.setName("дом");
    sensorConfiguration.setId(SENSOR);

    sensorConfig.setSensors(Collections.singletonList(sensorConfiguration));
    return sensorConfig;
  }
}
