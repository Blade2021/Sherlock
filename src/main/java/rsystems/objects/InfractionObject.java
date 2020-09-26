package rsystems.objects;

import java.sql.Date;

public class InfractionObject {
    public String violation;
    public String note;
    public Date submissionDate;
    public Long submitterID;

    public InfractionObject(String violation, String note, Date submissionDate, Long submitterID) {
        this.violation = violation;
        this.note = note;
        this.submissionDate = submissionDate;
        this.submitterID = submitterID;
    }

    @Override
    public String toString() {
        return "Infraction{" +
                "violation='" + violation + '\'' +
                ", note='" + note + '\'' +
                ", submissionDate=" + submissionDate +
                ", submitterID=" + submitterID +
                '}';
    }
}
