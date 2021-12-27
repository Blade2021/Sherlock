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
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.handlers.LogMessage;
import rsystems.objects.InfractionObject;

import java.awt.*;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class GuildMemberEvents extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        OffsetDateTime creationDate = event.getUser().getTimeCreated().minusDays(3);
        OffsetDateTime currentDate = OffsetDateTime.now();

        final Guild.VerificationLevel verificationLevel = event.getGuild().getVerificationLevel();
        if(verificationLevel == Guild.VerificationLevel.HIGH){
            if(event.getUser().getTimeCreated().isBefore(currentDate.minusMinutes(5))){
                //todo add notification to user that they will have to wait 10 minutes before talking
            }
        }

        if (creationDate.isBefore(currentDate)) {
            // Test passed, Account is older than 3 days
        } else {
            // Test failed, Account is not older than 3 days
        }

        if(SherlockBot.guildMap.get(event.getGuild().getIdLong()).getWelcomeMessageSetting() >= 1){
            welcomeUser(event.getGuild(), event.getMember());
        }
    }


    //todo REMOVE THIS WHEN COMPILING FOR PRODUCTION
    @Override
    public void onGuildMemberUpdate(GuildMemberUpdateEvent event) {
        event.getGuild().retrieveMemberById(event.getUser().getIdLong()).queue(foundMember -> {
            Role quarantineRole = event.getGuild().getRoleById(SherlockBot.guildMap.get(event.getGuild().getIdLong()).getQuarantineRoleID());

            if(foundMember.getRoles().contains(quarantineRole)){
                event.getGuild().removeRoleFromMember(foundMember,quarantineRole).queueAfter(30, TimeUnit.SECONDS);
            }
        });
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
