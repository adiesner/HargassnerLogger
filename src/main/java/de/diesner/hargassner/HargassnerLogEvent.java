package de.diesner.hargassner;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HargassnerLogEvent {

    public enum EventType {
        TIMESTAMP,
        DATA,
        MESSAGE,
    }

    private EventType type;
    private String text;

    public String[] getElements() {
        return text.split(" ");
    }
}
