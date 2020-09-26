package rsystems.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

public class EditModRoles extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            //Ignore message.  BOT LAW #2 - DO NOT LISTEN TO OTHER BOTS
        }
        String[] args = event.getMessage().getContentRaw().split("\\s+");


        //Add Mod Role
        if (SherlockBot.commands.get(2).checkCommand(event.getMessage().getContentRaw(), SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix())) {
            if (SherlockBot.guildMap.get(event.getGuild().getId()).addModRole(args[1])) {
                event.getMessage().addReaction("✅").queue();
            } else {
                event.getMessage().addReaction("⚠").queue();
            }
        }


        //Remove Mod Role
        if (SherlockBot.commands.get(3).checkCommand(event.getMessage().getContentRaw(), SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix())) {
            if (SherlockBot.guildMap.get(event.getGuild().getId()).removeModRole(args[1])) {
                event.getMessage().addReaction("✅").queue();
            } else {
                event.getMessage().addReaction("⚠").queue();
            }
        }

    }
}
