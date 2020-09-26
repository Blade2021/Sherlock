package rsystems.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;


import java.util.List;

public class Infraction extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            //Ignore message.  BOT LAW #2 - DO NOT LISTEN TO OTHER BOTS
        }

        String[] args = event.getMessage().getContentRaw().split("\\s+");

        // INFRACTION COMMAND
        if (SherlockBot.commands.get(0).checkCommand(event.getMessage().getContentRaw(), SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix())) {
            //Get a list of members
            List<Member> MentionedMembers = event.getMessage().getMentionedMembers();
            MentionedMembers.forEach(member -> {
                //todo Add infraction to user in DB
            });

            //Log action in the log channel for event's guild
            SherlockBot.guildMap.get(event.getGuild().getId()).logChannel.logAction(
                    "Infraction Violation",
                    MentionedMembers,
                    "Infraction submitted for the following user(s)",
                    event.getMember()
            );
        }
    }


    public void automaticInfraction(Guild guild, Member member) {
        //todo Add infraction to user in DB

        SherlockBot.guildMap.get(guild.getId()).logChannel.logAction(
                "Infraction submitted for User",
                member,
                "Infraction submitted for User",
                "BOT"
        );
    }

}
