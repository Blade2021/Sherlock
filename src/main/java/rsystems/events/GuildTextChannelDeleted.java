package rsystems.events;

import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

public class GuildTextChannelDeleted extends ListenerAdapter {

    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        SherlockBot.database.deleteRow("ArchiveTable","ChannelID",event.getChannel().getIdLong());
    }
}
