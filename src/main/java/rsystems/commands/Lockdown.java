package rsystems.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

public class Lockdown extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            //Ignore message.  BOT LAW #2 - DO NOT LISTEN TO OTHER BOTS
            return;
        }


        if (SherlockBot.commandMap.get(47).checkCommand(event.getMessage())) {
            // User passed authorization check & command was successfully found

            int oldVerifyLevel = event.getGuild().getVerificationLevel().getKey();
            event.getGuild().getManager().setVerificationLevel(Guild.VerificationLevel.HIGH).queue();


        }

    }

}
