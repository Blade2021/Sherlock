package rsystems.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.handlers.LogMessage;
import rsystems.objects.InfractionObject;

import java.awt.*;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class GuildMemberEvents extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        if(SherlockBot.guildMap.get(event.getGuild().getIdLong()).getWelcomeMessageSetting() >= 1){
            welcomeUser(event.getGuild(), event.getMember());
        }

        try {
            if(SherlockBot.database.getAutoRoles(event.getGuild().getIdLong()).size() > 0){
                ArrayList<Role> rolesToAdd = new ArrayList<>();

                for(Long roleID:SherlockBot.database.getAutoRoles(event.getGuild().getIdLong())){
                    if(event.getGuild().getRoleById(roleID) != null){
                        rolesToAdd.add(event.getGuild().getRoleById(roleID));
                    }
                }

                if(rolesToAdd.size() > 0){
                    event.getGuild().modifyMemberRoles(event.getMember(),rolesToAdd).queue();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        event.getGuild().retrieveAuditLogs().limit(1).queueAfter(1, TimeUnit.SECONDS, success -> {

            final AuditLogEntry logEntry = success.get(0);

            if ((logEntry.getTargetIdLong() == event.getUser().getIdLong()) &&(logEntry.getTimeCreated().minusSeconds(5).isBefore(OffsetDateTime.now(ZoneId.of("Z"))))) {
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


    private void welcomeUser(final Guild guild, final Member member){
        final TextChannel welcomeChannel = guild.getDefaultChannel();

        if((welcomeChannel != null) && (welcomeChannel.canTalk())){

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setThumbnail(member.getEffectiveAvatarUrl());
            embedBuilder.setTitle("Welcome " + member.getEffectiveName());
            embedBuilder.setColor(Color.GREEN);
            embedBuilder.setDescription(formattedWelcomeMSG(guild,member));
            embedBuilder.setFooter("UserID: " + member.getId());

            welcomeChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            embedBuilder.clear();

        }
    }

    private String formattedWelcomeMSG(final Guild guild, final Member member) {

        String output = null;
        try {
            output = SherlockBot.database.getString("WelcomeTable","WelcomeMessage","ChildGuildID",guild.getIdLong());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(output != null) {
            output = output.replace("{user.name}", member.getEffectiveName());
            output = output.replace("{user.mention}", member.getAsMention());
            output = output.replace("{guild.name}", guild.getName());
        }

        return output;
    }

}
