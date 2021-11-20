package rsystems.events;

import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.sql.SQLException;

public class GuildChannelEvents extends ListenerAdapter {

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        final Long guildID = event.getGuild().getIdLong();

        if((SherlockBot.guildMap.get(guildID).getLogChannelID() != null) && (SherlockBot.guildMap.get(guildID).getLogChannelID().equals(event.getChannel().getIdLong()))){
            SherlockBot.guildMap.get(guildID).setLogChannelID(null);
            try {
                SherlockBot.database.updateGuild(SherlockBot.guildMap.get(guildID));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
