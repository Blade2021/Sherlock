package rsystems.events;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.handlers.LogMessage;
import rsystems.objects.InfractionObject;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class GuildMemberEvents extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        OffsetDateTime creationDate = event.getUser().getTimeCreated().minusDays(3);
        OffsetDateTime currentDate = OffsetDateTime.now();

        if (creationDate.isBefore(currentDate)) {
            // Test passed, Account is older than 3 days
        } else {
            // Test failed, Account is not older than 3 days
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        event.getGuild().retrieveAuditLogs().limit(1).queueAfter(1, TimeUnit.SECONDS, success -> {

            final AuditLogEntry logEntry = success.get(0);

            if ((logEntry.getTargetIdLong() == event.getUser().getIdLong()) &&(logEntry.getTimeCreated().minusSeconds(2).isBefore(OffsetDateTime.now(ZoneId.of("Z"))))) {
                //Logged within acceptable amount of time.
                if((logEntry.getType().equals(ActionType.KICK) || (logEntry.getType().equals(ActionType.BAN)))){


                    final Long modID = logEntry.getUser().getIdLong();
                    final Long userID = logEntry.getTargetIdLong();
                    final String reason = logEntry.getReason();

                    InfractionObject infractionObject = new InfractionObject(event.getGuild().getIdLong());

                    if(reason != null){
                        infractionObject.setNote(reason);
                    }
                    infractionObject.setModeratorID(modID);
                    infractionObject.setUserID(userID);
                    infractionObject.setUserTag(event.getUser().getAsTag());

                    if(logEntry.getType().equals(ActionType.BAN)){
                        infractionObject.setEventType(InfractionObject.EventType.BAN);
                    } else {
                        infractionObject.setEventType(InfractionObject.EventType.KICK);
                    }

                    if(event.getGuild().getMemberById(modID) != null){
                        infractionObject.setModeratorTag(event.getGuild().getMemberById(modID).getUser().getAsTag());
                    }

                    try {
                        SherlockBot.database.insertCaseEvent(event.getGuild().getIdLong(),infractionObject);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    LogMessage.sendLogMessage(event.getGuild().getIdLong(),infractionObject);
                }

            }

        });
    }
}
