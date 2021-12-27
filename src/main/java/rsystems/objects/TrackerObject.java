package rsystems.objects;


import java.time.LocalDateTime;

public class TrackerObject {
    private LocalDateTime submitDateTime;
    private LocalDateTime expireDateTime;
    private Integer type;

    public TrackerObject(LocalDateTime submitDateTime, LocalDateTime expireDateTime, Integer type) {
        this.submitDateTime = submitDateTime;
        this.expireDateTime = expireDateTime;
        this.type = type;
    }

    public LocalDateTime getSubmitDateTime() {
        return submitDateTime;
    }

    public LocalDateTime getExpireDateTime() {
        return expireDateTime;
    }

    public Integer getType() {
        return type;
    }
}
