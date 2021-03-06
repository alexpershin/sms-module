package org.tcontrol.sms.config;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class SMSConfig {
    private List<PhoneInfo> phones = new ArrayList<>();

    private String inputFolder;
    private String outputFolder;
    private String processedFolder;
    private String heatingPin;
    private String forwardingPhone;

    @PostConstruct
    public void postConstruct(){
        heatingPin();
    }

    @Data
    @RequiredArgsConstructor
    public static class PhoneInfo
    {
        private String phone;
        private String name;
    }

    public Pin heatingPin(){
        return RaspiPin.getPinByName(heatingPin);
    }
}
