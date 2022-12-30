package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import rsystems.SherlockBot;
import rsystems.objects.SlashCommand;

import java.awt.*;
import java.util.ArrayList;

public class Verify extends SlashCommand {
    @Override
    public Permission getDiscordPermission() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandInteractionEvent event) {
        event.deferReply(isEphemeral()).queue();
        Long guildID = event.getGuild().getIdLong();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.decode("#1AE8F5"));
        builder.setTitle("Sherlock Verification");
        builder.setThumbnail(SherlockBot.bot.getAvatarUrl());

        /*
        LogChannelID
        Mute Role ID
        Permissions
         */

        Guild.VerificationLevel verifyLevel = event.getGuild().getVerificationLevel();
        builder.appendDescription(String.format("**Verify Level**\n" +
                "%s\n\n",verifyLevel));


        String muteRoleStatus = "Disabled";
        Role muteRole = null;

        Long quarantineRoleID = SherlockBot.guildMap.get(guildID).getQuarantineRoleID();

        if((quarantineRoleID != null) && (quarantineRoleID > 0)){
            if(event.getGuild().getRoleById(SherlockBot.guildMap.get(guildID).getQuarantineRoleID()) != null){
                muteRole = event.getGuild().getRoleById(SherlockBot.guildMap.get(guildID).getQuarantineRoleID());
                muteRoleStatus = "Enabled";
            }
        } else {
            SherlockBot.createQuarantineRole(event.getGuild().getIdLong());
        }

        builder.appendDescription(String.format("**Mute Role**\n" +
                "Status: %s\n",muteRoleStatus));

        if(muteRole != null){
            builder.appendDescription(String.format("Role: %s\n" +
                    "\n", muteRole.getAsMention()));
        }

        String logChannelStatus = "Disabled";
        TextChannel logChannel = null;
        if(SherlockBot.guildMap.get(guildID).getLogChannelID() != null){
            if(event.getGuild().getTextChannelById(SherlockBot.guildMap.get(guildID).getLogChannelID()) != null){
                logChannel = event.getGuild().getTextChannelById(SherlockBot.guildMap.get(guildID).getLogChannelID());
                logChannelStatus = "Enabled";
            }
        }

        builder.appendDescription(String.format("**Log Channel**\n" +
                "Status: %s\n",logChannelStatus));

        if(logChannel != null){
            builder.appendDescription(String.format("Channel: %s\n", logChannel.getAsMention()));
        }

        ArrayList<Permission> requiredPermissions = new ArrayList<>();
        requiredPermissions.add(Permission.MESSAGE_MANAGE);
        requiredPermissions.add(Permission.BAN_MEMBERS);
        requiredPermissions.add(Permission.KICK_MEMBERS);
        requiredPermissions.add(Permission.MANAGE_CHANNEL);
        requiredPermissions.add(Permission.MANAGE_ROLES);
        requiredPermissions.add(Permission.MANAGE_PERMISSIONS);
        requiredPermissions.add(Permission.MANAGE_SERVER);

        StringBuilder missingPermissionsString = new StringBuilder();
        for(Permission p:requiredPermissions){
            if(!event.getGuild().getSelfMember().hasPermission(p)){
                missingPermissionsString.append(p.getName()).append("\n");
            }
        }

        if(!missingPermissionsString.toString().isEmpty()){
            builder.addField("Missing Permissions:",missingPermissionsString.toString(),false);
        }

        event.getHook().editOriginalEmbeds(builder.build()).queue();
    }

    @Override
    public String getDescription() {
        return "Checks the status of all requirements for Sherlock";
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }
}
