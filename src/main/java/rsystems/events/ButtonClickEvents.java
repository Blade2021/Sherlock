package rsystems.events;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonClickEvents extends ListenerAdapter {

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        if (event.getComponentId().equals("hello")) {
            event.reply("Hello :)").queue(); // send a message in the channel
            event.editButton(event.getButton().asDisabled()).queue();
        } else if (event.getComponentId().equals("noclick")) {
            event.editMessage("That button didn't say click me").queue(); // update the message
            event.editButton(event.getButton().asDisabled()).queue();
        } else if (event.getComponentId().equals("test")){
            event.deferEdit().queue();
            if(event.isAcknowledged()){
                event.getHook().editOriginal("You clicked " + event.getButton().getLabel()).queue();
            } else {
                event.reply("You clicked " + event.getButton().getLabel()).setEphemeral(true).queue();
            }
            event.editButton(event.getButton().asDisabled()).queue();
        } else if (event.getComponentId().equals("previous")){
            event.deferReply(true).queue();
            event.getMessage().delete().queue();
        }
    }

}
