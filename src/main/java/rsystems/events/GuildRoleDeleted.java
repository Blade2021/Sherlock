package rsystems.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.util.Iterator;
import java.util.Map;

import static rsystems.SherlockBot.database;

public class GuildRoleDeleted extends ListenerAdapter {
    public void onRoleDelete(RoleDeleteEvent event){
        if(SherlockBot.guildMap.get(event.getGuild().getId()).assignableRoleMap.containsValue(event.getRole().getIdLong())){

            StringBuilder relatedCommands = new StringBuilder();

            Iterator it = SherlockBot.guildMap.get(event.getGuild().getId()).assignableRoleMap.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getValue().toString().equalsIgnoreCase(event.getRole().getId())) {
                    database.removeAssignableRole(event.getGuild().getIdLong(), String.valueOf(pair.getKey()));
                    relatedCommands.append(pair.getKey()).append("\n");
                    it.remove();
                    System.out.println(String.format("Removing Assignable Role | Role CMD: %s | RoleID: %d",pair.getKey(),pair.getValue()));
                }
            }

            String channelLogID = SherlockBot.guildMap.get(event.getGuild().getId()).getLogChannelID();
            if(channelLogID != null){
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Role Assignment Deleted")
                        .setDescription("A role containing assignments in this bot was deleted. The following commands were deleted.")
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
    }
}
