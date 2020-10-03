package rsystems.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.handlers.LogChannel;

import java.util.List;

import static rsystems.SherlockBot.*;

public class Infraction extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            //Ignore message.  BOT LAW #2 - DO NOT LISTEN TO OTHER BOTS
        }

        if(event.isFromGuild()) {
            String[] args = event.getMessage().getContentRaw().split("\\s+");

            // WRITE INFRACTION COMMAND
            if (SherlockBot.commands.get(1).checkCommand(event.getMessage().getContentRaw(), SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix())) {
                //Get a list of members
                List<Member> MentionedMembers = event.getMessage().getMentionedMembers();
                MentionedMembers.forEach(member -> {
                    database.insertInfraction(event.getGuild().getId(), member.getIdLong(), args[1], event.getMember().getIdLong());
                });

                LogChannel logChannel = new LogChannel();
                logChannel.logAction(event.getGuild(), "Infraction Violation", MentionedMembers, event.getMember());
            }
        }
    }

    // AUTOMATIC INFRACTION
    public void automaticInfraction(Guild guild, Member member) {
        database.insertInfraction(guild.getId(),member.getIdLong(),"Auto BoT Infraction", bot.getIdLong());

        LogChannel logChannel = new LogChannel();
        logChannel.logAction(guild,"Automatic Infraction",member, bot);
    }

}
