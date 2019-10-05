package org.tcontrol.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SMSProcessor {
    @Scheduled(cron = "0/10 * * * * ?")
    void inputSmsScan(){
        log.info("Scanning input SMS to process..");

        log.info("Input SMS processing completed");
    }

}
