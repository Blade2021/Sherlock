package rsystems.events;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.handlers.LogMessage;
import rsystems.objects.InfractionObject;

public class GuildBanEventListener extends ListenerAdapter {

    public void onGuildBan(final GuildBanEvent event) {
        event.getGuild().retrieveAuditLogs().limit(1).type(ActionType.BAN).queue(success -> {
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

            LogMessage.sendLogMessage(event.getGuild().getIdLong(),infractionObject.createEmbedMessge());

        });
    }
}
