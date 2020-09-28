package rsystems.events;

import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.handlers.LogChannel;

import static rsystems.SherlockBot.database;

public class BanListener extends ListenerAdapter {

    public void onGuildBan(GuildBanEvent event){
        database.putString("ElevatedEvents",event.getGuild().getIdLong(),"User banned",null,event.getUser().getIdLong());
        LogChannel logChannel = new LogChannel();
        logChannel.logAction(event.getGuild(),"User Banned",event.getUser());
    }

}
