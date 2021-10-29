package rsystems.commands.modCommands;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import rsystems.handlers.GrabRoleID;
import rsystems.objects.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class TakeRole extends Command {
    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, PrivateMessageReceivedEvent event) {

    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, GuildMessageReceivedEvent event) {
        if (message.getMentionedMembers().size() == 0) {
            // DON'T ALLOW COMMAND TO BE USED WITHOUT MENTIONABLES

        } else {
            // MENTIONABLES WERE FOUND

            List<Member> memberList = new ArrayList<>(message.getMentionedMembers());
            Long roleID = GrabRoleID.getRoleIDFromMessage(message, content);

            if ((roleID != null) && (event.getGuild().getRoleById(roleID) != null)) {
                // Was Role successfully found?

                Role role = event.getGuild().getRoleById(roleID);

                for (Member member : memberList) {
                    try {
                        event.getGuild().removeRoleFromMember(member.getId(), role).reason(String.format("Requested by %s", message.getAuthor().getAsTag())).queueAfter(5, TimeUnit.SECONDS, Success -> {
                            message.addReaction("✅").queue();
                        });
                    } catch (PermissionException e) {
                        reply(event,String.format("Encountered Permission error: %s while attempting to run command.",e.getPermission()));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public String getHelp() {
        return null;
    }
}
