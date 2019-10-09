package org.tcontrol.sms.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class SMSConfig {
    private List<PhoneInfo> phones = new ArrayList<>();

    private String inputFolder;
    private String outputFolder;
    private String processedFolder;

    @Data
    @RequiredArgsConstructor
    public static class PhoneInfo
    {
        private String phone;
        private String name;
    }
}