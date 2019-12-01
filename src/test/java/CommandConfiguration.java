import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tcontrol.sms.*;
import org.tcontrol.sms.config.SensorConfig;
import org.tcontrol.sms.config.ThermostatConfig;
import org.tcontrol.sms.dao.SensorValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    public ITemperatureMonitor temperatureMonitor() {
        ITemperatureMonitor temperatureMonitor = new ITemperatureMonitor() {

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
        return temperatureMonitor;
    }

    @Bean
    public IRelayController relayController() {
        IRelayController relayController = new IRelayController() {
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
        return relayController;
    }

    @Bean
    public ITimer timer() {
        return () -> LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0));
    }

    @Bean
    public IThermostat thermostat(ThermostatConfig thermostatConfig,
                                  ITemperatureMonitor temperatureMonitor,
                                  IRelayController relayController,
                                  ITimer timer) {
        IThermostat thermostat = new Thermostat(
                thermostatConfig,
                temperatureMonitor,
                relayController,
                timer
        );
        return thermostat;
    }

    @Bean
    public SensorConfig sensorConfig() {
        SensorConfig sensorConfig = new SensorConfig();

        SensorConfig.SensorConfiguration sensorConfiguration = new SensorConfig.SensorConfiguration();
        sensorConfiguration.setName("дом");
        sensorConfiguration.setId(SENSOR);

        sensorConfig.setSensors(Arrays.asList(sensorConfiguration));
        return sensorConfig;
    }
}
