package rsystems.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.GenericChannelUpdateEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.events.thread.GenericThreadEvent;
import net.dv8tion.jda.api.events.thread.ThreadRevealedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import rsystems.Config;
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
        if (event.getChannelType().isThread()) {
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
        try {
            SherlockBot.database.deleteRow("GuildTable", "GuildID", event.getGuildIdLong());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        try {
            SherlockBot.database.deleteRow("GuildTable", "GuildID", event.getGuild().getIdLong());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        try {
            if (SherlockBot.database.getLong("BlacklistedGuilds", "GuildID", "GuildID", event.getGuild().getIdLong()) != null) {
                event.getGuild().leave().queue();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        SherlockBot.guildMap.putIfAbsent(event.getGuild().getIdLong(), new GuildSettings(event.getGuild().getIdLong()));


        try {

            //INSERT THE NEW GUILD INTO THE DATABASE
            if (SherlockBot.database.insertGuild(event.getGuild().getIdLong(), event.getGuild().getOwnerIdLong()) >= 1) {

                if(event.getGuild().getCommunityUpdatesChannel() != null){
                    if(event.getGuild().getCommunityUpdatesChannel().canTalk()) {
                        SherlockBot.guildMap.get(event.getGuild().getIdLong()).setLogChannelID(event.getGuild().getCommunityUpdatesChannel().getIdLong());

                        sendSetupMessage(event);
                    }
                } else {
                    if(event.getGuild().getDefaultChannel().canTalk()){
                        sendSetupMessage(event);
                    }
                }

                /*
                Create quarantine Role
                 */
                event.getGuild().createRole().setColor(Color.decode("#9B1AF5")).setName("SL-Quarantine").queue(role -> {
                    role.getManager().revokePermissions(role.getPermissions()).queue();

                    ArrayList<Permission> mutePerms = new ArrayList<>();
                    mutePerms.add(Permission.MESSAGE_SEND);
                    mutePerms.add(Permission.MESSAGE_ADD_REACTION);
                    mutePerms.add(Permission.VOICE_STREAM);
                    mutePerms.add(Permission.VOICE_SPEAK);
                    mutePerms.add(Permission.CREATE_INSTANT_INVITE);
                    mutePerms.add(Permission.CREATE_PUBLIC_THREADS);
                    mutePerms.add(Permission.MESSAGE_SEND_IN_THREADS);

                    for (Category category : event.getGuild().getCategories()) {
                        try {
                            category.createPermissionOverride(role).setDeny(mutePerms).reason("Initiating bot perms").queue();
                        } catch (PermissionException e) {
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

                    for (VoiceChannel voiceChannel : event.getGuild().getVoiceChannels()) {
                        try {
                            voiceChannel.createPermissionOverride(role).setDeny(mutePerms).queue();
                        } catch (PermissionException e) {
                            break;
                        }
                    }


                    SherlockBot.guildMap.get(event.getGuild().getIdLong()).setQuarantineRoleID(role.getIdLong());
                });

                /*
                Create all bot related interactions on the guild
                 */
                SherlockBot.slashCommandDispatcher.submitCommands(event.getGuild().getIdLong());

                /*
                Join all active threads
                 */
                event.getGuild().getThreadChannels().forEach(threadChannel -> {
                    threadChannel.join().queue();
                });

                SherlockBot.guildMap.get(event.getGuild().getIdLong()).save();

            } else {
                ErrorReportHandler.sendErrorReport("Failed to add guild to database", event.getGuild().getIdLong());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        //Join all current threads
        for (ThreadChannel thread : event.getGuild().getThreadChannels()) {
            thread.join().queue();
        }
    }

    private void sendSetupMessage(GuildJoinEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Sherlock Setup");
        builder.setDescription(String.format("Hello there!\n\n" +
                "I am Sherlock!  It is great to meet you.  I have a few tasks that must be completed for me to work efficiently for your server.\n\nIf you are a server administrator, please do %ssetup to finish the setup steps.", Config.get("DEFAULTPREFIX")));
        builder.setColor(SherlockBot.getColor("generic"));

        event.getGuild().getDefaultChannel().sendMessageEmbeds(builder.build()).queue();

        builder.clear();
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        if ((event.isFromGuild()) && (event.isFromType(ChannelType.TEXT))) {
            Long guildID = event.getGuild().getIdLong();

            // Update logChannel
            if ((SherlockBot.guildMap.get(guildID).getLogChannelID() != null) && (SherlockBot.guildMap.get(guildID).getLogChannelID().equals(event.getChannel().getIdLong()))) {
                LogMessage.clearLogChannel(guildID);
            }
        }
    }

    @Override
    public void onGenericThread(GenericThreadEvent event) {
        event.getThread().join().queue();
    }

    @Override
    public void onGenericChannelUpdate(GenericChannelUpdateEvent<?> event) {
        if (event.isFromGuild() && event.getChannelType().isThread()) {
            ThreadChannel threadChannel = (ThreadChannel) event.getChannel();
            if (!threadChannel.isArchived()) {
                threadChannel.join().queue();
            }
        }
    }
}

