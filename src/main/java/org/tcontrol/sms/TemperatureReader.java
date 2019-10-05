package org.tcontrol.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TemperatureReader {
    @Scheduled(cron = "0/30 * * * * ?")
    void inputSmsScan(){
        log.info("Reading temperature sensors...");

        log.info("Reading temperature completed");
    }
}
