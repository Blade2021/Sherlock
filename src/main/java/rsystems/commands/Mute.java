package rsystems.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.util.List;

public class Mute extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            //Ignore message.  BOT LAW #2 - DO NOT LISTEN TO OTHER BOTS
        }

        // INFRACTION COMMAND
        if (SherlockBot.commands.get(1).checkCommand(event.getMessage().getContentRaw(), SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix())) {
            //Get a list of members
            List<Member> MentionedMembers = event.getMessage().getMentionedMembers();
            MentionedMembers.forEach(member -> {
                //Add Mute Role
                event.getGuild().modifyMemberRoles(member, event.getGuild().getRoleById(SherlockBot.guildMap.get(event.getGuild().getId()).getMuteRoleID())).queue();
            });

            try {
                //Log action in the log channel for event's guild
                SherlockBot.guildMap.get(event.getGuild().getId()).logChannel.logAction(
                        "Muted User",
                        MentionedMembers,
                        "Applied Mute role to User(s)",
                        event.getMember()
                );
            } catch(NullPointerException e){
                SherlockBot.guildMap.get(event.getGuild().getId()).logError("Cannot mute user, NULL Found");
            }
        }
    }

}
