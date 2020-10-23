package rsystems.events;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.util.Iterator;

public class GuildMessageDeleted extends ListenerAdapter {

    public void onGuildMessageDelete( GuildMessageDeleteEvent event) {


        if(SherlockBot.database.triggerMessageLookup(event.getGuild().getIdLong(),event.getMessageIdLong()) != null){
            Long responseID = SherlockBot.database.triggerMessageLookup(event.getGuild().getIdLong(),event.getMessageIdLong());

            event.getChannel().getIterableHistory().takeAsync(100).thenAcceptAsync(messages -> {
                Iterator it = messages.iterator();
                while(it.hasNext()){
                    Message message = (Message) it.next();
                    if(message.getIdLong() == responseID){
                        message.delete().queue();
                        break;
                    }
                }
            });
        }
    }
}
