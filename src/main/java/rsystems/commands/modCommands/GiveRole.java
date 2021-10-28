package rsystems.commands.modCommands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import rsystems.handlers.GrabRoleID;
import rsystems.objects.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GiveRole extends Command {

    @Override
    public Permission getDiscordPermission() {
        return Permission.MANAGE_ROLES;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, PrivateMessageReceivedEvent event) {

    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, GuildMessageReceivedEvent event) {
        if (message.getMentionedMembers().size() == 0) {
            // DON'T ALLOW COMMAND TO BE USED WITHOUT MENTIONABLES
            // Mentionables is now a word.  If you ever come across this, let me know.  I'll give you a gold star.


        } else {
            // MENTIONABLES WERE FOUND!  HOORAY!

            List<Member> memberList = new ArrayList<>(message.getMentionedMembers());
            Long roleID = GrabRoleID.getRoleIDFromMessage(message, content);

            if ((roleID != null) && (event.getGuild().getRoleById(roleID) != null)) {
                // Was Role successfully found?

                Role role = event.getGuild().getRoleById(roleID);

                for (Member member : memberList) {
                    try {
                        event.getGuild().addRoleToMember(member.getId(), role).reason(String.format("Requested by %s", message.getAuthor().getAsTag())).queueAfter(10, TimeUnit.SECONDS);
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
