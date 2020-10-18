package rsystems.objects;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimedEvent {
    public int eventType;
    public LocalDateTime eventExpiration;
    public Long eventGuildID;
    public Long eventUserID;


    public TimedEvent(int eventType, Long eventGuildID, Long eventUserID, String eventExpiration) {
        this.eventType = eventType;

        eventExpiration = eventExpiration.substring(0,19);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.eventExpiration = LocalDateTime.parse(eventExpiration,formatter);
        this.eventGuildID = eventGuildID;
        this.eventUserID = eventUserID;
    }

    @Override
    public String toString() {
        return "TimedEvent{" +
                "eventType=" + eventType +
                ", eventExpiration=" + eventExpiration +
                ", eventGuildID=" + eventGuildID +
                ", eventUserID=" + eventUserID +
                '}';
    }
}
