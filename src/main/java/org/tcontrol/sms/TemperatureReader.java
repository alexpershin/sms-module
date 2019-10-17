package org.tcontrol.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.dao.SensorValue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

@Component
public class TemperatureReader implements ITemperatureReader {

    private static final Logger LOGGER = Logger.getLogger(TemperatureReader.class.getName());

    private static final String TEMPERATURE_PREFIX = "t=";

    @Value("${w1DevicesPath}")
    private String w1DevicesPath;

    @Value("${w1SensorInfoFileName:#{'w1_slave'}}")
    private String w1SensorInfoFileName;

    @Override
    public SensorValue loadValue(String sensorUUID) throws IOException {
        double t = readTemperatureFromFile(getFullPathToDevice(sensorUUID));
        return new SensorValue(null, System.currentTimeMillis(), t);
    }

    private Path getFullPathToDevice(String deviceFileName) {
        return FileSystems.getDefault().getPath(
                getW1DevicesPath() + "/" + deviceFileName
                        + "/" + getW1SensorInfoFileName());
    }

    private static double readTemperatureFromFile(Path pathDeviceFile) throws IOException{
        int iniPos, endPos;
        String strTemp;
        double tValue = 0;
        List<String> lines;
        try {
            lines = Files.readAllLines(pathDeviceFile, Charset.defaultCharset());
            for (String line : lines) {
                if (line.contains(TEMPERATURE_PREFIX)) {
                    iniPos = line.indexOf(TEMPERATURE_PREFIX) + 2;
                    endPos = line.length();
                    strTemp = line.substring(iniPos, endPos);
                    tValue = Double.parseDouble(strTemp) / 1000;
                }
            }
        } catch (IOException ex) {
            LOGGER.log(SEVERE, "Error while reading file " + pathDeviceFile);
            throw ex;
        }
        return tValue;
    }

    private String getW1DevicesPath() {
        return w1DevicesPath;
    }


    private String getW1SensorInfoFileName() {
        return w1SensorInfoFileName;
    }
}

