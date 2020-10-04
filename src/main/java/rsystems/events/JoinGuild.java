package rsystems.events;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.objects.GuildSettings;

import java.awt.*;
import java.util.ArrayList;

import static rsystems.SherlockBot.database;

/*
AUTHOR: Matt W.

DESCRIPTION: This class will setup the guild roles, and channel overrides when joining a guild.
 */

public class JoinGuild extends ListenerAdapter {
    public void onGuildJoin(GuildJoinEvent event) {
        //Initiate and store the new guild in the guildSettings Object
        SherlockBot.guildMap.put(event.getGuild().getId(),new GuildSettings("!"));
        createMuteRole(event.getGuild());
        database.addGuild(event.getGuild().getId(),event.getGuild().getOwnerId());
    }

    /*
    CREATE MUTE ROLE & ASSIGN IT APPLICABLE PERMISSIONS
     */
    private void createMuteRole(Guild guild) {
        guild.createRole()
                .setName("Mute")
                .setPermissions(Permission.EMPTY_PERMISSIONS)
                .setColor(Color.DARK_GRAY).queue(success -> {

            ArrayList<Permission> mutePerms = new ArrayList<>();
            mutePerms.add(Permission.MESSAGE_WRITE);
            mutePerms.add(Permission.MESSAGE_ADD_REACTION);
            mutePerms.add(Permission.VOICE_STREAM);
            mutePerms.add(Permission.VOICE_SPEAK);

            for(Category category : guild.getCategories()){
                try{
                    category.createPermissionOverride(success).setDeny(mutePerms).reason("Initiating bot perms").queue();
                } catch (PermissionException e){
                    break;
                }
            }

            //Set the mute role permission override for each channel
            for (TextChannel channel : guild.getTextChannels()) {
                try {
                    channel.createPermissionOverride(success).setDeny(mutePerms).queue();
                } catch (PermissionException e) {
                    break;
                }
            }

            for (VoiceChannel voiceChannel:guild.getVoiceChannels()){
                try{
                    voiceChannel.createPermissionOverride(success).setDeny(mutePerms).queue();
                } catch(PermissionException e){
                    break;
                }
            }

            //Write the roleID to settings for later use.
            SherlockBot.guildMap.get(guild.getId()).setMuteRoleID(success.getId());
            database.putValue("GuildTable","MuteRoleID","GuildID",guild.getIdLong(),success.getIdLong());
        });
    }
}
