package rsystems.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.objects.SelfRole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static rsystems.SherlockBot.database;

public class SelfRoles extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String[] args = event.getMessage().getContentRaw().split("\\s+");

        if(event.getMessage().getContentDisplay().startsWith(SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix())) {

            String checkString = args[0].replaceFirst(SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix(), "");

            /*
            USER REQUESTING ROLE TO BE ADDED/REMOVED
             */
            if (SherlockBot.guildMap.get(event.getGuild().getId()).selfRoleMap.containsKey(checkString)) {
                Long roleID = SherlockBot.guildMap.get(event.getGuild().getId()).selfRoleMap.get(checkString);

                List<Role> roles = event.getMember().getRoles();
                try {
                    //Member already has role, Remove it
                    if (roles.contains(event.getGuild().getRoleById(roleID))) {
                        event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(roleID)).reason("Requested by user").queue(success -> {
                            event.getMessage().addReaction("✅").queue();
                            event.getMessage().reply("I have removed the " + event.getGuild().getRoleById(roleID).getName() + " from you.").queue(
                                    messageSentSuccess -> {
                                        messageSentSuccess.delete().queueAfter(30, TimeUnit.SECONDS);
                                    }
                            );
                        });
                    } else {
                        //Member does not have role, Add it
                        event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(roleID)).reason("Requested by user").queue(success -> {
                            event.getMessage().addReaction("✅").queue();
                            event.getMessage().reply(" I have added the " + event.getGuild().getRoleById(roleID).getName() + " to you.").queue(
                                    messageSentSuccess -> {
                                        //messageSentSuccess.delete().queueAfter(30, TimeUnit.SECONDS);
                                    }
                            );

                        });
                    }
                } catch (NullPointerException | IllegalArgumentException e) {
                    event.getMessage().reply("I could not find the role associated with that command.").queue();
                    event.getMessage().addReaction("⚠").queue();
                } catch (PermissionException e) {
                    event.getMessage().reply("Missing Permission:" + e.getPermission().toString());
                    event.getMessage().addReaction("⚠").queue();
                }
            }
        }


        /*
        GET A LIST OF SELF ROLES
         */
        if (SherlockBot.commandMap.get(9).checkCommand(event.getMessage())) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            StringBuilder roleCommands = new StringBuilder();
            StringBuilder rolesString = new StringBuilder();
            StringBuilder roleIDString = new StringBuilder();

            ArrayList<SelfRole> roles = new ArrayList<>();
            roles.addAll(database.getSelfRoles(event.getGuild().getIdLong()));

            for(SelfRole r: roles){
                roleCommands.append(r.command).append("\n");
                roleIDString.append(r.RoleID).append("\n");
                try{
                    rolesString.append(event.getGuild().getRoleById(r.RoleID).getName()).append("\n");
                } catch(NullPointerException e){
                    rolesString.append("ERROR").append("\n");
                }

            }

            embedBuilder.setTitle("SELF Roles")
                    .addField("Role Command",roleCommands.toString(),true)
                    .addField("Role Name",rolesString.toString(),true)
                    .addField("Role ID",roleIDString.toString(),true);
            event.getChannel().sendMessage(embedBuilder.build()).queue();
            /*
            todo:Replace with automatic message remove
             */
            //event.getMessage().delete().reason("Cleaning up after bot trigger").queue();
            embedBuilder.clear();
        }
    }
}

