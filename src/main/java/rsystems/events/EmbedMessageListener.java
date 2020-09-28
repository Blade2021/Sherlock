package rsystems.events;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EmbedMessageListener extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
        if(event.getAuthor().isBot()){
            return;
        }

        if(event.getMessage().getEmbeds().size() > 0){
            event.getMessage().suppressEmbeds(true).reason("Channel is on timeout").queue();
        }
    }
}
