package rsystems.events;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.sql.SQLException;
import java.util.Map;

public class GuildMessageReceived extends ListenerAdapter {

    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {

        final String guildPrefix = SherlockBot.guildMap.get(event.getGuild().getIdLong()).getPrefix();
        String message = event.getMessage().getContentRaw();

        final boolean defaultPrefixFound = message.toLowerCase().startsWith(SherlockBot.defaultPrefix.toLowerCase());
        if (defaultPrefixFound || (message.toLowerCase().startsWith(guildPrefix.toLowerCase()))) {
            //PREFIX FOUND

            Long guildID = event.getGuild().getIdLong();

            String content = null;
            if (defaultPrefixFound) {
                content = message.substring(SherlockBot.defaultPrefix.length());
            } else {
                content = message.substring(guildPrefix.length());
            }


            //SELF ROLES
            try {
                if (SherlockBot.database.getTableCount(event.getGuild().getIdLong(),"SelfRoles") > 0) {

                    Map<String,Long> guildSelfRoleMap = SherlockBot.database.getGuildSelfRoles(guildID);

                    //ITERATE THROUGH GUILD SELF ROLE MAP
                    for (Map.Entry<String, Long> entry : guildSelfRoleMap.entrySet()) {
                        //check content for trigger (Ignoring case)
                        if (entry.getKey().equalsIgnoreCase(content)) {
                            //ENTRY FOUND
                            Long roleID = entry.getValue();
                            handleSelfRoleEvent(event,roleID);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


        }
    }

    private void handleSelfRoleEvent(final GuildMessageReceivedEvent event, final Long roleID){

        Role role = event.getGuild().getRoleById(roleID);
        if (role != null) {

            try {

                if (event.getMember().getRoles().contains(role)) {
                    event.getGuild().removeRoleFromMember(event.getMember().getIdLong(), role).reason("Requested via SelfRole").queue(success -> {
                        event.getMessage().addReaction("✅").queue();
                    });
                } else {
                    event.getGuild().addRoleToMember(event.getMember().getIdLong(), role).reason("Requested via SelfRole").queue(success -> {
                        event.getMessage().addReaction("✅").queue();
                    });
                }

            } catch (PermissionException permissionException) {
                event.getMessage().reply("Missing Permissions: " + permissionException.getPermission().toString()).queue();
                event.getMessage().addReaction("⚠").queue();
            }
        }
    }


}
