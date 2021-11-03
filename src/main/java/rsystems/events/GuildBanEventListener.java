package rsystems.events;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.handlers.LogMessage;
import rsystems.objects.InfractionObject;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class GuildBanEventListener extends ListenerAdapter {

    public void onGuildBan(final GuildBanEvent event) {


        event.getGuild().retrieveAuditLogs().limit(1).type(ActionType.BAN).queueAfter(1, TimeUnit.SECONDS,success -> {
            final AuditLogEntry log = success.get(0);

            final Long modID = log.getUser().getIdLong();
            final Long userID = log.getTargetIdLong();
            final String reason = log.getReason();

            InfractionObject infractionObject = new InfractionObject(event.getGuild().getIdLong());
            infractionObject.setNote(reason);
            infractionObject.setModeratorID(modID);
            infractionObject.setUserID(userID);
            infractionObject.setUserTag(event.getUser().getAsTag());
            infractionObject.setEventType(InfractionObject.EventType.BAN);

            if(event.getGuild().getMemberById(modID) != null){
                infractionObject.setModeratorTag(event.getGuild().getMemberById(modID).getUser().getAsTag());
            }

            try {
                SherlockBot.database.insertCaseEvent(event.getGuild().getIdLong(),infractionObject);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            LogMessage.sendLogMessage(event.getGuild().getIdLong(),infractionObject.createEmbedMessge());

        });
    }
}
