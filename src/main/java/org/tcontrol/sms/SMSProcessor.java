package org.tcontrol.sms;

import com.pi4j.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.commands.Commands;
import org.tcontrol.sms.config.SMSConfig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class SMSProcessor {

    @Autowired
    ITemperatureMonitor temperatureMonitor;

    @Autowired
    private SMSConfig smsConfig;

    @Scheduled(cron = "0/10 * * * * ?")
    void inputSmsScan() {
        log.info("Scanning input SMS to process..");
        processInputFolder();
        log.info("Input SMS processing completed");
    }

    private void processInputFolder() {
        final String inputFolderName = smsConfig.getInputFolder();
        Path path = Paths.get(inputFolderName);
        try {
            Files.list(path).forEach(p -> {
                Commands c = readCommandFromFile(p);
            });
//                    .map(this::readCommandFromFile)
//                    .filter(Objects::nonNull)
//                    .map(Commands::run);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Commands readCommandFromFile(Path filePath) {
        try {
            Commands command = null;
            List<String> lines = Files.readAllLines(filePath, Charset.defaultCharset());
            if (lines.size() == 1) {
                String commandText = lines.get(0);
                command = Commands.valueOf(StringUtil.trim(commandText).toUpperCase());
            } else {
                log.info("Error while parsing command: {}", lines);
            }
            //moving to result folder
            Path resultPath = Paths.get(smsConfig.getProcessedFolder(), filePath.getFileName().toString());
            try {
                Files.move(filePath, resultPath);
            }catch(IOException e){
                log.info("Error while moving file " + resultPath.toString());
            }
            return command;
        } catch (IOException ex) {
            log.info("Error while reading file " + filePath.toString());
        }
        return null;
    }

}
