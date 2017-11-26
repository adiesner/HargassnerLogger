package de.diesner.hargassner;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class InfluxDbForward extends TimerTask {

    private final Pattern numberOnly = Pattern.compile("^[0-9.]+$");
    private final Pattern digitOnly = Pattern.compile("^[0-9]+$");

    private final String remoteUri;
    private final String measurement;
    private final Client client;
    private final List<DataToPost> postDataList = new ArrayList<>();

    private final Timer timer;
    private int successCounter = 0;
    private int failCounter = 0;
    private int loopCounter = 0;

    private enum ColumnType {
        INT, FLOAT, TEXT;

        public static ColumnType of(String type) {
            for (ColumnType t : ColumnType.values()) {
                if (t.name().equalsIgnoreCase(type)) {
                    return t;
                }
            }
            return null;
        }
    }

    @Getter
    @AllArgsConstructor
    private static class Column {
        ColumnType type;
        String name;
    }

    private Map<Integer, Column> columnHashMap = new HashMap<>();

    @Getter
    @AllArgsConstructor
    private static class DataToPost {
        private String postData;
        private int retriesLeft;

        public void decRetriesLeft() {
            retriesLeft--;
        }
    }

    public InfluxDbForward(Properties properties) {
        String remoteUri = properties.getProperty("output.influxdb.remoteUri");
        String measurement = properties.getProperty("output.influxdb.measurement");

        for (Map.Entry<Object, Object> property : properties.entrySet()) {
            String key = (String) property.getKey();
            if (key.startsWith("column.")) {
                String columnNumber = key.substring(7);
                try {
                    int num = Integer.parseInt(columnNumber);
                    String value = (String) property.getValue();
                    String[] split = value.split(";");
                    if (split.length != 2) {
                        throw new IllegalArgumentException("Expected value in format <type>;<name>");
                    }
                    ColumnType type = ColumnType.of(split[0]);
                    if (type == null) {
                        throw new IllegalArgumentException("Unepected type: " + split[0]);
                    }
                    columnHashMap.put(num, new Column(type, split[1]));
                } catch (IllegalArgumentException e) {
                    log.error("Unable to parse key {}", key, e);
                }
            }
        }

        this.remoteUri = remoteUri;
        this.measurement = measurement;
        ClientConfig clientConfig = new DefaultClientConfig();
        client = Client.create(clientConfig);
        timer = new Timer();
        timer.schedule(this, 1000, 1000);
    }

    public void enableHttpDebug() {
        client.addFilter(new LoggingFilter(System.out));
    }

    public void messageReceived(HargassnerLogEvent logEvent) {
        if (logEvent.getType() == HargassnerLogEvent.EventType.DATA) {
            Map<String, String> values = extractValues(logEvent.getElements());
            addPostItem(getDataToPost(values));
        }
    }

    private DataToPost getDataToPost(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return new DataToPost("", 0);
        }
        String postData = toLineProtocol(measurement, values);
        int MAXRETRYCOUNT = 3;
        log.debug("Adding postdata: " + postData);
        return new DataToPost(postData, MAXRETRYCOUNT);
    }

    private Map<String, String> extractValues(String[] elements) {
        StringBuffer errorElements = new StringBuffer();
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 1; i < elements.length; i++) {
            Column column = columnHashMap.get(i);
            if (column != null) {
                if (column.getType() == ColumnType.TEXT) {
                    values.put(column.getName(), "\"" + elements[i] + "\"");
                } else {
                    values.put(column.getName(), elements[i]);
                }
            } else if (numberOnly.matcher(elements[i]).matches()) {
                values.put("v" + i, elements[i]);
            } else {
                errorElements.append(" v" + i + "=" + elements[i]);
            }
        }
        if (errorElements.length() > 0) {
            log.error("Unable to post elements: {}", errorElements.toString());
        }
        return values;
    }

    private String toLineProtocol(String measurement, Map<String, String> values) {
        StringBuffer data = new StringBuffer(measurement);
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (!isFirst) {
                data.append(",");
            } else {
                data.append(" ");
                isFirst = false;
            }
            data.append(entry.getKey()).append("=").append(entry.getValue());
            if (digitOnly.matcher(entry.getValue()).matches()) {
                data.append("i");
            }
        }

        data.append(" ").append(System.currentTimeMillis());
        return data.toString();
    }

    private boolean postData(DataToPost dataToPost) {
        if (dataToPost == null || dataToPost.getRetriesLeft() == 0) {
            return true;
        }
        WebResource webResource = client.resource(remoteUri);
        ClientResponse response = webResource.post(ClientResponse.class, dataToPost.getPostData());

        if (response.getStatus() != 204) {
            log.error("Failed : HTTP error code : " + response.getStatus());
            if (response.hasEntity()) {
                log.error(response.getEntity(String.class));
            }
            return false;
        }
        return true;
    }

    private DataToPost getPostItem() {
        synchronized (postDataList) {
            if (!postDataList.isEmpty()) {
                return postDataList.remove(0);
            }
        }
        return null;
    }

    private void addPostItem(DataToPost dataToPost) {
        if ((dataToPost != null) && (dataToPost.getRetriesLeft() > 0)) {
            synchronized (postDataList) {
                postDataList.add(dataToPost);
            }
        }
    }

    @Override
    public void run() {
        List<DataToPost> failedRequests = new ArrayList<>();
        DataToPost dataToPost;
        do {
            dataToPost = getPostItem();
            if (dataToPost != null && dataToPost.getRetriesLeft() > 0) {
                boolean success = false;
                try {
                    success = postData(dataToPost);
                    if (success) {
                        successCounter++;
                    }
                } catch (Exception e) {
                    log.error("Exception while posting: " + e.getMessage(), e);
                }
                if ((!success) && (dataToPost.getRetriesLeft() > 0)) {
                    failedRequests.add(dataToPost);
                    failCounter++;
                }
            }
        } while (dataToPost != null);

        for (DataToPost retry : failedRequests) {
            retry.decRetriesLeft();
            addPostItem(retry);
        }

        loopCounter++;
        if (loopCounter % 60 == 0) {
            log.debug("Added {} entries! Failed: {}", successCounter, failCounter);
        }
    }
}
