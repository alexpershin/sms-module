package org.tcontrol.sms;

import com.pi4j.util.StringUtil;
import lombok.AllArgsConstructor;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@AllArgsConstructor
public class SMSProcessor {

    ITemperatureMonitor temperatureMonitor;

    private SMSConfig smsConfig;

    private CommandExecutor commandExecutor;

    final private static Pattern patternPhone = Pattern.compile(".*_00_\\+([0-9]*)_00.txt");

    @Scheduled(cron = "${sms-config.schedule}")
    void inputSmsScan() {
        log.info("Scanning input SMS to process..");
        processInputFolder();
        log.info("Input SMS processing completed");
    }

    private void processInputFolder() {
        final String inputFolderName = smsConfig.getInputFolder();
        Path path = Paths.get(inputFolderName);
        try {
            List<Path> files;

            try (Stream<Path> stream = Files.list(path)) {// to make sure that directory closed
                files = stream.collect(Collectors.toList());
            }
            files.stream().filter(p -> !Files.isDirectory(p)).forEach(p -> {
                String fileName = p.getFileName().toString();
                Matcher matcher = patternPhone.matcher(fileName);
                if (matcher.find()) {
                    String phoneNumber = matcher.group(1);
                    boolean inList = smsConfig.getPhones().stream()
                            .anyMatch(phone -> phone.getPhone().equals(phoneNumber));

                    if (inList) {
                        String commandName = readCommandFromFile(p);
                        if (commandName != null) {
                            CommandResult result = commandExecutor.run(commandName);
                            try {
                                writeAnswer(fileName, phoneNumber, result, commandName);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                //moving to result folder
                Path resultPath = Paths.get(smsConfig.getProcessedFolder(), p.getFileName().toString());
                try {
                    Files.move(p, resultPath);
                } catch (IOException e) {
                    log.info("Error while moving file " + resultPath.toString());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeAnswer(String fileName, String phoneNumber, CommandResult result, String command) throws IOException {
        Path path = Paths.get(smsConfig.getOutputFolder(), "OUT+" + phoneNumber + "." + fileName);
        Files.deleteIfExists(path);
        Files.createFile(path);
        String resultText =
                (result.getStatus() != STATUS.OK ?
                        "Command " + command + " result: " + result.getStatus().name() + "\n"
                        : "")
                        + result.getMessage();
        Files.write(path, resultText.getBytes());
        log.info("Answer ready (status:{}, phone:+{})", result.getStatus(), phoneNumber);
    }

    private String readCommandFromFile(Path filePath) {
        try {
            String command = null;
            List<String> lines = Files.readAllLines(filePath, Charset.defaultCharset());
            if (lines.size() == 1) {
                String commandText = lines.get(0);
                command = StringUtil.trim(commandText).toUpperCase();
            } else {
                log.info("Error while found command: {}", lines);
            }
            return command;
        } catch (IOException ex) {
            log.info("Error while reading file " + filePath.toString());
        }
        return null;
    }

}
