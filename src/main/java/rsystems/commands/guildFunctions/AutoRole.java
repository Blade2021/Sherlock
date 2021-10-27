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
import java.util.List;

public class AutoRole extends Command {
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
    public void dispatch(User sender, MessageChannel channel, Message message, String content, GuildMessageReceivedEvent event) {
        String[] args = content.split("\\s+");

        if(args.length > 0){
            String subCommand = args[0];

            // ADD AUTO ROLE COMMAND
            if(subCommand.equalsIgnoreCase("add") && args.length>=3){
                String roleTrigger = args[1];
                Long roleID = extractRoleID(event,content);

                if((!roleTrigger.isEmpty()) && (roleID != null)){
                    //Success of grabbing trigger & role ID

                    //Query Database for current auto role count
                    if(SherlockBot.database.getTableCount(event.getGuild().getIdLong(),"AutoRoles") < SherlockBot.database.getGuildData(event.getGuild().getIdLong()).getGrantedAutoRoleCount()){
                        if(SherlockBot.database.insertAutoRole(event.getGuild().getIdLong(),roleID) == 200){
                            event.getMessage().addReaction("✅").queue();
                        } else {
                            // DATABASE ERROR
                            reply(event,"A system error occurred.  Please try again later or contact us for support.  ERROR_ID:2302");
                        }
                    } else {
                        // MAX NUMBER OF AUTO ROLES REACHED
                        reply(event,"Sorry, You've hit your max quota for auto roles.  Contact us for information on getting more.");
                    }

                } else {
                    //MALFORMED OR MISSING INFORMATION ERROR
                    reply(event,"Your request was not processed due to missing information.  Please try again");
                }

            }

            // REMOVE AUTO ROLE COMMAND
            if(subCommand.equalsIgnoreCase("remove") && args.length>=2){
                Long roleID = extractRoleID(event,content);

                if(roleID != null){

                    int removableResult = SherlockBot.database.removeAutoRole(event.getGuild().getIdLong(),roleID);
                    if(removableResult >= 1){
                        event.getMessage().addReaction("✅").queue();
                    } else {
                        event.getMessage().addReaction("⚠").queue();
                    }
                }
            }

            // REMOVE AUTO ROLE COMMAND
            if(subCommand.equalsIgnoreCase("list")){

                if(SherlockBot.database.getAutoRoles(event.getGuild().getIdLong()).size() > 0) {
                    StringBuilder roleNameList = new StringBuilder();
                    StringBuilder roleIDList = new StringBuilder();

                    int successfulRolesFound = 0;

                    for(Long id:SherlockBot.database.getAutoRoles(event.getGuild().getIdLong())){
                        if(event.getGuild().getRoleById(id) != null) {
                            successfulRolesFound++;

                            roleNameList.append(event.getGuild().getRoleById(id).getName()).append("\n");
                            roleIDList.append(id).append("\n");
                        }
                    }

                    if(successfulRolesFound > 0) {
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle("Auto Roles:")
                                .setColor(Color.CYAN)
                                .addField("Role Name:", roleNameList.toString(), true)
                                .addField("Role ID", roleIDList.toString(), true);

                        reply(event, embedBuilder.build());
                    } else {
                        reply(event,"No auto roles found");
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
