package de.diesner.hargassner;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static de.diesner.hargassner.HargassnerLogEvent.EventType.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DataParserTest {

    DataParser dataParser;
    InfluxDbForward influxDbForward;
    Properties properties;

    @Before
    public void before() {
        influxDbForward = Mockito.mock(InfluxDbForward.class);
        properties = new Properties();
        dataParser = new DataParser(properties, influxDbForward);
    }

    @Test
    public void testParser() throws Exception {
        loadFileToParser(dataParser, "/raw_ttyUSB0.txt", 100);

        ArgumentCaptor<HargassnerLogEvent> argumentCaptor = ArgumentCaptor.forClass(HargassnerLogEvent.class);
        verify(influxDbForward, times(65)).messageReceived(argumentCaptor.capture());

        List<HargassnerLogEvent> events = argumentCaptor.getAllValues();
        assertThat("Number Events", events.size(), equalTo(65));

        List<HargassnerLogEvent> msgEvents = filter(events, MESSAGE);
        List<HargassnerLogEvent> timeEvents = filter(events, TIMESTAMP);
        List<HargassnerLogEvent> dataEvents = filter(events, DATA);

        assertThat("Message Count", msgEvents.size(), equalTo(0));
        assertThat("Timestamp Count", timeEvents.size(), equalTo(33));
        assertThat("Data Count", dataEvents.size(), equalTo(32));

        // the first time element was skipped since the separator was 0x0A instead of 0x0D in my recording.
        // I am not really sure why...
        assertThat("First time event", timeEvents.get(0).getText(), equalTo("tm 2017-11-26 12:26:19"));
        assertThat("Second time event", timeEvents.get(1).getText(), equalTo("tm 2017-11-26 12:26:20"));
        assertThat("Second Last time event", timeEvents.get(31).getText(), equalTo("tm 2017-11-26 12:26:39"));
        assertThat("Last time event", timeEvents.get(32).getText(), equalTo("tm 2017-11-26 12:26:40"));

        assertThat("First data event", dataEvents.get(0).getElements()[7], equalTo("30.7"));
        assertThat("Last data event", dataEvents.get(31).getElements()[7], equalTo("30.5"));
    }

    @Test
    public void testParserSecondRecording() throws Exception {
        loadFileToParser(dataParser, "/raw_ttyUSB0_withText.txt", 500);

        ArgumentCaptor<HargassnerLogEvent> argumentCaptor = ArgumentCaptor.forClass(HargassnerLogEvent.class);
        verify(influxDbForward, times(668)).messageReceived(argumentCaptor.capture());

        List<HargassnerLogEvent> events = argumentCaptor.getAllValues();
        assertThat("Number Events", events.size(), equalTo(668));

        List<HargassnerLogEvent> msgEvents = filter(events, MESSAGE);
        List<HargassnerLogEvent> timeEvents = filter(events, TIMESTAMP);
        List<HargassnerLogEvent> dataEvents = filter(events, DATA);

        assertThat("Message Count", msgEvents.size(), equalTo(5));
        assertThat("Timestamp Count", timeEvents.size(), equalTo(332));
        assertThat("Data Count", dataEvents.size(), equalTo(331));

        assertThat("First Msg Event Text", msgEvents.get(0).getText(), equalTo("z 13:03:47 Kessel Start"));
        assertThat("Last Msg Event Text", msgEvents.get(4).getText(), equalTo("z 13:03:48 Lambdaheizung ein"));
    }

    @SneakyThrows
    private void loadFileToParser(DataParser dataparser, String filename, int bufferSize) {
        InputStream rawStream = DataParserTest.class.getResourceAsStream(filename);

        byte[] buffer = new byte[bufferSize];
        while (true) {
            int r = rawStream.read(buffer);
            if (r == -1) break;
            dataparser.parseData(buffer, r);
        }
    }

    private List<HargassnerLogEvent> filter(List<HargassnerLogEvent> events, HargassnerLogEvent.EventType type) {
        return events.stream().filter(e -> e.getType() == type).collect(Collectors.toList());
    }


}