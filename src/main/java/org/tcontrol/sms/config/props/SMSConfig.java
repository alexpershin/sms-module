package org.tcontrol.sms.config.props;

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
    private String forwardingPhone;

    @Data
    @RequiredArgsConstructor
    public static class PhoneInfo
    {
        private String phone;
        private String name;
    }
}
