package rsystems.events;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.Config;
import rsystems.SherlockBot;

import java.util.Map;

public class GuildMessageEvent extends ListenerAdapter {

    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {

        final String prefix = SherlockBot.guildMap.get(event.getGuild().getIdLong()).getPrefix();
        String message = event.getMessage().getContentRaw();

        if ((message.toLowerCase().startsWith(prefix.toLowerCase())) || (message.toLowerCase().startsWith(Config.get("defaultPrefix").toLowerCase()))) {
            //PREFIX FOUND

            Long guildID = event.getGuild().getIdLong();

            String content = null;
            if (message.toLowerCase().startsWith(prefix.toLowerCase())) {
                content = message.substring(prefix.length());
            } else {
                content = message.substring(Config.get("defaultPrefix").length());
            }


            //SELF ROLES
            if (SherlockBot.database.getSelfRoles(guildID).size() > 0) {

                //ITERATE THROUGH GUILD SELF ROLE MAP
                for (Map.Entry<String, Long> entry : SherlockBot.database.getGuildSelfRoles(guildID).entrySet()) {

                    //check content for trigger (Ignoring case)
                    if (entry.getKey().equalsIgnoreCase(content)) {
                        //ENTRY FOUND

                        Long roleID = entry.getValue();
                        handleSelfRoleEvent(event,roleID);

                    }
                }
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
