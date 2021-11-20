package rsystems.handlers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.exceptions.PermissionException;
import rsystems.Config;
import rsystems.SherlockBot;
import rsystems.objects.InfractionObject;

import java.sql.SQLException;
import java.util.Iterator;

public class LogMessage {

    public static boolean registerLogChannel(final Guild guild, final TextChannel logChannel){

        if(logChannel.canTalk()) {

            SherlockBot.guildMap.get(guild.getIdLong()).setLogChannelID(logChannel.getIdLong());

            Long previousLogChannelID = SherlockBot.guildMap.get(guild.getIdLong()).getLogChannelID();
            if ((previousLogChannelID != null) && (previousLogChannelID != logChannel.getIdLong())) {

                TextChannel previousLogChannel = guild.getTextChannelById(previousLogChannelID);
                previousLogChannel.retrieveWebhooks().queue(webhooks -> {

                    Iterator it = webhooks.iterator();
                    while (it.hasNext()) {
                        Webhook hook = (Webhook) it.next();
                        if (hook.getSourceChannel().getId().equalsIgnoreCase(Config.get("AnnouncementChannelID"))) {
                            previousLogChannel.deleteWebhookById(hook.getId()).queue();
                            System.out.println("Webhook removed from previous channel");
                            break;
                        }
                    }
                });

            }

            logChannel.retrieveWebhooks().queue(webhooks -> {
                boolean skipRegister = false;

                for (Webhook webhook : webhooks) {
                    if (webhook.getSourceChannel().getId().equalsIgnoreCase(Config.get("AnnouncementChannelID"))) {
                        skipRegister = true;
                        break;
                    }
                }

                if (!skipRegister) {
                    System.out.println("not skipping");
                    TextChannel newsChannel = SherlockBot.jda.getGuildById(Config.get("MainGuild")).getTextChannelById(Config.get("AnnouncementChannelID"));
                    newsChannel.follow(logChannel).queue();
                }

                //System.out.println(webhooks);
            });

            try {
                SherlockBot.database.updateGuild(SherlockBot.guildMap.get(guild.getIdLong()));
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

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
