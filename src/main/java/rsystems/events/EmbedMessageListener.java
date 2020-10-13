package rsystems.events;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.util.List;

public class EmbedMessageListener extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        if (event.getMessage().getEmbeds().size() > 0) {

            if (SherlockBot.guildMap.get(event.getGuild().getId()).embedFilter >= 1) {
                //Ignore embeds coming from admins
                try {
                    if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                        return;
                    }
                } catch (PermissionException | NullPointerException e) {
                    System.out.println("Could not grab permissions for user. USER: " + event.getMember().getId());
                }

                if (!SherlockBot.guildMap.get(event.getGuild().getId()).exceptionMap.containsKey(event.getChannel().getId())) {

                    //Take the filter and times it by 10
                    int filterAmount = SherlockBot.guildMap.get(event.getGuild().getId()).embedFilter * 10;

                    //Only allow values up to 30
                    if (filterAmount > 30) {
                        filterAmount = 30;
                    }

                    //Initiate a variable to store embed count
                    int totalEmbedCount = 0;

                    //Get history of the past x number of messages
                    List<Message> messages = event.getChannel().getHistory().retrievePast(filterAmount).complete();

                    //Parse through the messages to see if message contained an embed (IGNORING BOTS)
                    for (Message m : messages) {
                        if (m.getMember().getUser().isBot()) {
                            continue;
                        }

                        if (m.getEmbeds().size() > 0) {
                            totalEmbedCount++;
                        }
                    }

                    //More then 2 embeds were found within the filter so suppress the new embed
                    if (totalEmbedCount > 2) {
                        System.out.println("Total Embed count: " + totalEmbedCount);
                        event.getMessage().suppressEmbeds(true).reason("Channel on cooldown").queue();
                        event.getMessage().addReaction("🥶").queue();
                    }
                }
            }
        }
    }
}
