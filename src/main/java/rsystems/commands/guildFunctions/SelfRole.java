package rsystems.commands.guildFunctions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SelfRole extends Command {
    @Override
    public Integer getPermissionIndex() {
        return 64;
    }

    @Override
    public Permission getDiscordPermission(){
        return Permission.MANAGE_ROLES;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, PrivateMessageReceivedEvent event) {
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, GuildMessageReceivedEvent event) throws SQLException {
        String[] args = content.split("\\s+");

        if(args.length > 0){
            String subCommand = args[0];

            // ADD SELF ROLE COMMAND
            if(subCommand.equalsIgnoreCase("add") && args.length>=3){
                String roleTrigger = args[1];
                Long roleID = extractRoleID(event,content);

                if((!roleTrigger.isEmpty()) && (roleID != null)){
                    //Success of grabbing trigger & role ID

                    //Query Database for current self role count
                    if(SherlockBot.database.getTableCount(event.getGuild().getIdLong(),"SelfRoles") < SherlockBot.database.getGuildData(event.getGuild().getIdLong()).getGrantedSelfRoleCount()){
                        if(SherlockBot.database.insertSelfRole(event.getGuild().getIdLong(),roleTrigger,roleID) == 200){
                            event.getMessage().addReaction("✅").queue();
                        } else {
                            // DATABASE ERROR
                            reply(event,"A system error occurred.  Please try again later or contact us for support.  ERROR_ID:2301");
                        }
                    } else {
                        // MAX NUMBER OF SELF ROLES REACHED
                        reply(event,"Sorry, You've hit your max quota for self roles.  Contact us for information on getting more.");
                    }

                } else {
                    //MALFORMED OR MISSING INFORMATION ERROR
                    reply(event,"Your request was not processed due to missing information.  Please try again");
                }

            }

            // REMOVE SELF ROLE COMMAND
            if(subCommand.equalsIgnoreCase("remove") && args.length>=2){
                String roleTrigger = args[1];

                if(!roleTrigger.isEmpty()){

                    int removableResult = SherlockBot.database.removeSelfRole(event.getGuild().getIdLong(),roleTrigger);
                    if(removableResult >= 1){
                        event.getMessage().addReaction("✅").queue();
                    } else {
                        event.getMessage().addReaction("⚠").queue();
                    }
                }
            }

            // REMOVE SELF ROLE COMMAND
            if(subCommand.equalsIgnoreCase("list")){

                if(SherlockBot.database.getGuildSelfRoles(event.getGuild().getIdLong()).size() > 0) {
                    StringBuilder triggerList = new StringBuilder();
                    StringBuilder roleNameList = new StringBuilder();
                    StringBuilder roleIDList = new StringBuilder();

                    int successfulRolesFound = 0;

                    for (Map.Entry<String, Long> roleMap : SherlockBot.database.getGuildSelfRoles(event.getGuild().getIdLong()).entrySet()) {
                        if(event.getGuild().getRoleById(roleMap.getValue()) != null) {
                            successfulRolesFound++;

                            triggerList.append(roleMap.getKey()).append("\n");
                            roleNameList.append(event.getGuild().getRoleById(roleMap.getValue()).getName()).append("\n");
                            roleIDList.append(roleMap.getValue()).append("\n");
                        }
                    }

                    if(successfulRolesFound > 0) {
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle("Self Roles:")
                                .setColor(Color.CYAN)
                                .addField("Trigger:", triggerList.toString(), true)
                                .addField("Role Name:", roleNameList.toString(), true)
                                .addField("Role ID", roleIDList.toString(), true);

                        reply(event, embedBuilder.build());
                    } else {
                        reply(event,"No self roles found");
                    }
                }
            }

        }
    }

    @Override
    public String getHelp() {
        return null;
    }

    private Long extractRoleID(GuildMessageReceivedEvent event, String content){

        String[] args = content.split("\\s+");

        Long roleID = null;
        try{
            roleID = Long.valueOf(args[2]);
        } catch(NumberFormatException e){
            List<Role> roles = event.getGuild().getRoles();
            for(Role r:roles){
                if(r.getName().equalsIgnoreCase(args[2])){
                    roleID=r.getIdLong();
                    break;
                }
            }
        }

        return roleID;
    }
}
