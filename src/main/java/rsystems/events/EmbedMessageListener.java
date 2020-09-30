package rsystems.events;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.util.concurrent.TimeUnit;

public class EmbedMessageListener extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
        if(event.getAuthor().isBot()){
            return;
        }

        if(event.getMessage().getEmbeds().size() > 0){
            int filterLevel = SherlockBot.guildMap.get(event.getGuild().getId()).embedFilter;
            if(filterLevel >= 10) {
                event.getMessage().suppressEmbeds(true).reason("Channel is on timeout").queue();
            } else if(filterLevel == 2){
                event.getMessage().suppressEmbeds(true).reason("Filter level is set to 2").queueAfter(5, TimeUnit.MINUTES);
            } else if(filterLevel == 3){
                event.getMessage().suppressEmbeds(true).reason("Filter level is set to 3").queueAfter(5, TimeUnit.MINUTES);
            }
        }
    }
}
