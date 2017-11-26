package de.diesner.hargassner;

import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class HargassnerLogger {

    private Properties properties;
    private SerialPort serialPort;

    public static void main(String[] args) {
        HargassnerLogger hargassnerLogger = new HargassnerLogger();
        try {
            if (hargassnerLogger.initialize(args)) {
                hargassnerLogger.run();
                hargassnerLogger.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run() {
        DataParser dataParser = new DataParser(properties);
        byte[] readBuffer = new byte[500];
        try {
            while (true) {
                while (serialPort.bytesAvailable() == 0) {
                    Thread.sleep(100);
                }

                int numRead = serialPort.readBytes(readBuffer, serialPort.bytesAvailable() > readBuffer.length ? readBuffer.length : serialPort.bytesAvailable());
                if (numRead > 0) {
                    dataParser.parseData(readBuffer, numRead);
                }
            }
        } catch (Exception e) {
            log.error("Exception in main task: {}", e.getMessage(), e);
        }
        serialPort.closePort();
    }

    private void close() {
        if (serialPort != null) {
            serialPort.closePort();
        }
    }

    private boolean initialize(String[] args) throws IOException {
        properties = new Properties();

        InputStream is;
        if (args.length == 1) {
            log.debug("Loading properties file: " + args[0]);
            is = new FileInputStream(args[0]);
        } else {
            is = getClass().getResourceAsStream("/application.properties");
        }
        try {
            properties.load(is);
        } finally {
            is.close();
        }

        String port = properties.getProperty("serial.port");
        log.info("Opening serial port: " + port);
        serialPort = SerialPort.getCommPort(port);
        serialPort.openPort();
        return true;
    }

}
