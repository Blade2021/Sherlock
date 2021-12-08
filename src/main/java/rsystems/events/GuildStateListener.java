package rsystems.events;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.events.thread.ThreadRevealedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import rsystems.SherlockBot;
import rsystems.handlers.ErrorReportHandler;
import rsystems.handlers.LogMessage;
import rsystems.objects.GuildSettings;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class GuildStateListener extends ListenerAdapter {

    @Override
    public void onChannelCreate(@NotNull ChannelCreateEvent event) {
        if(event.getChannelType().isThread()){
            ThreadChannel channel = (ThreadChannel) event.getChannel();
            channel.join().queue();
        }
    }

    @Override
    public void onThreadRevealed(ThreadRevealedEvent event) {
        event.getThread().join().queue();
    }

    @Override
    public void onUnavailableGuildLeave(UnavailableGuildLeaveEvent event) {
        SherlockBot.database.deleteRow("Guilds","GuildID",event.getGuildIdLong());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        SherlockBot.database.deleteRow("Guilds","GuildID",event.getGuild().getIdLong());
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        try {
            if(SherlockBot.database.getLong("BlacklistedGuilds","GuildID","GuildID",event.getGuild().getIdLong()) != null){
                event.getGuild().leave().queue();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        SherlockBot.guildMap.putIfAbsent(event.getGuild().getIdLong(),new GuildSettings(event.getGuild().getIdLong()));

        try {
            if(SherlockBot.database.insertGuild(event.getGuild().getIdLong(),event.getGuild().getOwnerIdLong()) >= 1){
                event.getGuild().createRole().setColor(Color.decode("#B0A98F")).setName("SL-Muted").queue(role -> {
                    SherlockBot.database.putValue("Guilds","MuteRoleID","GuildID",event.getGuild().getIdLong(),role.getIdLong());

                    ArrayList<Permission> mutePerms = new ArrayList<>();
                    mutePerms.add(Permission.MESSAGE_SEND);
                    mutePerms.add(Permission.MESSAGE_ADD_REACTION);
                    mutePerms.add(Permission.VOICE_STREAM);
                    mutePerms.add(Permission.VOICE_SPEAK);
                    mutePerms.add(Permission.CREATE_INSTANT_INVITE);
                    mutePerms.add(Permission.CREATE_PUBLIC_THREADS);
                    mutePerms.add(Permission.MESSAGE_SEND_IN_THREADS);

                    for(Category category : event.getGuild().getCategories()){
                        try{
                            category.createPermissionOverride(role).setDeny(mutePerms).reason("Initiating bot perms").queue();
                        } catch (PermissionException e){
                            break;
                        }
                    }

                    //Set the mute role permission override for each channel
                    for (TextChannel channel : event.getGuild().getTextChannels()) {
                        try {
                            channel.createPermissionOverride(role).setDeny(mutePerms).queue();
                        } catch (PermissionException e) {
                            break;
                        }
                    }

                    for (VoiceChannel voiceChannel:event.getGuild().getVoiceChannels()){
                        try{
                            voiceChannel.createPermissionOverride(role).setDeny(mutePerms).queue();
                        } catch(PermissionException e){
                            break;
                        }
                    }

                    SherlockBot.guildMap.get(event.getGuild().getIdLong()).setMuteRoleID(role.getIdLong());
                });
            } else {
                ErrorReportHandler.sendErrorReport("Failed to add guild to database",event.getGuild().getIdLong());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        //Join all current threads
        for(ThreadChannel thread:event.getGuild().getThreadChannels()){
            thread.join().queue();
        }
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        if((event.isFromGuild()) && (event.isFromType(ChannelType.TEXT))){
            Long guildID = event.getGuild().getIdLong();

            // Update logChannel
            if((SherlockBot.guildMap.get(guildID).getLogChannelID() != null) && (SherlockBot.guildMap.get(guildID).getLogChannelID().equals(event.getChannel().getIdLong()))){
                LogMessage.clearLogChannel(guildID);
            }
        }
    }
}
