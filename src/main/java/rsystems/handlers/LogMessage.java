package rsystems.handlers;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import rsystems.SherlockBot;
import rsystems.objects.InfractionObject;

public class LogMessage {

    public static void sendLogMessage(Long guildID, MessageEmbed embed){

        if(SherlockBot.getGuildSettings(guildID).getLogChannelID() != null){
            final Long logChannelID = SherlockBot.guildMap.get(guildID).getLogChannelID();

            if(SherlockBot.jda.getGuildById(guildID).getTextChannelById(logChannelID) != null){

                final TextChannel logChannel = SherlockBot.jda.getGuildById(guildID).getTextChannelById(logChannelID);
                try{
                    logChannel.sendMessageEmbeds(embed).queue();
                } catch(NullPointerException | PermissionException e){
                    // REMOVE LOG CHANNEL COMPLETELY FROM PREVIOUS DATA

                    SherlockBot.database.putValueNull("Guilds","LogChannelID","GuildID",guildID);
                    SherlockBot.guildMap.get(guildID).setLogChannelID(null);
                }

            }
        }

    }

    public static void sendLogMessage(Long guildID, InfractionObject infractionObject){

        if(SherlockBot.getGuildSettings(guildID).getLogChannelID() != null){
            final Long logChannelID = SherlockBot.guildMap.get(guildID).getLogChannelID();

            if(SherlockBot.jda.getGuildById(guildID).getTextChannelById(logChannelID) != null){

                final TextChannel logChannel = SherlockBot.jda.getGuildById(guildID).getTextChannelById(logChannelID);
                try{
                    logChannel.sendMessageEmbeds(infractionObject.createEmbedMessge()).queue((message -> {
                        SherlockBot.database.putValue("CaseTable","LogMessageID","CaseID",infractionObject.getCaseNumber(),message.getIdLong());
                    }));
                } catch(NullPointerException | PermissionException e){
                    // REMOVE LOG CHANNEL COMPLETELY FROM PREVIOUS DATA

                    SherlockBot.database.putValueNull("Guilds","LogChannelID","GuildID",guildID);
                    SherlockBot.guildMap.get(guildID).setLogChannelID(null);
                }

            }
        }

    }

    public static void sendLogMessage(Long guildID, String message){

        if(SherlockBot.getGuildSettings(guildID).getLogChannelID() != null){
            final Long logChannelID = SherlockBot.guildMap.get(guildID).getLogChannelID();

            if(SherlockBot.jda.getGuildById(guildID).getTextChannelById(logChannelID) != null){

                final TextChannel logChannel = SherlockBot.jda.getGuildById(guildID).getTextChannelById(logChannelID);
                try{
                    logChannel.sendMessage(message).queue();
                } catch(NullPointerException | PermissionException e){
                    // REMOVE LOG CHANNEL COMPLETELY FROM PREVIOUS DATA

                    SherlockBot.database.putValueNull("Guilds","LogChannelID","GuildID",guildID);
                    SherlockBot.guildMap.get(guildID).setLogChannelID(null);
                }

            }
        }

    }

}
