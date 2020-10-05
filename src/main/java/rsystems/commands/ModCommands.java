package rsystems.commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

public class ModCommands extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            //Ignore message.  BOT LAW #2 - DO NOT LISTEN TO OTHER BOTS
            return;
        }
        String[] args = event.getMessage().getContentRaw().split("\\s+");


        //Add Mod Role
        if (SherlockBot.commands.get(20).checkCommandMod(event.getMessage())) {
            if (SherlockBot.guildMap.get(event.getGuild().getId()).addModRole(args[1])) {
                event.getMessage().addReaction("✅").queue();
            } else {
                event.getMessage().addReaction("⚠").queue();
            }
        }


        //Remove Mod Role
        if (SherlockBot.commands.get(20).checkCommandMod(event.getMessage())) {
            if (SherlockBot.guildMap.get(event.getGuild().getId()).removeModRole(args[1])) {
                event.getMessage().addReaction("✅").queue();
            } else {
                event.getMessage().addReaction("⚠").queue();
            }
        }

    }
}
