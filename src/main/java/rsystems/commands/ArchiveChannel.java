package rsystems.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.handlers.LogChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static rsystems.SherlockBot.database;

public class ArchiveChannel extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        /*
        ARCHIVE CHANNEL
         */
        if (SherlockBot.commandMap.get(34).checkCommand(event.getMessage())) {
            if(event.getMessage().getMentionedChannels().size() > 0){
                try{
                    Map<String,Long> channelMap = new HashMap<>();

                    for(TextChannel channel:event.getMessage().getMentionedChannels()){
                        //Confirm the channel got loaded into the database
                        if(SherlockBot.database.storeArchiveChannel(event.getGuild().getIdLong(),channel.getIdLong(),channel.getParent().getIdLong(),channel.getPosition()) > 0){
                            channel.getManager().setParent(event.getGuild().getCategoryById(SherlockBot.guildMap.get(event.getGuild().getId()).getArchiveCategoryID())).queue();
                            channelMap.putIfAbsent(channel.getName(),channel.getIdLong());

                            overridePermissions(event.getGuild(),channel,false);
                        }
                    }

                    if(channelMap.size() > 0){
                        StringBuilder channelNameString = new StringBuilder();
                        StringBuilder channelIDString = new StringBuilder();


                        for(Map.Entry<String,Long> entry:channelMap.entrySet()){
                            channelNameString.append(entry.getKey()).append("\n");
                            channelIDString.append(entry.getValue()).append("\n");
                        }

                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle("Moved Channels")
                                .addField("Channel Name:",channelNameString.toString(),true)
                                .addField("Channel ID(s):",channelIDString.toString(),true);
                        event.getChannel().sendMessage(embedBuilder.build()).queue();
                        embedBuilder.clear();

                        LogChannel logChannel = new LogChannel();
                        logChannel.logArchiveChannel(event.getGuild(),SherlockBot.guildMap.get(event.getGuild().getId()).getArchiveCategoryID(),channelNameString.toString(),channelIDString.toString(),event.getMember());
                    }

                }catch(NullPointerException e){

                }catch(PermissionException e){
                    event.getChannel().sendMessage("Missing Permission: " + e.getPermission()).queue();
                }
            }
        }


        /*
        RETRIEVE CHANNEL FROM ARCHIVE
         */
        if (SherlockBot.commandMap.get(35).checkCommand(event.getMessage())) {
            if(event.getMessage().getMentionedChannels().size() > 0){
                for(TextChannel channel:event.getMessage().getMentionedChannels()){
                    Long previousCat = null;
                    Integer previousPosition = null;

                    if(SherlockBot.database.getLong("ArchiveTable","PreviousCategory","ChannelID",channel.getIdLong()) != null){
                        previousCat = SherlockBot.database.getLong("ArchiveTable","PreviousCategory","ChannelID",channel.getIdLong());
                    }

                    if(SherlockBot.database.getInt("ArchiveTable","PreviousPosition","ChannelID",channel.getIdLong()) != null){
                        previousPosition = SherlockBot.database.getInt("ArchiveTable","PreviousPosition","ChannelID",channel.getIdLong());
                    }

                    overridePermissions(event.getGuild(),channel,true);

                    try {
                        if ((previousCat != null) && (event.getGuild().getCategoryById(previousCat) != null)) {
                            channel.getManager().setParent(event.getGuild().getCategoryById(previousCat)).queue();

                            //Remove the channel from the ArchiveTable
                            SherlockBot.database.deleteRow("ArchiveTable","ChannelID",channel.getIdLong());
                        }

                        if(previousPosition != null){
                            channel.getManager().setPosition(previousPosition).queueAfter(5, TimeUnit.SECONDS);
                        }
                    }catch(NullPointerException e){

                    }
                }
            }
        }
    }

    private void overridePermissions(Guild guild, TextChannel channel, Boolean archiveReturn) {
        HashMap<Long, Integer> eventMap = new HashMap<>();

        if(archiveReturn){
            eventMap.putAll(database.retractEvent(guild.getIdLong(), channel.getIdLong()));
        }

        for (PermissionOverride permissionOverride : channel.getRolePermissionOverrides()) {
                //RETURNING THE CHANNEL FROM ARCHIVE
            if((archiveReturn) && (eventMap.size() > 0)){
                try {
                    if (eventMap.get(permissionOverride.getIdLong()) == 1) {
                        channel.upsertPermissionOverride(permissionOverride.getPermissionHolder()).grant(Permission.MESSAGE_WRITE).queue();
                    } else if (eventMap.get(permissionOverride.getIdLong()) == 2) {
                        channel.upsertPermissionOverride(permissionOverride.getPermissionHolder()).deny(Permission.MESSAGE_WRITE).queue();
                    } else {
                        channel.upsertPermissionOverride(permissionOverride.getPermissionHolder()).clear(Permission.MESSAGE_WRITE).queue();
                    }
                } catch (NullPointerException e) {
                    System.out.println("Did not find a permission override for ID: " + permissionOverride.getId());
                }
            } else if(!archiveReturn){
                //SENDING THE CHANNEL TO ARCHIVE

                int currentState = 0;

                if(permissionOverride.getRole().isPublicRole()){
                    System.out.println("debug");
                }

                if (permissionOverride.getAllowed().contains(Permission.MESSAGE_WRITE)) {
                    currentState = 1;
                } else if (permissionOverride.getDenied().contains(Permission.MESSAGE_WRITE)) {
                    currentState = 2;
                }

                System.out.println("Attempting to set override for Role:" + permissionOverride.getRole().getName());
                database.insertTimedEvent(guild.getIdLong(), channel.getIdLong(), 3, "Channel Archive", permissionOverride.getRole().getIdLong(), currentState);
                channel.upsertPermissionOverride(permissionOverride.getPermissionHolder()).deny(Permission.MESSAGE_WRITE).queue();
            }
        }
    }
}
