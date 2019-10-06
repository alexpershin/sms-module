package org.tcontrol.sms;

import com.pi4j.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.commands.CommandResult;
import org.tcontrol.sms.commands.CommandExecutor;
import org.tcontrol.sms.commands.STATUS;
import org.tcontrol.sms.config.SMSConfig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class SMSProcessor {

    @Autowired
    ITemperatureMonitor temperatureMonitor;

    @Autowired
    private SMSConfig smsConfig;

    @Autowired
    private CommandExecutor commandExecutor;

    private Pattern patternPhone = Pattern.compile(".*_00_\\+([0-9]*)_00.txt");

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
            Files.list(path).filter(p -> !Files.isDirectory(p)).forEach(p -> {
                String fileName = p.getFileName().toString();
                Matcher matcher = patternPhone.matcher(fileName);
                if (matcher.find()) {
                    String phoneNumber = matcher.group(1);
                    CommandExecutor.CommandName commandName = readCommandFromFile(p);
                    if (commandName != null) {
                        CommandResult result = commandExecutor.run(commandName);
                        try {
                            writeAnswer(fileName, phoneNumber, result, commandName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeAnswer(String fileName, String phoneNumber, CommandResult result, CommandExecutor.CommandName command) throws IOException {
        Path path = Paths.get(smsConfig.getOutputFolder(), "OUT_" + fileName);
        Files.deleteIfExists(path);
        Files.createFile(path);
        String resultText =
                (result.getStatus() != STATUS.OK ?
                        "Command " + command.name() + " result: " + result.getStatus().name() + "\n"
                        : "")
                        + result.getMessage();
        Files.write(path, resultText.getBytes());
        log.info("Answer ready( status:{}, phone:+{}", result.getStatus(), phoneNumber);
    }

    private CommandExecutor.CommandName readCommandFromFile(Path filePath) {
        try {
            CommandExecutor.CommandName command = null;
            List<String> lines = Files.readAllLines(filePath, Charset.defaultCharset());
            if (lines.size() == 1) {
                String commandText = lines.get(0);
                command = CommandExecutor.CommandName.valueOf(StringUtil.trim(commandText).toUpperCase());
            } else {
                log.info("Error while parsing command: {}", lines);
            }
            //moving to result folder
            Path resultPath = Paths.get(smsConfig.getProcessedFolder(), filePath.getFileName().toString());
            try {
                Files.move(filePath, resultPath);
            } catch (IOException e) {
                log.info("Error while moving file " + resultPath.toString());
            }
            return command;
        } catch (IOException ex) {
            log.info("Error while reading file " + filePath.toString());
        }
        return null;
    }

}
