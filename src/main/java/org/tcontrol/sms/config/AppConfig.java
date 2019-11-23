package org.tcontrol.sms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AppConfig {

    @Bean
    @ConfigurationProperties(prefix = "sensor-config")
    SensorConfig sensorConfig() {
        return new SensorConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "sms-config")
    SMSConfig smsConfig() {
        return new SMSConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "termostat")
    TermostatConfig termostatConfig() {
        return new TermostatConfig();
    }
}
