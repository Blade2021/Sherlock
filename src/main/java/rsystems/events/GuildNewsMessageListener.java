package rsystems.events;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.sql.SQLException;

public class GuildNewsMessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if(event.isFromType(ChannelType.NEWS)){
            try {
                Boolean checkDatabase = SherlockBot.database.checkForChannelID(event.getGuild().getIdLong(),event.getChannel().getIdLong());

                if(checkDatabase){
                    event.getMessage().crosspost().queue();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
