package org.tcontrol.sms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tcontrol.sms.IRelayController;
import org.tcontrol.sms.ITemperatureMonitor;
import org.tcontrol.sms.IThermostat;
import org.tcontrol.sms.ITimer;
import org.tcontrol.sms.Thermostat;
import org.tcontrol.sms.config.props.ButtonConfig;
import org.tcontrol.sms.config.props.SMSConfig;
import org.tcontrol.sms.config.props.SensorConfig;
import org.tcontrol.sms.config.props.ThermostatConfig;
import org.tcontrol.sms.config.props.VoltageMonitorConfig;

@Configuration
@EnableScheduling
public class AppConfig {

  @Bean
  @ConfigurationProperties(prefix = "sensor-config")
  public SensorConfig sensorConfig() {
    return new SensorConfig();
  }

  @Bean
  @ConfigurationProperties(prefix = "sms-config")
  public SMSConfig smsConfig() {
    return new SMSConfig();
  }

  @Bean
  @ConfigurationProperties(prefix = "thermostat-electro")
  public ThermostatConfig thermostatElectroConfig() {
    return new ThermostatConfig();
  }

  @Bean
  @ConfigurationProperties(prefix = "thermostat-gas")
  public ThermostatConfig thermostatGasConfig() {
    return new ThermostatConfig();
  }

  @Bean
  @ConfigurationProperties(prefix = "voltage-monitor")
  public VoltageMonitorConfig voltageMonitorConfig() {
    return new VoltageMonitorConfig();
  }

  @Bean
  @ConfigurationProperties(prefix = "button-controller")
  public ButtonConfig buttonConfig() {
    return new ButtonConfig();
  }

  @Bean
  public IThermostat thermostatElectro(
      final ThermostatConfig thermostatElectroConfig,
      final ITemperatureMonitor temperatureMonitor,
      final IRelayController relayController,
      final ITimer timer) {
    return new Thermostat(thermostatElectroConfig, temperatureMonitor, relayController,
        timer, "termo1(electro)");
  }

  @Bean
  public IThermostat thermostatGas(
      final ThermostatConfig thermostatGasConfig,
      final ITemperatureMonitor temperatureMonitor,
      final IRelayController relayController,
      final ITimer timer) {
    return new Thermostat(thermostatGasConfig, temperatureMonitor, relayController,
        timer, "termo2(gas)");
  }
}
