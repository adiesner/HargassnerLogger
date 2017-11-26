package de.diesner.hargassner;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

@Slf4j
public class DataParser {

    private final InfluxDbForward influxDbForward;

    private static Pattern patternFloat = Pattern.compile("^[0-9.]$");
    private static Pattern patternString = Pattern.compile("[A-Za-z]");

    private int outputCounter = 0;

    private DataBuffer dataBuffer = new DataBuffer(5000);

    private enum ColType {
        F,
        S,
        I
    }

    private Map<Integer, ColType> colTypeMap = new LinkedHashMap();

    public DataParser(Properties properties) {
        this(properties, new InfluxDbForward(properties));
    }

    public DataParser(Properties properties, InfluxDbForward forwarder) {
        influxDbForward = forwarder;
        //        influxDbForward.enableHttpDebug();
    }

    public void parseData(final byte[] data, int length) {
        dataBuffer.add(data, length);

        boolean dataFound;
        do {
            int msgStart = dataBuffer.indexOf((byte) 0x0D, 0);
            int msgEnd = dataBuffer.indexOf((byte) 0x0D, msgStart + 1);

            if ((msgEnd >= 0) && (msgStart >= 0) && (msgEnd > msgStart)) {
                String message = new String(dataBuffer.read(msgStart + 1, msgEnd), Charset.forName("ISO-8859-1"));
                parseData(message);
                dataFound = true;
            } else {
                dataFound = false;
            }
        } while (dataFound);
    }

    public void parseData(String data) {
        if (data.startsWith("pm ")) {
            String[] elements = data.split(" ");
            analyzeTypes(elements);
            influxDbForward.messageReceived(new HargassnerLogEvent(HargassnerLogEvent.EventType.DATA, data.trim()));

            outputCounter++;

            if (outputCounter % 60 == 0) {
                outputTypes();
            }
        } else if (data.startsWith("tm ")) {
            influxDbForward.messageReceived(new HargassnerLogEvent(HargassnerLogEvent.EventType.TIMESTAMP, data.trim()));
        } else if (data.startsWith("z ")) {
            influxDbForward.messageReceived(new HargassnerLogEvent(HargassnerLogEvent.EventType.MESSAGE, data.trim()));
        } else {
            log.error("Unexpected data received: {}", data);
        }
    }

    private void outputTypes() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < colTypeMap.size(); i++) {
            buffer.append(colTypeMap.get(i)).append(";");
        }
        log.info(buffer.toString());
    }

    private void analyzeTypes(String[] row) {
        for (int i = 0; i < row.length; i++) {
            ColType currentType;
            currentType = colTypeMap.getOrDefault(i, ColType.I);

            Boolean isFloat = patternFloat.matcher(row[i]).matches();
            Boolean isString = patternString.matcher(row[i]).matches();

            if (isString) {
                currentType = ColType.S;
            } else if ((currentType == ColType.I) && (isFloat)) {
                currentType = ColType.F;
            }

            colTypeMap.put(i, currentType);
        }
    }

}
