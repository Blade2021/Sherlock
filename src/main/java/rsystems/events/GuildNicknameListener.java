package rsystems.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.handlers.LogMessage;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class GuildNicknameListener extends ListenerAdapter {

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        if (event.getJDA().getSelfUser().getIdLong() == event.getUser().getIdLong()) {
            try {
                Integer result = SherlockBot.database.getInt("SubscriberTable", "SubLevel", "ChildGuildID", event.getGuild().getIdLong());

                if ((result != null) && (result >= 1)) {
                    return;
                } else {

                    if ((event.getUser().getIdLong() == SherlockBot.jda.getSelfUser().getIdLong()) && (event.getNewNickname() != null)) {

                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setTitle("System Warning");
                        builder.setDescription("You cannot change the name of this bot without being a subscriber.\n\nThe bot will automatically leave your server if unable to return its nickname.\n\nMultiple attempts will also go against our TOS and will remove your ability to use our Bot for your server");
                        builder.setColor(SherlockBot.getColor(SherlockBot.colorType.GENERIC));
                        builder.setThumbnail(SherlockBot.bot.getEffectiveAvatarUrl());

                        event.getGuild().retrieveAuditLogs().limit(5).type(ActionType.MEMBER_UPDATE).queueAfter(1, TimeUnit.SECONDS, success -> {

                            for (AuditLogEntry logEntry : success) {

                                if ((logEntry.getTargetIdLong() == SherlockBot.jda.getSelfUser().getIdLong()) && (logEntry.getUser().getIdLong() != SherlockBot.bot.getIdLong())) {

                                    logEntry.getUser().openPrivateChannel().queue(privateChannel -> {

                                        EmbedBuilder privbuilder = new EmbedBuilder();
                                        privbuilder.setTitle("System Warning");
                                        privbuilder.setDescription("You cannot change the name of this bot without being a subscriber.\n\nThe bot will automatically leave your server if unable to return its nickname.\n\nMultiple attempts will also go against our TOS and will remove your ability to use our Bot for your server");
                                        privbuilder.setColor(SherlockBot.getColor(SherlockBot.colorType.GENERIC));
                                        privbuilder.setThumbnail(SherlockBot.bot.getEffectiveAvatarUrl());
                                        privateChannel.sendMessageEmbeds(privbuilder.build()).queue();
                                        privbuilder.clear();

                                    });

                                    break;

                                }

                            }
                        });

                        LogMessage.sendLogMessage(event.getGuild().getIdLong(),builder.build());
                        builder.clear();

                        event.getGuild().getSelfMember().modifyNickname("Watson").reason("Only subscribers are authorized to change this bot's nickname.").queueAfter(2, TimeUnit.SECONDS);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
