package org.tcontrol.sms;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tcontrol.sms.dao.SensorValue;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class CSVStatisticsWriter {

    @Value("${dataFile}")
    private String dataFile;

    public void write(SensorValue[] values) {
        boolean fileExist = Files.exists(Paths.get(dataFile));
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(dataFile, true), CSVFormat.EXCEL)) {
            if (!fileExist) {
                printer.printRecord("sensorId", "timestamp", "value");
            }
            for (SensorValue value : values) {
                if (value != null) {
                    printer.printRecord(value.getSensorId(), value.getTimestamp(), value.getValue());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
