package org.tcontrol.sms;

import com.pi4j.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.commands.CommandResult;
import org.tcontrol.sms.commands.CommandExecutor;
import org.tcontrol.sms.commands.STATUS;
import org.tcontrol.sms.config.props.SMSConfig;

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

    private SMSConfig smsConfig;

    private CommandExecutor commandExecutor;

    final private static Pattern patternPhone = Pattern.compile(".*_00_\\+([0-9]*)_00.txt");
    final private static Pattern patternTime = Pattern.compile("IN([0-9]*)_([0-9]*)_00_.*_(0[0-9]).txt");

    @Scheduled(cron = "${sms-config.schedule}")
    public void inputSmsScan() {
        log.info("Scanning input SMS to process..");
        processInputFolderSMS();
        log.info("Input SMS processing completed");
    }

    private void processInputFolderSMS() {
        final String inputFolderName = smsConfig.getInputFolder();
        Path path = Paths.get(inputFolderName);

        try (Stream<Path> stream = Files.list(path)) {
            List<Path> files = stream.collect(Collectors.toList());
            for (Path incomingFile : files) {
                if (!Files.isDirectory(incomingFile)) {
                    processIncomingSmsFile(incomingFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processIncomingSmsFile(Path inconingSmsFilePath) throws IOException {
        final String fileName = inconingSmsFilePath.getFileName().toString();
        final Matcher matcher = patternPhone.matcher(fileName);
        boolean inList = false;
        String incomingPhoneNumber = "?";

        //processing command
        if (matcher.find()) {
            incomingPhoneNumber = matcher.group(1);
            final String incomingPhoneNumberMatch = incomingPhoneNumber;
            inList = smsConfig.getPhones().stream()
                    .anyMatch(phone -> phone.getPhone().equals(incomingPhoneNumberMatch));

            if (inList) {
                String commandName = readCommandFromFile(inconingSmsFilePath);
                if (commandName != null) {
                    CommandResult result = commandExecutor.run(commandName);
                    writeCommandAnswer(fileName, incomingPhoneNumber, result, commandName);
                }
            }

        }

        //forwarding
        if (!inList) {
            String text = "Forwarding from (" + incomingPhoneNumber + "):"
                    + "\n" + readMessageTextFromFile(inconingSmsFilePath);
            forward(fileName, incomingPhoneNumber, smsConfig.getForwardingPhone(), text);
        }

        //moving to result folder
        Path resultPath = Paths.get(smsConfig.getProcessedFolder(), inconingSmsFilePath.getFileName().toString());
        Files.move(inconingSmsFilePath, resultPath);
    }

    private void writeCommandAnswer(String fileName, String phoneNumber, CommandResult result, String command)
            throws IOException {

        String resultText =
                (result.getStatus() != STATUS.OK ?
                        "Command " + command + " result: " + result.getStatus().name() + "\n"
                        : "")
                        + result.getMessage();

        writeOutSmsFile(fileName, phoneNumber, resultText);
        log.info("Answer ready (status:{}, phone:+{})", result.getStatus(), phoneNumber);
    }

    private void forward(String fileName, String incomingNumber, String forwardingPhoneNumber, String resultText)
            throws IOException {
        writeOutSmsFile(fileName, forwardingPhoneNumber, resultText);
        log.info("Message forwarded (from:{}, to:+{})", incomingNumber, forwardingPhoneNumber);
    }

    private void writeOutSmsFile(String fileName, String phoneNumber, String resultText) throws IOException {

        final Matcher matcher = patternTime.matcher(fileName);
        String date = "";
        String time = "";
        String suffix = "";

        //processing command
        if (matcher.find()) {
            date = matcher.group(1);
            time = matcher.group(2);
            suffix = matcher.group(3);
        }

        String outFileName = "OUT+" + phoneNumber + "." + date + time + suffix + ".txt";
        Path path = Paths.get(smsConfig.getOutputFolder(), outFileName);
        Files.deleteIfExists(path);
        Files.createFile(path);

        Files.write(path, resultText.getBytes());
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

    private String readMessageTextFromFile(Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath, Charset.defaultCharset());
            return lines.stream().reduce((s1, s2) -> s1 + "\n" + s2).orElse("");
        } catch (IOException ex) {
            log.info("Error while reading file " + filePath.toString());
        }
        return null;
    }

}
