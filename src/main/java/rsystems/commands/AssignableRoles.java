package rsystems.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.objects.AssignableRole;

import java.util.ArrayList;
import java.util.List;

import static rsystems.SherlockBot.database;

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


        /*
        GET A LIST OF ASSIGNABLE ROLES
         */
        if (SherlockBot.commands.get(9).checkCommand(event.getMessage().getContentRaw(), SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix())) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            StringBuilder roleCommands = new StringBuilder();
            StringBuilder rolesString = new StringBuilder();
            StringBuilder roleIDString = new StringBuilder();

            ArrayList<AssignableRole> roles = new ArrayList<>();
            roles.addAll(database.getAssignableRoles(event.getGuild().getIdLong()));

            for(AssignableRole r: roles){
                roleCommands.append(r.command).append("\n");
                roleIDString.append(r.RoleID).append("\n");
                try{
                    rolesString.append(event.getGuild().getRoleById(r.RoleID).getName()).append("\n");
                } catch(NullPointerException e){
                    rolesString.append("ERROR").append("\n");
                }

            }

            embedBuilder.setTitle("Assignable Roles")
                    .addField("Role Command",roleCommands.toString(),true)
                    .addField("Role Name",rolesString.toString(),true)
                    .addField("Role ID",roleIDString.toString(),true);
            event.getChannel().sendMessage(embedBuilder.build()).queue();
            event.getMessage().delete().reason("Cleaning up after bot trigger").queue();
            embedBuilder.clear();
        }
    }
}

