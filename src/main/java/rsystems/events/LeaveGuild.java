package rsystems.events;

import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import static rsystems.SherlockBot.database;

public class LeaveGuild extends ListenerAdapter {
    public void onGuildLeave(GuildLeaveEvent event){
        //Initiate and store the new guild in the guildSettings Object
        SherlockBot.guildMap.remove(event.getGuild().getId());
        if(!database.removeGuild(event.getGuild().getId())){
            System.out.println("Failed to remove guild");
        }
    }
}
