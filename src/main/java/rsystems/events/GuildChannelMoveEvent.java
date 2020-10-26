package rsystems.events;

import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateParentEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

public class GuildChannelMoveEvent extends ListenerAdapter {

/*
Protection against moving a channel out of the archive category.
 */
    public void onTextChannelUpdateParent( TextChannelUpdateParentEvent event) {
        if(event.getOldParent().getIdLong() == SherlockBot.guildMap.get(event.getGuild().getId()).getArchiveCategoryID()){
            SherlockBot.database.deleteRow("ArchiveTable","ChannelID",event.getChannel().getIdLong());
        }
    }
}
