package rsystems.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import static rsystems.SherlockBot.database;

public class GuildRoleDeleted extends ListenerAdapter {
    public void onRoleDelete(RoleDeleteEvent event){
        //Check self roles for role ID
        if(SherlockBot.guildMap.get(event.getGuild().getId()).selfRoleMap.containsValue(event.getRole().getIdLong())){

            StringBuilder relatedCommands = new StringBuilder();

            Iterator it = SherlockBot.guildMap.get(event.getGuild().getId()).selfRoleMap.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getValue().toString().equalsIgnoreCase(event.getRole().getId())) {
                    database.removeSelfRole(event.getGuild().getIdLong(), String.valueOf(pair.getKey()));
                    relatedCommands.append(pair.getKey()).append("\n");
                    it.remove();
                    System.out.println(String.format("Removing Self Role | Role CMD: %s | RoleID: %d",pair.getKey(),pair.getValue()));
                }
            }

            String channelLogID = SherlockBot.guildMap.get(event.getGuild().getId()).getLogChannelID();
            if(channelLogID != null){
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Role Assignment Deleted")
                        .setDescription("A role containing assignments in this bot was deleted. The following commands were deleted automatically.")
                        .setColor(Color.RED)
                        .addField("Assigned Commands:",relatedCommands.toString(),false)
                        .addField("Role Name:",event.getRole().getName(),true)
                        .addField("Role ID:",event.getRole().getId(),true);

                try{
                    event.getGuild().getTextChannelById(channelLogID).sendMessage(embedBuilder.build()).queue();
                } catch(NullPointerException | PermissionException e){

                } finally {
                    embedBuilder.clear();
                }
            }
        }

        ArrayList<Long> autoRoles = new ArrayList<>();
        autoRoles = SherlockBot.database.getAutoRoles(event.getGuild().getIdLong());
        if(autoRoles.contains(event.getRole().getIdLong())){

            SherlockBot.database.deleteRow("AutoRole","RoleID",event.getRole().getIdLong());

            String channelLogID = SherlockBot.guildMap.get(event.getGuild().getId()).getLogChannelID();
            if(channelLogID != null){
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Role Assignment Deleted")
                        .setDescription("A role that was connected to an auto role was deleted.")
                        .setColor(Color.RED)
                        .addField("Role Name:",event.getRole().getName(),true)
                        .addField("Role ID:",event.getRole().getId(),true);

                try{
                    event.getGuild().getTextChannelById(channelLogID).sendMessage(embedBuilder.build()).queue();
                } catch(NullPointerException | PermissionException e){

                } finally {
                    embedBuilder.clear();
                }
            }

        }

        // Delete any rows from the database connected to that role
        SherlockBot.database.deleteRow("ReactionTable","RoleID",event.getRole().getIdLong());


    }
}
