package rsystems.commands;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.util.List;

public class AssignableRoles extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String[] args = event.getMessage().getContentRaw().split("\\s+");

        String checkString = args[0].replaceFirst(SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix(), "");

        /*
        USER REQUESTING ROLE TO BE ADDED/REMOVED
         */
        if (SherlockBot.guildMap.get(event.getGuild().getId()).assignableRoleMap.containsKey(checkString)) {
            Long roleID = SherlockBot.guildMap.get(event.getGuild().getId()).assignableRoleMap.get(checkString);

            List<Role> roles = event.getMember().getRoles();
            try {
                //Member already has role, Remove it
                if (roles.contains(event.getGuild().getRoleById(roleID))) {
                    event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(roleID)).reason("Requested by user").queue(success -> {
                        event.getMessage().addReaction("✅").queue();
                    });
                } else {
                    //Member does not have role, Add it
                    event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(roleID)).reason("Requested by user").queue(success -> {
                        event.getMessage().addReaction("✅").queue();
                    });
                }
            } catch (NullPointerException | IllegalArgumentException e) {
                event.getChannel().sendMessage(event.getMember().getAsMention() + " I could not find the role associated with that command.").queue();
                event.getMessage().addReaction("⚠").queue();
                //todo log error
            } catch (PermissionException e) {
                event.getChannel().sendMessage("Missing Permission:" + e.getPermission().toString());
                event.getMessage().addReaction("⚠").queue();
                //todo log error
            }
        }
    }
}

