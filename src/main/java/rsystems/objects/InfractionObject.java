package rsystems.objects;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import rsystems.SherlockBot;

import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class InfractionObject {
    private String note;
    private ZonedDateTime submissionDate;
    private Long moderatorID;
    private Long userID;
    private Long messageID;
    private String userTag;
    private String moderatorTag;
    private int caseNumber;
    private int eventType;

    /*
    INFRACTION LEVEL:
    0 - warning / user note
    1 - reserved
    2 - mute
    3 - soft ban / kick
    4 - timed ban
    5 - permanent ban
     */

    public InfractionObject(Long guildID){
        // Trigger get case number from database

        try {
            this.caseNumber = SherlockBot.database.getInt("Guilds","CaseIndex","GuildID",guildID);
            SherlockBot.database.putValue("Guilds","CaseIndex","GuildID",guildID,this.caseNumber+1);

            this.submissionDate = LocalDateTime.now().atZone(ZoneId.of("UTC"));
            this.note = String.format("Moderator: please do `%sreason %05d [reason]`",SherlockBot.guildMap.get(guildID).getPrefix(),this.caseNumber);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public InfractionObject(String violation, String note, Date submissionDate, Long moderatorID) {
        this.note = note;
        //this.submissionDate = submissionDate;
        this.moderatorID = moderatorID;
    }

    @Override
    public String toString() {
        return "InfractionObject{" +
                "note='" + note + '\'' +
                ", submissionDate=" + submissionDate +
                ", submitterID=" + moderatorID +
                ", violatorID=" + userID +
                ", messageID=" + messageID +
                ", caseNumber=" + caseNumber +
                ", infractionLevel=" + eventType +
                '}';
    }

    public MessageEmbed createEmbedMessge(){
        EmbedBuilder builder = new EmbedBuilder();

        String eventType = "Mod Action";
        switch(this.eventType){
            case 0:
                builder.setColor(Color.yellow);
                eventType = "Warning";
                break;
            case 1:
            case 2:
                builder.setColor(Color.decode("#C133FF"));
                eventType = "Mute";
                break;
            case 3:
                builder.setColor(Color.decode("#FF9C33"));
                eventType = "Kick";
                break;
            case 4:
            case 5:
                builder.setColor(Color.RED);
                eventType = "Ban";
                break;

        }

        builder.setTitle(String.format("%s - Case#: %05d",eventType,this.caseNumber))
                .addField("User",String.format("%s (%d)",this.userTag,this.userID),true)
                .addField("Moderator",String.format("%s (%d)",this.moderatorTag,this.moderatorID),true)
                .addField("Reason",this.note,false);

                if(this.messageID != null){
                    builder.addField("Message ID",this.messageID.toString(),false);
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-uu   HH:mm:ss z");
                builder.setFooter("Submitted: " + this.submissionDate.format(formatter));


        return builder.build();
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getModeratorID() {
        return moderatorID;
    }

    public void setModeratorID(Long moderatorID) {
        this.moderatorID = moderatorID;
    }

    public Long getMessageID() {
        return messageID;
    }

    public void setMessageID(Long messageID) {
        this.messageID = messageID;
    }

    public int getCaseNumber() {
        return caseNumber;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getUserTag() {
        return userTag;
    }

    public void setUserTag(String userTag) {
        this.userTag = userTag;
    }

    public String getModeratorTag() {
        return moderatorTag;
    }

    public void setModeratorTag(String moderatorTag) {
        this.moderatorTag = moderatorTag;
    }

    public ZonedDateTime getSubmissionDate() {
        return submissionDate;
    }
}
