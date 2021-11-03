package rsystems.events;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.util.concurrent.TimeUnit;

public class GuildNicknameListener extends ListenerAdapter {

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        if (event.getJDA().getSelfUser().getIdLong() == event.getUser().getIdLong()) {
            if (SherlockBot.database.getInt("SubscriberTable", "SubLevel", "ChildGuildID", event.getGuild().getIdLong()) >= 1) {
                return;
            } else {
                event.getGuild().retrieveAuditLogs().limit(5).type(ActionType.MEMBER_UPDATE).queueAfter(1, TimeUnit.SECONDS, success -> {

                    for(AuditLogEntry logEntry:success){

                        if((logEntry.getTargetIdLong() == SherlockBot.jda.getSelfUser().getIdLong()) && (logEntry.getUser().getIdLong() != SherlockBot.bot.getIdLong())){

                            logEntry.getUser().openPrivateChannel().queue(privateChannel -> {
                                privateChannel.sendMessage("You cannot change the name of this bot without being a subscriber.\n\nThe bot will automatically leave your server if unable to return its nickname.\n\nMultiple attempts will also go against our TOS and will remove your ability to use our Bot for your server").queue();

                            });

                            break;

                        }

                    }

                    event.getGuild().getSelfMember().modifyNickname("Watson").reason("Only subscribers are authorized to change this bot's nickname.").queueAfter(10,TimeUnit.SECONDS);
                });
            }
        }
    }
}
