package rsystems.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
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
            toggleRole(event.getGuild(), event.getMember(), event.getChannel(), SherlockBot.guildMap.get(event.getGuild().getId()).assignableRoleMap.get(checkString));
        }

    }

    /*
    METHOD TO ADD/REMOVE ROLE FROM USER

    Segregated into method call for future additions.
     */
    private void toggleRole(Guild guild, Member member, TextChannel channel, Long roleID) {
        List<Role> roles = member.getRoles();
        try {
            //Member already has role, Remove it
            if (roles.contains(guild.getRoleById(roleID))) {
                guild.removeRoleFromMember(member, guild.getRoleById(roleID)).reason("Requested by user").queue();
            } else {
            //Member does not have role, Add it
                guild.addRoleToMember(member, guild.getRoleById(roleID)).reason("Requested by user").queue();
            }
        } catch (NullPointerException e) {
            channel.sendMessage("Could not get role").queue();
            //todo log error
        } catch (PermissionException e) {
            channel.sendMessage("Missing Permission:" + e.getPermission().toString());
            //todo log error
        }
    }
}

