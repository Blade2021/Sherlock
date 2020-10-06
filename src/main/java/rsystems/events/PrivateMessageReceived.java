package rsystems.events;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PrivateMessageReceived extends ListenerAdapter {

    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event){
        if(event.getAuthor().isBot()){
            return;
        }

        String[] greetings = {"hello", "hi", "hey", "aloha"};
        for(String greet: greetings){
            if(event.getMessage().getContentDisplay().toLowerCase().contains(greet)){
                event.getChannel().sendMessage("Hello there! \uD83D\uDC4B").queue();
                return;
            }
        }

        //event.getChannel().sendMessage("Sorry " + event.getAuthor().getAsMention() + " I don't currently accept messages via Direct Message.  Please type your commands in the guild").queue();
    }

}
