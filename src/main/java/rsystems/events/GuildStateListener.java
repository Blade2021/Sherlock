package rsystems.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.GenericChannelUpdateEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.thread.GenericThreadEvent;
import net.dv8tion.jda.api.events.thread.ThreadRevealedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import rsystems.Config;
import rsystems.SherlockBot;
import rsystems.handlers.ErrorReportHandler;
import rsystems.handlers.LogMessage;
import rsystems.objects.GuildSettings;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import static rsystems.SherlockBot.createQuarantineRole;

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

        SherlockBot.guildMap.remove(event.getGuild().getIdLong());

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
                    if(event.getGuild().getDefaultChannel().asTextChannel().canTalk()){
                        sendSetupMessage(event);
                    }
                }

                /*
                Create quarantine Role
                 */
                createQuarantineRole(event.getGuild().getIdLong());

                /*
                Create all bot related interactions on the guild
                 */
                //SherlockBot.slashCommandDispatcher.submitCommands(event.getGuild().getIdLong());

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
        builder.setColor(SherlockBot.getColor(SherlockBot.colorType.GENERIC));

        event.getGuild().getDefaultChannel().asTextChannel().sendMessageEmbeds(builder.build()).queue();

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

        if(event.isFromGuild()){
            try {
                SherlockBot.database.removeAutoPushChannel(event.getGuild().getIdLong(),event.getChannel().getIdLong());
            } catch (SQLException e) {
                e.printStackTrace();
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

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        try {
            int selfRoleCount = SherlockBot.database.getTableCount(event.getGuild().getIdLong(), "SelfRoles");
            int autoRoleCount = SherlockBot.database.getTableCount(event.getGuild().getIdLong(), "AutoRoles");

            ArrayList<String> triggers = new ArrayList<>();
            boolean autoRoleFound = false;

            if ((selfRoleCount > 0) || (autoRoleCount > 0)){

                if(selfRoleCount > 0) {
                    Map<String, Long> guildSelfRoleMap = SherlockBot.database.getGuildSelfRoles(event.getGuild().getIdLong());

                    //ITERATE THROUGH GUILD SELF ROLE MAP
                    for (Map.Entry<String, Long> entry : guildSelfRoleMap.entrySet()) {

                        if (entry.getValue().equals(event.getRole().getIdLong())) {
                            //ENTRY FOUND
                            triggers.add(entry.getKey());

                            SherlockBot.database.deleteRow("SelfRoles","RoleID",event.getRole().getIdLong());
                        }
                    }
                }

                if(autoRoleCount > 0){

                    if(SherlockBot.database.getLong("AutoRoles","RoleID","RoleID",event.getRole().getIdLong()) != null){
                        autoRoleFound = true;

                        SherlockBot.database.deleteRow("AutoRoles","RoleID",event.getRole().getIdLong());
                    }

                }

                if((autoRoleFound || triggers.size() > 0)){

                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("Role Assignments Removed");
                    builder.setDescription("A role containing assignments in this bot was deleted. The following commands were deleted automatically.");
                    builder.setColor(SherlockBot.getColor(SherlockBot.colorType.GENERIC));

                    builder.addField("Role Name:",event.getRole().getName(),true);
                    builder.addField("Role ID",event.getRole().getId(),true);
                    builder.addBlankField(true);

                    if(autoRoleFound){
                        builder.addField("Auto Role:","TRUE",false);
                    }

                    if(triggers.size() > 0){
                        StringBuilder triggersString = new StringBuilder();

                        for(String trigger:triggers){
                            triggersString.append(trigger).append("\n");
                        }

                        builder.addField("SelfRole Triggers:",triggersString.toString(),false);
                    }

                    LogMessage.sendLogMessage(event.getGuild().getIdLong(),builder.build());

                    builder.clear();

                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

