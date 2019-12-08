import org.junit.Before;
import org.junit.Test;
import org.tcontrol.sms.SMSProcessor;
import org.tcontrol.sms.commands.CommandExecutor;
import org.tcontrol.sms.config.SMSConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

public class SMSProcessorTest {
    public static final String FORWARDING_PHONE = "79221111111";
    private SMSProcessor smsProcessor;
    private SMSConfig smsConfig;

    @Before
    public void before() {
        smsConfig = new SMSConfig();
        smsConfig.setForwardingPhone(FORWARDING_PHONE);
        smsConfig.setInputFolder("src/test/resources/messages/inbox");
        smsConfig.setProcessedFolder("src/test/resources/messages/processed");
        smsConfig.setOutputFolder("src/test/resources/messages/outbox");

        CommandExecutor commandExecutor = new CommandExecutor(
                () -> null, () -> null, () -> null, () -> null, () -> null);

        smsProcessor = new SMSProcessor(smsConfig, commandExecutor);
    }

    @Test
    public void emptyTest() {
        smsProcessor.inputSmsScan();
    }

    @Test
    public void forwardingTest() throws IOException {
        String inputFileName = "abc.txt";
        Path inputFilePath = Paths.get(smsConfig.getInputFolder(), inputFileName);
        Files.write(inputFilePath, "abc".getBytes());
        smsProcessor.inputSmsScan();
        try (Stream<Path> stream = Files.list(Paths.get(smsConfig.getInputFolder()))) {
            List<Path> files = stream.collect(Collectors.toList());
            assertTrue(files.size() == 0);
        }
        final String expectedOutfileName = "OUT+" + FORWARDING_PHONE + ".abc.txt";
        try (Stream<Path> stream = Files.list(Paths.get(smsConfig.getOutputFolder()))) {
            Optional<Path> fileOptional = stream.filter(path -> !Files.isDirectory(path))
                    .filter(path -> path.endsWith(expectedOutfileName)).findFirst();
            assertTrue(fileOptional.isPresent());
        } finally {
            Files.delete(Paths.get(smsConfig.getOutputFolder(), expectedOutfileName));
            Files.delete(Paths.get(smsConfig.getProcessedFolder(), inputFileName));
        }
    }

}
